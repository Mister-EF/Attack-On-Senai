package io.github.AOS.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.AOS.Main;
import io.github.AOS.input.MenuInputHandler;
import io.github.AOS.systems.CheatManager;
import io.github.AOS.utils.PlaceholderFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Tela do Menu Principal do Attack on SENAI.
 * Oferece as opções GO, PONTUAÇÃO (Leaderboard modal), SAIR, botão Fullscreen
 * e o Botão Secreto no canto superior esquerdo para ativar o display de Cheats.
 */
public class MainMenuScreen implements Screen {

    public static final float VIRTUAL_WIDTH = 1280f;
    public static final float VIRTUAL_HEIGHT = 720f;

    private final Main game;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final MenuInputHandler inputHandler;

    // Fontes e alinhamento de texto
    private final BitmapFont titleFont;
    private final BitmapFont regularFont;
    private final GlyphLayout glyphLayout;

    // Texturas de interface (geradas via Pixmap)
    private final Texture backgroundTexture;
    private final Texture buttonTexture;
    private final Texture buttonHoverTexture;
    private final Texture headerAccentTexture;
    private final Texture modalOverlayTexture;
    private final Texture modalPanelTexture;
    private final Texture fullscreenBtnTexture;
    private final Texture secretCheatHoverTexture;

    // Retângulos de colisão para cliques de mouse
    private final Rectangle btnGoBounds;
    private final Rectangle btnLeaderboardBounds;
    private final Rectangle btnExitBounds;
    private final Rectangle btnFullscreenBounds;
    private final Rectangle btnSecretCheatBounds;
    private final Rectangle modalBounds;
    private final Rectangle btnCloseModalBounds;

    // Estados da interface
    private boolean showLeaderboardModal = false;
    private int hoveredButton = 0; // 1: GO, 2: Leaderboard, 3: Exit, 4: Fullscreen, 5: Close Modal, 6: Secret Cheat

    // Lista estática temporária das últimas 5 pontuações em memória
    private static final List<String> RECENT_SCORES = Arrays.asList(
            "1º - 18.500 pts | Aprovado com Louvor",
            "2º - 14.200 pts | Especialista Back-End",
            "3º - 11.850 pts | Sobrevivente do SENAI",
            "4º - 09.400 pts | Desafio Módulo 2",
            "5º - 06.200 pts | Primeira Batalha"
    );

    public MainMenuScreen(Main game) {
        this.game = game;

        // Configuração de câmera e FitViewport 1280x720
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0f);

        this.inputHandler = new MenuInputHandler(this, viewport);

