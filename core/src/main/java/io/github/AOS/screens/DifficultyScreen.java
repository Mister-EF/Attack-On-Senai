package io.github.AOS.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.AOS.Main;
import io.github.AOS.systems.Difficulty;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Tela de Seleção de Dificuldade do "Attack on SENAI".
 * Exibida após clicar em "GO" no Menu Principal.
 * Apresenta três opções: Fácil, Médio e Difícil.
 * Ao escolher, inicia a BossArenaScreen com o multiplicador de dificuldade correto.
 */
public class DifficultyScreen implements Screen {

    private static final float VIRTUAL_WIDTH  = 1280f;
    private static final float VIRTUAL_HEIGHT = 720f;

    // Layout dos botões de dificuldade
    private static final float BTN_WIDTH  = 320f;
    private static final float BTN_HEIGHT = 72f;
    private static final float BTN_GAP    = 28f;
    private static final float BTN_CENTER_X = (VIRTUAL_WIDTH - BTN_WIDTH) / 2f;

    // A posição Y do bloco de botões é centralizada verticalmente
    private static final float BLOCK_HEIGHT = BTN_HEIGHT * 3 + BTN_GAP * 2;
    private static final float BLOCK_BOTTOM  = (VIRTUAL_HEIGHT - BLOCK_HEIGHT) / 2f - 30f;
    private static final float BTN_Y_FACIL  = BLOCK_BOTTOM + (BTN_HEIGHT + BTN_GAP) * 2;
    private static final float BTN_Y_MEDIO  = BLOCK_BOTTOM + (BTN_HEIGHT + BTN_GAP);
    private static final float BTN_Y_DIFICIL = BLOCK_BOTTOM;

    private final Main game;
    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final BitmapFont titleFont;
    private final BitmapFont btnFont;
    private final GlyphLayout layout;

    private final Texture bgTexture;
    private final Texture btnNormalTexture;
    private final Texture btnHoverFacilTexture;
    private final Texture btnHoverMedioTexture;
    private final Texture btnHoverDificilTexture;
    private final Texture accentTexture;

    private final Rectangle btnFacil;
    private final Rectangle btnMedio;
    private final Rectangle btnDificil;

    /** 0 = none, 1 = Fácil, 2 = Médio, 3 = Difícil */
    private int hoveredBtn = 0;

