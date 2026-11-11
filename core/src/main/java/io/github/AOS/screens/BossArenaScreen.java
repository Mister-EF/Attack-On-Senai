package io.github.AOS.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.AOS.Main;
import io.github.AOS.combat.CombatSystem;
import io.github.AOS.entities.Minion;
import io.github.AOS.entities.Player;
import io.github.AOS.entities.PowerUp;
import io.github.AOS.entities.bosses.BossC;
import io.github.AOS.input.PlayerInputHandler;
import io.github.AOS.systems.CheatManager;
import io.github.AOS.utils.PlaceholderFactory;
import io.github.AOS.world.Hazard;
import io.github.AOS.world.ParallaxBackground;
import io.github.AOS.world.Platform;

/**
 * Tela da Arena e Fase de Plataforma 2D:
 * - Etapa vertical com múltiplas plataformas, espinhos (hazards) e inimigos básicos vermelhos (Minions).
 * - Arena final com 1 único Boss (BossC).
 * - Menu de Pausa com ESC (Recomeçar / Sair).
 * - Tela de Vitória com mensagem exata "parabéns! ✌️ ✌️".
 * - Cheats ocultos por padrão, ativáveis via botão secreto no Menu Principal.
 */
public class BossArenaScreen implements Screen, PlayerInputHandler.ArenaInputListener {

    public static final float VIRTUAL_WIDTH = 1280f;
    public static final float VIRTUAL_HEIGHT = 720f;

    // Dimensões do Mundo com Verticalidade
    public static final float WORLD_WIDTH = 4200f;
    public static final float WORLD_HEIGHT = 900f;
    private static final float GROUND_HEIGHT = 80f;

    private final Main game;

    // Câmera do mundo de jogo
    private final OrthographicCamera camera;
    private final FitViewport viewport;

    // Câmera e ferramentas fixas da HUD
    private final OrthographicCamera hudCamera;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont hudFont;
    private final BitmapFont titleFont;
    private final BitmapFont smallFont;
    private final GlyphLayout glyphLayout;
    private final Vector3 mouseCoords;

    // Cenário e plataformas
    private final ParallaxBackground parallaxBackground;
    private final Texture groundTexture;
    private final Rectangle startGroundBounds;
    private final Rectangle bossArenaGroundBounds;

    private final Array<Platform> platforms;
    private final Array<Hazard> hazards;
    private final Array<Minion> minions;
    private final Array<PowerUp> powerUps;

    // Entidades e Combate
    private Player player;
    private BossC bossC; // 1 ÚNICO BOSS FINAL
    private final CombatSystem combatSystem;
    private final CheatManager cheatManager;
    private PlayerInputHandler inputHandler;
    private int powerUpCycleIndex = 0;

    // Estados de Pausa (Tecla ESC)
    private boolean isPaused = false;
    private final Rectangle pauseModalBounds;
    private final Rectangle btnPauseRestartBounds;
    private final Rectangle btnPauseExitBounds;
    private int hoveredPauseButton = 0; // 1: Recomeçar, 2: Sair

    // Texturas de interface do menu de pausa
    private final Texture modalOverlayTexture;
    private final Texture modalPanelTexture;
    private final Texture btnTexture;
    private final Texture btnHoverTexture;

    public BossArenaScreen(Main game) {
        this.game = game;

        // Câmera do mundo
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0f);