        // Configuração de fontes LibGDX nativas
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3.2f);
        this.titleFont.setColor(Color.GREEN);

        this.regularFont = new BitmapFont();
        this.regularFont.getData().setScale(1.5f);
        this.regularFont.setColor(Color.WHITE);

        this.glyphLayout = new GlyphLayout();

        // Criação de texturas de UI via PlaceholderFactory
        this.backgroundTexture = PlaceholderFactory.createBackgroundTexture();
        this.buttonTexture = PlaceholderFactory.createColoredTexture(new Color(0.16f, 0.16f, 0.22f, 1f));
        this.buttonHoverTexture = PlaceholderFactory.createColoredTexture(new Color(0.20f, 0.55f, 0.28f, 1f));
        this.headerAccentTexture = PlaceholderFactory.createColoredTexture(Color.GREEN);
        this.modalOverlayTexture = PlaceholderFactory.createColoredTexture(new Color(0f, 0f, 0f, 0.75f));
        this.modalPanelTexture = PlaceholderFactory.createColoredTexture(new Color(0.10f, 0.10f, 0.15f, 0.98f));
        this.fullscreenBtnTexture = PlaceholderFactory.createColoredTexture(new Color(0.22f, 0.22f, 0.30f, 1f));

        // Verde mais forte possível (opaco/alfa máximo 1.0) para o hover do botão secreto
        this.secretCheatHoverTexture = PlaceholderFactory.createColoredTexture(new Color(0f, 1f, 0f, 1f));

        // Geometria dos botões de menu
        float btnWidth = 360f;
        float btnHeight = 60f;
        float btnCenterX = (VIRTUAL_WIDTH - btnWidth) / 2f;

        this.btnGoBounds = new Rectangle(btnCenterX, 380f, btnWidth, btnHeight);
        this.btnLeaderboardBounds = new Rectangle(btnCenterX, 290f, btnWidth, btnHeight);
        this.btnExitBounds = new Rectangle(btnCenterX, 200f, btnWidth, btnHeight);

        // Botão Fullscreen no canto superior direito
        float fsWidth = 140f;
        float fsHeight = 44f;
        this.btnFullscreenBounds = new Rectangle(VIRTUAL_WIDTH - fsWidth - 20f, VIRTUAL_HEIGHT - fsHeight - 20f, fsWidth, fsHeight);

        // Botão Secreto no canto superior esquerdo (invisível por padrão, verde forte ao passar mouse)
        this.btnSecretCheatBounds = new Rectangle(15f, VIRTUAL_HEIGHT - 55f, 65f, 40f);

        // Modal de pontuação
        float modalWidth = 560f;
        float modalHeight = 420f;
        float modalX = (VIRTUAL_WIDTH - modalWidth) / 2f;
        float modalY = (VIRTUAL_HEIGHT - modalHeight) / 2f;
        this.modalBounds = new Rectangle(modalX, modalY, modalWidth, modalHeight);
        this.btnCloseModalBounds = new Rectangle(modalX + (modalWidth - 200f) / 2f, modalY + 30f, 200f, 50f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        camera.update();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // 1. Fundo Preto preenchendo a tela
        batch.draw(backgroundTexture, 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // 2. Título do Jogo
        drawCenteredText(batch, titleFont, "ATTACK ON SENAI", 580f);

        // Faixa de acento visual abaixo do título
        float barWidth = 420f;
        batch.draw(headerAccentTexture, (VIRTUAL_WIDTH - barWidth) / 2f, 515f, barWidth, 4f);

        // Subtítulo
        regularFont.setColor(Color.LIGHT_GRAY);
        drawCenteredText(batch, regularFont, "BOSS RUSH PLATFORMER", 495f);

        // 3. Botões Principais
        renderMenuButton(batch, btnGoBounds, "GO", hoveredButton == 1);
        renderMenuButton(batch, btnLeaderboardBounds, "PONTUAÇÃO", hoveredButton == 2);
        renderMenuButton(batch, btnExitBounds, "SAIR", hoveredButton == 3);

        // 4. Botão Fullscreen no canto superior direito
        renderFullscreenButton(batch);

        // 5. Botão Secreto no canto superior esquerdo (só desenha se o mouse estiver sobre ele com verde forte opaco)
        if (hoveredButton == 6) {
            batch.draw(secretCheatHoverTexture, btnSecretCheatBounds.x, btnSecretCheatBounds.y, btnSecretCheatBounds.width, btnSecretCheatBounds.height);
        }

        // 6. Painel Modal de Pontuação (Leaderboard) quando ativado
        if (showLeaderboardModal) {
            renderLeaderboardModal(batch);
        }

        batch.end();
    }

    private void renderMenuButton(SpriteBatch batch, Rectangle bounds, String text, boolean isHovered) {
        Texture tex = isHovered ? buttonHoverTexture : buttonTexture;
        batch.draw(tex, bounds.x, bounds.y, bounds.width, bounds.height);

        regularFont.setColor(isHovered ? Color.WHITE : Color.LIGHT_GRAY);
        glyphLayout.setText(regularFont, text);
        float textX = bounds.x + (bounds.width - glyphLayout.width) / 2f;
        float textY = bounds.y + (bounds.height + glyphLayout.height) / 2f;
        regularFont.draw(batch, glyphLayout, textX, textY);
    }

    private void renderFullscreenButton(SpriteBatch batch) {
        boolean isHovered = (hoveredButton == 4);
        Texture tex = isHovered ? buttonHoverTexture : fullscreenBtnTexture;
        batch.draw(tex, btnFullscreenBounds.x, btnFullscreenBounds.y, btnFullscreenBounds.width, btnFullscreenBounds.height);

        String fsText = Gdx.graphics.isFullscreen() ? "[ JANELA ]" : "[ FULLSCREEN ]";
        regularFont.setColor(isHovered ? Color.WHITE : Color.LIGHT_GRAY);
        glyphLayout.setText(regularFont, fsText);
        float textX = btnFullscreenBounds.x + (btnFullscreenBounds.width - glyphLayout.width) / 2f;
        float textY = btnFullscreenBounds.y + (btnFullscreenBounds.height + glyphLayout.height) / 2f;
        regularFont.draw(batch, glyphLayout, textX, textY);
    }

    private void renderLeaderboardModal(SpriteBatch batch) {
        batch.draw(modalOverlayTexture, 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.draw(modalPanelTexture, modalBounds.x, modalBounds.y, modalBounds.width, modalBounds.height);
        batch.draw(headerAccentTexture, modalBounds.x, modalBounds.y + modalBounds.height - 6f, modalBounds.width, 6f);

        regularFont.setColor(Color.GREEN);
        glyphLayout.setText(regularFont, "ÚLTIMAS PONTUAÇÕES");
        float titleX = modalBounds.x + (modalBounds.width - glyphLayout.width) / 2f;
        regularFont.draw(batch, glyphLayout, titleX, modalBounds.y + modalBounds.height - 35f);

        regularFont.setColor(Color.WHITE);
        float itemY = modalBounds.y + modalBounds.height - 95f;
        for (String scoreEntry : RECENT_SCORES) {
            glyphLayout.setText(regularFont, scoreEntry);
            float itemX = modalBounds.x + (modalBounds.width - glyphLayout.width) / 2f;
            regularFont.draw(batch, glyphLayout, itemX, itemY);
            itemY -= 48f;
        }

        renderMenuButton(batch, btnCloseModalBounds, "FECHAR", hoveredButton == 5);
    }

    private void drawCenteredText(SpriteBatch batch, BitmapFont font, String text, float y) {
        glyphLayout.setText(font, text);
        float x = (VIRTUAL_WIDTH - glyphLayout.width) / 2f;
        font.draw(batch, glyphLayout, x, y);
    }

    public boolean handleClick(float worldX, float worldY) {
        if (showLeaderboardModal) {
            if (btnCloseModalBounds.contains(worldX, worldY) || !modalBounds.contains(worldX, worldY)) {
                showLeaderboardModal = false;
                return true;
            }
            return true;
        }

        // Botão Secreto no canto superior esquerdo: ativa/desativa cheats
        if (btnSecretCheatBounds.contains(worldX, worldY)) {
            CheatManager.toggleCheatsDisplay();
            return true;
        }

        // Botão Fullscreen
        if (btnFullscreenBounds.contains(worldX, worldY)) {
            toggleFullscreen();
            return true;
        }

        // Opção GO -> Vai para a tela da fase
        if (btnGoBounds.contains(worldX, worldY)) {
            game.setScreen(new BossArenaScreen(game));
            return true;
        }

        // Opção PONTUAÇÃO -> Abre modal
        if (btnLeaderboardBounds.contains(worldX, worldY)) {
            showLeaderboardModal = true;
            return true;
        }

        // Opção SAIR -> Encerra
        if (btnExitBounds.contains(worldX, worldY)) {
            Gdx.app.exit();
            return true;
        }

        return false;
    }

    public void handleMouseMove(float worldX, float worldY) {
        if (showLeaderboardModal) {
            hoveredButton = btnCloseModalBounds.contains(worldX, worldY) ? 5 : 0;
            return;
        }

        if (btnSecretCheatBounds.contains(worldX, worldY)) {
            hoveredButton = 6;
        } else if (btnGoBounds.contains(worldX, worldY)) {
            hoveredButton = 1;
        } else if (btnLeaderboardBounds.contains(worldX, worldY)) {
            hoveredButton = 2;
        } else if (btnExitBounds.contains(worldX, worldY)) {
            hoveredButton = 3;
        } else if (btnFullscreenBounds.contains(worldX, worldY)) {
            hoveredButton = 4;
        } else {
            hoveredButton = 0;
        }
    }

    public void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode((int) VIRTUAL_WIDTH, (int) VIRTUAL_HEIGHT);
        } else {
            Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(currentMode);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        regularFont.dispose();
        backgroundTexture.dispose();
        buttonTexture.dispose();
        buttonHoverTexture.dispose();
        headerAccentTexture.dispose();
        modalOverlayTexture.dispose();
        modalPanelTexture.dispose();
        fullscreenBtnTexture.dispose();
        secretCheatHoverTexture.dispose();
    }
}