    public DifficultyScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0f);

        // Fontes
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.6f);
        titleFont.setColor(Color.GREEN);

        btnFont = new BitmapFont();
        btnFont.getData().setScale(2.1f);
        btnFont.setColor(Color.WHITE);

        layout = new GlyphLayout();

        // Texturas geradas via Pixmap (AGENTS.md — sem assets externos)
        bgTexture = PlaceholderFactory.createBackgroundTexture();

        btnNormalTexture     = makeBtnTexture(new Color(0.08f, 0.08f, 0.08f, 0.92f), new Color(0.3f, 0.3f, 0.3f, 1f));
        btnHoverFacilTexture  = makeBtnTexture(new Color(0.0f,  0.4f,  0.0f,  0.95f), Color.GREEN);
        btnHoverMedioTexture  = makeBtnTexture(new Color(0.4f,  0.35f, 0.0f,  0.95f), Color.YELLOW);
        btnHoverDificilTexture= makeBtnTexture(new Color(0.4f,  0.0f,  0.0f,  0.95f), Color.RED);

        // Acento decorativo acima do título
        accentTexture = makeAccentTexture();

        // Hitboxes de clique
        btnFacil  = new Rectangle(BTN_CENTER_X, BTN_Y_FACIL,  BTN_WIDTH, BTN_HEIGHT);
        btnMedio  = new Rectangle(BTN_CENTER_X, BTN_Y_MEDIO,  BTN_WIDTH, BTN_HEIGHT);
        btnDificil = new Rectangle(BTN_CENTER_X, BTN_Y_DIFICIL, BTN_WIDTH, BTN_HEIGHT);

        // Input simples via InputAdapter
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float[] world = screenToWorld(screenX, screenY);
                float wx = world[0], wy = world[1];
                if (btnFacil.contains(wx, wy)) {
                    launch(Difficulty.FACIL);
                } else if (btnMedio.contains(wx, wy)) {
                    launch(Difficulty.MEDIO);
                } else if (btnDificil.contains(wx, wy)) {
                    launch(Difficulty.DIFICIL);
                }
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                float[] world = screenToWorld(screenX, screenY);
                float wx = world[0], wy = world[1];
                if (btnFacil.contains(wx, wy))        hoveredBtn = 1;
                else if (btnMedio.contains(wx, wy))   hoveredBtn = 2;
                else if (btnDificil.contains(wx, wy)) hoveredBtn = 3;
                else                                   hoveredBtn = 0;
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                game.setScreen(new MainMenuScreen(game));
                return true;
            }
            // Atalhos rápidos para a apresentação (1=Fácil, 2=Médio, 3=Difícil)
            if (keycode == Input.Keys.NUM_1 || keycode == Input.Keys.NUMPAD_1) {
                game.setScreen(new BossArenaScreen(game, 0.5f)); // Fácil (0.5x)
                return true;
            }
            if (keycode == Input.Keys.NUM_2 || keycode == Input.Keys.NUMPAD_2) {
                game.setScreen(new BossArenaScreen(game, 1.0f)); // Médio (1.0x)
                return true;
            }
            if (keycode == Input.Keys.NUM_3 || keycode == Input.Keys.NUMPAD_3) {
                game.setScreen(new BossArenaScreen(game, 1.5f)); // Difícil (1.5x)
                return true;
            }
            return false;
            }
        });
    }

    private void launch(Difficulty difficulty) {
        game.setScreen(new BossArenaScreen(game, difficulty));
    }

    private float[] screenToWorld(int screenX, int screenY) {
        camera.update();
        com.badlogic.gdx.math.Vector3 v = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
        camera.unproject(v, viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());
        return new float[]{v.x, v.y};
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        camera.update();
        viewport.apply();

        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Fundo
        batch.draw(bgTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Acento decorativo
        float accentW = 400f, accentH = 6f;
        batch.draw(accentTexture, (VIRTUAL_WIDTH - accentW) / 2f, 548f, accentW, accentH);

        // Título
        drawCentered(batch, titleFont, "SELECIONE A DIFICULDADE", 530f);

        // Subtítulo de instrução
        BitmapFont sub = new BitmapFont();
        sub.getData().setScale(1.1f);
        sub.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        drawCentered(batch, sub, "ESC para voltar ao Menu", 490f);
        sub.dispose();

        // Botão Fácil
        Texture fTex = (hoveredBtn == 1) ? btnHoverFacilTexture : btnNormalTexture;
        batch.draw(fTex, btnFacil.x, btnFacil.y, btnFacil.width, btnFacil.height);
        BitmapFont fFont = btnFont;
        fFont.setColor(hoveredBtn == 1 ? Color.WHITE : new Color(0.5f, 0.9f, 0.5f, 1f));
        drawCentered(batch, fFont, "Fácil  — 0.5x Boss Speed", btnFacil.y + BTN_HEIGHT * 0.65f);

        // Botão Médio
        Texture mTex = (hoveredBtn == 2) ? btnHoverMedioTexture : btnNormalTexture;
        batch.draw(mTex, btnMedio.x, btnMedio.y, btnMedio.width, btnMedio.height);
        fFont.setColor(hoveredBtn == 2 ? Color.WHITE : new Color(0.9f, 0.9f, 0.5f, 1f));
        drawCentered(batch, fFont, "Médio  — 1.0x Boss Speed", btnMedio.y + BTN_HEIGHT * 0.65f);

        // Botão Difícil
        Texture dTex = (hoveredBtn == 3) ? btnHoverDificilTexture : btnNormalTexture;
        batch.draw(dTex, btnDificil.x, btnDificil.y, btnDificil.width, btnDificil.height);
        fFont.setColor(hoveredBtn == 3 ? Color.WHITE : new Color(0.9f, 0.4f, 0.4f, 1f));
        drawCentered(batch, fFont, "Difícil — 1.5x Boss Speed", btnDificil.y + BTN_HEIGHT * 0.65f);

        batch.end();
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float y) {
        layout.setText(font, text);
        font.draw(batch, text, (VIRTUAL_WIDTH - layout.width) / 2f, y);
    }

    // ── Utilitários de criação de texturas via Pixmap ──────────────────────────

    /** Cria textura de botão com fill sólido e borda colorida. */
    private static Texture makeBtnTexture(Color fill, Color border) {
        int w = (int) BTN_WIDTH;
        int h = (int) BTN_HEIGHT;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        // Fill
        pm.setColor(fill);
        pm.fillRectangle(0, 0, w, h);
        // Borda
        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);
        pm.drawRectangle(1, 1, w - 2, h - 2);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /** Linha de acento decorativa verde. */
    private static Texture makeAccentTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.GREEN);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // ── Ciclo de vida do Screen ────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float[] world = screenToWorld(screenX, screenY);
                float wx = world[0], wy = world[1];
                if (btnFacil.contains(wx, wy)) {
                    launch(Difficulty.FACIL);
                } else if (btnMedio.contains(wx, wy)) {
                    launch(Difficulty.MEDIO);
                } else if (btnDificil.contains(wx, wy)) {
                    launch(Difficulty.DIFICIL);
                }
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                float[] world = screenToWorld(screenX, screenY);
                float wx = world[0], wy = world[1];
                if (btnFacil.contains(wx, wy))        hoveredBtn = 1;
                else if (btnMedio.contains(wx, wy))   hoveredBtn = 2;
                else if (btnDificil.contains(wx, wy)) hoveredBtn = 3;
                else                                  hoveredBtn = 0;
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new MainMenuScreen(game));
                    return true;
                }
                // Atalhos rápidos para a apresentação (1=Fácil, 2=Médio, 3=Difícil)
                if (keycode == Input.Keys.NUM_1 || keycode == Input.Keys.NUMPAD_1) {
                    game.setScreen(new BossArenaScreen(game, 0.5f));
                    return true;
                }
                if (keycode == Input.Keys.NUM_2 || keycode == Input.Keys.NUMPAD_2) {
                    game.setScreen(new BossArenaScreen(game, 1.0f));
                    return true;
                }
                if (keycode == Input.Keys.NUM_3 || keycode == Input.Keys.NUMPAD_3) {
                    game.setScreen(new BossArenaScreen(game, 1.5f));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        bgTexture.dispose();
        btnNormalTexture.dispose();
        btnHoverFacilTexture.dispose();
        btnHoverMedioTexture.dispose();
        btnHoverDificilTexture.dispose();
        accentTexture.dispose();
        titleFont.dispose();
        btnFont.dispose();
    }
}