        // Câmera estática da HUD
        this.hudCamera = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        this.hudCamera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0f);
        this.hudCamera.update();

        this.shapeRenderer = new ShapeRenderer();
        this.hudFont = new BitmapFont();
        this.hudFont.getData().setScale(1.2f);

        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2.2f);

        this.smallFont = new BitmapFont();
        this.smallFont.getData().setScale(0.95f);

        this.glyphLayout = new GlyphLayout();
        this.mouseCoords = new Vector3();

        // Cenário e chão base
        this.parallaxBackground = new ParallaxBackground();
        this.groundTexture = PlaceholderFactory.createGroundTexture();

        // Chão inicial e chão da arena do boss (com fosso entre eles)
        this.startGroundBounds = new Rectangle(0f, 0f, 320f, GROUND_HEIGHT);
        this.bossArenaGroundBounds = new Rectangle(2400f, 0f, 1800f, GROUND_HEIGHT);

        // Coleções do mundo
        this.platforms = new Array<>();
        this.hazards = new Array<>();
        this.minions = new Array<>();
        this.powerUps = new Array<>();

        // Combate e cheats
        this.combatSystem = new CombatSystem();
        this.cheatManager = new CheatManager();

        // Geometria do Menu de Pausa
        float pWidth = 440f;
        float pHeight = 280f;
        float pX = (VIRTUAL_WIDTH - pWidth) / 2f;
        float pY = (VIRTUAL_HEIGHT - pHeight) / 2f;
        this.pauseModalBounds = new Rectangle(pX, pY, pWidth, pHeight);

        float bWidth = 280f;
        float bHeight = 50f;
        float bX = pX + (pWidth - bWidth) / 2f;
        this.btnPauseRestartBounds = new Rectangle(bX, pY + 115f, bWidth, bHeight);
        this.btnPauseExitBounds = new Rectangle(bX, pY + 45f, bWidth, bHeight);

        this.modalOverlayTexture = PlaceholderFactory.createColoredTexture(new Color(0f, 0f, 0f, 0.75f));
        this.modalPanelTexture = PlaceholderFactory.createColoredTexture(new Color(0.12f, 0.12f, 0.18f, 0.98f));
        this.btnTexture = PlaceholderFactory.createColoredTexture(new Color(0.20f, 0.20f, 0.28f, 1f));
        this.btnHoverTexture = PlaceholderFactory.createColoredTexture(new Color(0.20f, 0.60f, 0.30f, 1f));

        // Inicializa mapa, entidades e plataformas
        initBattle();
    }

    /**
     * Inicializa ou reinicia todos os elementos da fase.
     */
    private void initBattle() {
        this.isPaused = false;

        if (player != null) player.dispose();
        if (bossC != null) bossC.dispose();

        for (Platform p : platforms) p.dispose();
        platforms.clear();

        for (Hazard h : hazards) h.dispose();
        hazards.clear();

        for (Minion m : minions) m.dispose();
        minions.clear();

        for (PowerUp pu : powerUps) pu.dispose();
        powerUps.clear();

        // Jogador posicionado no início da fase
        this.player = new Player(80f, GROUND_HEIGHT);

        // 1 ÚNICO BOSS FINAL posicionado na arena à direita
        this.bossC = new BossC(3300f, GROUND_HEIGHT);

        // Construção do percurso com verticalidade e múltiplas plataformas
        platforms.add(new Platform(360f, 170f, 260f, 24f));
        platforms.add(new Platform(680f, 290f, 280f, 24f));
        platforms.add(new Platform(1040f, 420f, 240f, 24f));
        platforms.add(new Platform(1360f, 540f, 340f, 24f)); // Ponto mais alto
        platforms.add(new Platform(1780f, 400f, 280f, 24f));
        platforms.add(new Platform(2120f, 240f, 240f, 24f));

        // Obstáculos letais fixos (espinhos no fosso do chão)
        hazards.add(new Hazard(320f, 0f, 2080f, 45f, 40));

        // Inimigos básicos (Minions vermelhos) patrulhando as plataformas suspensas
        minions.add(new Minion(700f, 314f, 680f, 910f));
        minions.add(new Minion(1380f, 564f, 1360f, 1650f));
        minions.add(new Minion(1800f, 424f, 1780f, 2010f));

        // Power-ups temáticos de IA na fase
        powerUps.add(new PowerUp(PowerUp.Type.GEMINI, 500f, 210f));
        powerUps.add(new PowerUp(PowerUp.Type.CLAUDE, 1500f, 580f));
        powerUps.add(new PowerUp(PowerUp.Type.CHATGPT, 2550f, GROUND_HEIGHT + 30f));

        this.inputHandler = new PlayerInputHandler(player);
        this.inputHandler.setArenaListener(this);
        Gdx.input.setInputProcessor(inputHandler);
    }

    public void spawnPowerUpAtPlayer() {
        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type nextType = types[powerUpCycleIndex % types.length];
        powerUpCycleIndex++;

        float spawnX = player.getPosition().x;
        float spawnY = player.getPosition().y + 40f;
        powerUps.add(new PowerUp(nextType, spawnX, spawnY));
    }

    public void forceVictory() {
        if (bossC != null && !bossC.isDead()) {
            bossC.takeDamage(999999);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {
        // Se o jogo NÃO estiver pausado, executa a física e atualizações
        if (!isPaused) {
            updateGame(delta);
        }

        // Limpeza de buffer
        ScreenUtils.clear(Color.BLACK);

        // Renderização do mundo de jogo
        viewport.apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // Fundo em paralaxe
        parallaxBackground.render(batch, camera);

        // Chão inicial e chão da arena do boss
        batch.draw(groundTexture, startGroundBounds.x, startGroundBounds.y, startGroundBounds.width, startGroundBounds.height);
        batch.draw(groundTexture, bossArenaGroundBounds.x, bossArenaGroundBounds.y, bossArenaGroundBounds.width, bossArenaGroundBounds.height);

        // Plataformas suspensas com verticalidade
        for (Platform platform : platforms) {
            platform.render(batch);
        }

        // Obstáculos letais (espinhos)
        for (Hazard hazard : hazards) {
            hazard.render(batch);
        }

        // Power-ups
        for (PowerUp pu : powerUps) {
            pu.render(batch, hudFont, glyphLayout);
        }

        // Inimigos básicos (Minions vermelhos)
        for (Minion minion : minions) {
            minion.render(batch);
        }

        // Único Boss Final (BossC) e Jogador
        bossC.render(batch);
        player.render(batch);

        batch.end();

        // Renderização da HUD e menus flutuantes (projeção fixa)
        renderHUD();

        // Se pausado, renderiza o Menu de Pausa
        if (isPaused) {
            renderPauseMenu();
        }
    }

    /**
     * Atualizações de lógica de jogo (executadas apenas quando não pausado).
     */
    private void updateGame(float delta) {
        cheatManager.update(delta);

        // Efeito slow-motion do Claude sobre o boss
        float bossDelta = player.isClaudeSlowActive() ? delta * 0.40f : delta;

        // Atualização do jogador
        player.update(delta);

        // Atualização do Boss Final apenas quando o jogador se aproxima da arena ou do Boss
        bossC.update(bossDelta, player, bossArenaGroundBounds);

        // Atualização dos Minions
        for (Minion minion : minions) {
            minion.update(delta);
        }

        // Atualização dos Power-ups
        updatePowerUps(delta);

        // Colisões do jogador com chão e plataformas suspensas
        player.checkPlatformCollision(startGroundBounds);
        player.checkPlatformCollision(bossArenaGroundBounds);
        for (Platform platform : platforms) {
            player.checkPlatformCollision(platform.getBounds());
        }

        // Colisões com espinhos letais (Hazards)
        for (Hazard hazard : hazards) {
            if (Intersector.overlaps(player.getBounds(), hazard.getBounds())) {
                if (!player.isInvulnerable()) {
                    player.takeDamage(hazard.getDamage());
                    player.applyKnockback(-240f, 520f); // Impulso para tentar o salto novamente
                }
            }
        }

        // Combate com Minions
        for (Minion minion : minions) {
            if (minion.isDead()) continue;

            // Ataque do jogador no minion
            if (player.isAttacking() && Intersector.overlaps(player.getAttackBounds(), minion.getBounds())) {
                minion.takeDamage(player.getAttackDamage());
            }

            // Minion encosta no jogador
            if (Intersector.overlaps(player.getBounds(), minion.getBounds())) {
                if (!player.isInvulnerable()) {
                    player.takeDamage(15);
                    float knockDir = (minion.getPosition().x < player.getPosition().x) ? 1f : -1f;
                    player.applyKnockback(knockDir * 300f, 250f);
                }
            }
        }

        // Combate com o Boss Final
        combatSystem.update(delta, player, bossC);

        // Restrição de limites da fase
        keepEntitiesInBounds();

        // Rastreamento de câmera horizontal e vertical suave
        updateCameraFollow();
    }

    private void updatePowerUps(float delta) {
        for (int i = powerUps.size - 1; i >= 0; i--) {
            PowerUp p = powerUps.get(i);
            p.update(delta);

            if (!p.isCollected() && Intersector.overlaps(player.getBounds(), p.getBounds())) {
                p.apply(player);
                cheatManager.showNotification("POWER-UP ATIVADO: " + p.getType().getDisplayName());
                p.dispose();
                powerUps.removeIndex(i);
            }
        }
    }

    private void keepEntitiesInBounds() {
        // Jogador
        if (player.getPosition().x < 0f) {
            player.getPosition().x = 0f;
            player.getVelocity().x = 0f;
            player.getBounds().setPosition(player.getPosition().x, player.getPosition().y);
        } else if (player.getPosition().x + player.getWidth() > WORLD_WIDTH) {
            player.getPosition().x = WORLD_WIDTH - player.getWidth();
            player.getVelocity().x = 0f;
            player.getBounds().setPosition(player.getPosition().x, player.getPosition().y);
        }

        // Se cair totalmente fora do mapa
        if (player.getPosition().y < -50f) {
            player.takeDamage(9999);
        }

        // Boss Final dentro da arena
        if (bossC.getPosition().x < bossArenaGroundBounds.x) {
            bossC.getPosition().x = bossArenaGroundBounds.x;
            bossC.getVelocity().x = 0f;
            bossC.getBounds().setPosition(bossC.getPosition().x, bossC.getPosition().y);
        } else if (bossC.getPosition().x + bossC.getWidth() > WORLD_WIDTH) {
            bossC.getPosition().x = WORLD_WIDTH - bossC.getWidth();
            bossC.getVelocity().x = 0f;
            bossC.getBounds().setPosition(bossC.getPosition().x, bossC.getPosition().y);
        }
    }

    /**
     * Câmera suave acompanhando o jogador tanto em X quanto em Y para valorizar a verticalidade.
     */
    private void updateCameraFollow() {
        float halfW = VIRTUAL_WIDTH / 2f;
        float targetCameraX = MathUtils.clamp(
                player.getPosition().x + (player.getWidth() / 2f),
                halfW,
                WORLD_WIDTH - halfW
        );

        float halfH = VIRTUAL_HEIGHT / 2f;
        float targetCameraY = MathUtils.clamp(
                player.getPosition().y + (player.getHeight() / 2f) + 40f,
                halfH,
                WORLD_HEIGHT - halfH
        );

        camera.position.x = MathUtils.lerp(camera.position.x, targetCameraX, 0.10f);
        camera.position.y = MathUtils.lerp(camera.position.y, targetCameraY, 0.08f);
        camera.update();
    }

    /**
     * Renderiza a HUD principal.
     */
    private void renderHUD() {
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        shapeRenderer.begin(ShapeType.Filled);

        // 1. Barra de Vida do Jogador (Vermelha)
        float playerBarX = 35f;
        float playerBarY = 650f;
        float playerBarW = 280f;
        float playerBarH = 22f;

        shapeRenderer.setColor(new Color(0.15f, 0.15f, 0.18f, 1f));
        shapeRenderer.rect(playerBarX, playerBarY, playerBarW, playerBarH);

        float playerHpRatio = Math.max(0f, (float) player.getCurrentHp() / (float) player.getMaxHp());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(playerBarX, playerBarY, playerBarW * playerHpRatio, playerBarH);

        // 2. Barra de Vida do Boss (Roxa) - visível na arena
        float bossBarW = 480f;
        float bossBarH = 26f;
        float bossBarX = (VIRTUAL_WIDTH - bossBarW) / 2f;
        float bossBarY = 648f;

        shapeRenderer.setColor(new Color(0.15f, 0.15f, 0.18f, 1f));
        shapeRenderer.rect(bossBarX, bossBarY, bossBarW, bossBarH);

        float bossHpRatio = Math.max(0f, (float) bossC.getCurrentHp() / (float) bossC.getMaxHp());
        shapeRenderer.setColor(Color.PURPLE);
        shapeRenderer.rect(bossBarX, bossBarY, bossBarW * bossHpRatio, bossBarH);

        shapeRenderer.end();

        // Molduras
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(playerBarX, playerBarY, playerBarW, playerBarH);
        shapeRenderer.rect(bossBarX, bossBarY, bossBarW, bossBarH);
        shapeRenderer.end();

        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Textos das barras
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "JOGADOR  [HP: " + player.getCurrentHp() + " / " + player.getMaxHp() + "]", playerBarX, playerBarY + 40f);

        hudFont.setColor(Color.PURPLE);
        glyphLayout.setText(hudFont, bossC.getName() + "  [HP: " + bossC.getCurrentHp() + " / " + bossC.getMaxHp() + "]");
        float bossTextX = bossBarX + (bossBarW - glyphLayout.width) / 2f;
        hudFont.draw(batch, glyphLayout, bossTextX, bossBarY + 44f);

        // Indicadores de Buffs de IA
        float buffY = playerBarY - 14f;

        if (player.isGodMode()) {
            smallFont.setColor(Color.GOLD);
            smallFont.draw(batch, "[MODO DEUS ATIVO]", playerBarX, buffY);
            buffY -= 18f;
        }
        if (player.isGeminiShieldActive()) {
            smallFont.setColor(new Color(0.2f, 0.7f, 1f, 1f));
            smallFont.draw(batch, String.format("ESCUDO GEMINI: %.1fs", player.getGeminiShieldTimer()), playerBarX, buffY);
            buffY -= 18f;
        }
        if (player.isClaudeSlowActive()) {
            smallFont.setColor(new Color(0.95f, 0.6f, 0.2f, 1f));
            smallFont.draw(batch, String.format("SLOW-MOTION CLAUDE: %.1fs", player.getClaudeSlowTimer()), playerBarX, buffY);
            buffY -= 18f;
        }
        if (player.isChatGptBuffActive()) {
            smallFont.setColor(new Color(0.2f, 0.9f, 0.5f, 1f));
            smallFont.draw(batch, String.format("OVERCLOCK CHATGPT: %.1fs (DANO 2x)", player.getChatGptBuffTimer()), playerBarX, buffY);
            buffY -= 18f;
        }

        // DISPLAY DE CHEATS (OCULTO POR PADRÃO, visível apenas se ativado pelo botão secreto)
        if (CheatManager.isCheatsDisplayEnabled()) {
            smallFont.setColor(new Color(0.3f, 1.0f, 0.4f, 0.95f));
            smallFont.draw(batch, "CHEATS [ATIVADOS]: [F1] Modo Deus  |  [F2] Instakill  |  [F3] Spawn Power-Up  |  [F4] Forçar Vitória", 35f, 30f);

            if (cheatManager.hasActiveNotification()) {
                hudFont.setColor(Color.YELLOW);
                drawCenteredMessage(batch, cheatManager.getLastNotification(), 580f);
            }
        }

        // TELA DE VITÓRIA (Mensagem exata: "parabéns! ✌️ ✌️")
        if (bossC.isDead()) {
            titleFont.setColor(Color.GREEN);
            glyphLayout.setText(titleFont, "parabéns! ✌️ ✌️");
            float winTextX = (VIRTUAL_WIDTH - glyphLayout.width) / 2f;
            titleFont.draw(batch, glyphLayout, winTextX, 440f);

            hudFont.setColor(Color.WHITE);
            drawCenteredMessage(batch, "Pressione [ESC] para o Menu de Pausa  |  [R] para Reiniciar", 370f);
        } else if (player.isDead()) {
            titleFont.setColor(Color.RED);
            glyphLayout.setText(titleFont, "VOCÊ FOI DERROTADO!");
            float loseTextX = (VIRTUAL_WIDTH - glyphLayout.width) / 2f;
            titleFont.draw(batch, glyphLayout, loseTextX, 440f);

            hudFont.setColor(Color.WHITE);
            drawCenteredMessage(batch, "Pressione [R] para tentar novamente  |  [ESC] para o Menu de Pausa", 370f);
        }

        batch.end();
    }

    /**
     * Renderiza o Menu de Pausa flutuante acionado pela tecla ESC.
     */
    private void renderPauseMenu() {
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Escurece o fundo
        batch.draw(modalOverlayTexture, 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Painel flutuante
        batch.draw(modalPanelTexture, pauseModalBounds.x, pauseModalBounds.y, pauseModalBounds.width, pauseModalBounds.height);

        // Título "PAUSA"
        titleFont.setColor(Color.WHITE);
        glyphLayout.setText(titleFont, "PAUSA");
        float titleX = pauseModalBounds.x + (pauseModalBounds.width - glyphLayout.width) / 2f;
        titleFont.draw(batch, glyphLayout, titleX, pauseModalBounds.y + pauseModalBounds.height - 35f);

        // Botão 1: "Recomeçar"
        Texture texRestart = (hoveredPauseButton == 1) ? btnHoverTexture : btnTexture;
        batch.draw(texRestart, btnPauseRestartBounds.x, btnPauseRestartBounds.y, btnPauseRestartBounds.width, btnPauseRestartBounds.height);
        hudFont.setColor(Color.WHITE);
        glyphLayout.setText(hudFont, "Recomeçar");
        float rTextX = btnPauseRestartBounds.x + (btnPauseRestartBounds.width - glyphLayout.width) / 2f;
        float rTextY = btnPauseRestartBounds.y + (btnPauseRestartBounds.height + glyphLayout.height) / 2f;
        hudFont.draw(batch, glyphLayout, rTextX, rTextY);

        // Botão 2: "Sair"
        Texture texExit = (hoveredPauseButton == 2) ? btnHoverTexture : btnTexture;
        batch.draw(texExit, btnPauseExitBounds.x, btnPauseExitBounds.y, btnPauseExitBounds.width, btnPauseExitBounds.height);
        hudFont.setColor(Color.WHITE);
        glyphLayout.setText(hudFont, "Sair");
        float eTextX = btnPauseExitBounds.x + (btnPauseExitBounds.width - glyphLayout.width) / 2f;
        float eTextY = btnPauseExitBounds.y + (btnPauseExitBounds.height + glyphLayout.height) / 2f;
        hudFont.draw(batch, glyphLayout, eTextX, eTextY);

        batch.end();
    }

    private void drawCenteredMessage(SpriteBatch batch, String message, float y) {
        glyphLayout.setText(hudFont, message);
        float x = (VIRTUAL_WIDTH - glyphLayout.width) / 2f;
        hudFont.draw(batch, glyphLayout, x, y);
    }

    @Override
    public void onEscapePressed() {
        // Alterna o estado de pausa do jogo
        this.isPaused = !this.isPaused;
    }

    @Override
    public void onRestartPressed() {
        initBattle();
    }

    @Override
    public boolean onCheatKeyPressed(int keycode) {
        return cheatManager.handleCheatKey(keycode, player, bossC, this);
    }

    @Override
    public boolean onScreenClicked(int screenX, int screenY) {
        if (!isPaused) {
            return false;
        }

        // Converte coordenadas do mouse para o espaço virtual da HUD
        hudCamera.unproject(mouseCoords.set(screenX, screenY, 0));

        // Botão "Recomeçar"
        if (btnPauseRestartBounds.contains(mouseCoords.x, mouseCoords.y)) {
            initBattle();
            return true;
        }

        // Botão "Sair" -> volta ao menu principal
        if (btnPauseExitBounds.contains(mouseCoords.x, mouseCoords.y)) {
            game.setScreen(new MainMenuScreen(game));
            return true;
        }

        return false;
    }

    @Override
    public void onScreenMouseMoved(int screenX, int screenY) {
        if (!isPaused) {
            hoveredPauseButton = 0;
            return;
        }

        hudCamera.unproject(mouseCoords.set(screenX, screenY, 0));

        if (btnPauseRestartBounds.contains(mouseCoords.x, mouseCoords.y)) {
            hoveredPauseButton = 1;
        } else if (btnPauseExitBounds.contains(mouseCoords.x, mouseCoords.y)) {
            hoveredPauseButton = 2;
        } else {
            hoveredPauseButton = 0;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        this.isPaused = true;
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
        parallaxBackground.dispose();
        groundTexture.dispose();
        player.dispose();
        bossC.dispose();

        for (Platform p : platforms) p.dispose();
        for (Hazard h : hazards) h.dispose();
        for (Minion m : minions) m.dispose();
        for (PowerUp pu : powerUps) pu.dispose();

        shapeRenderer.dispose();
        hudFont.dispose();
        titleFont.dispose();
        smallFont.dispose();
        modalOverlayTexture.dispose();
        modalPanelTexture.dispose();
        btnTexture.dispose();
        btnHoverTexture.dispose();
    }
}
