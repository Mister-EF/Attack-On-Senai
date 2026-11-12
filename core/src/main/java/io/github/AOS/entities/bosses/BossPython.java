package io.github.AOS.entities.bosses;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.github.AOS.entities.BossProjectile;
import io.github.AOS.entities.Player;
import io.github.AOS.utils.AnimationLoader;
import io.github.AOS.utils.AnimationLoader.SpriteAnimation;
import io.github.AOS.utils.AssetLoader;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Chefe Final: Boss Python ("A Serpente da Indentação").
 * Suporta animações reais de spritesheet (animated, attack, hurt) e idle via PNG,
 * com fallback transparente para o placeholder Roxo (Color.PURPLE) conforme o AGENTS.md.
 * Padrões de ataque dinâmicos: investida rápida e disparos de projéteis amarelos
 * em leque (spread fan) e rajada em linha (burst).
 */
public class BossPython extends Boss {

    public enum State {
        IDLE,        // Fica parado observando o jogador e disparando rajadas (2.0s)
        PREPARING,   // Preparação visual rápida antes do arranque (0.4s)
        DASHING,     // Investida veloz horizontal (0.75s)
        RECOVERING   // Breve pausa para recuperação pós-investida (0.6s)
    }

    private static final float BOSS_WIDTH = 96f;
    private static final float BOSS_HEIGHT = 96f;
    private static final int INITIAL_HP = 220;

    // Dimensões de renderização do Sprite (proporção 256x144)
    private static final float SPRITE_RENDER_HEIGHT = 132f;
    private static final float SPRITE_RENDER_WIDTH = SPRITE_RENDER_HEIGHT * (256f / 144f); // ~234.6f

    private static final float GRAVITY = -1400f;
    private static final float BASE_DASH_SPEED = 650f;
    private static final float BASE_SHOOT_INTERVAL = 2.0f;
    private static final float BASE_IDLE_DURATION = 2.0f;
    private static final float BASE_PREPARE_DURATION = 0.4f;
    private static final float BASE_DASH_DURATION = 0.75f;
    private static final float BASE_RECOVER_DURATION = 0.6f;

    // Multiplicador de dificuldade: 0.5x=Fácil, 1.0x=Médio, 1.5x=Difícil
    private final float speedMult;

    private State currentState = State.IDLE;
    private float stateTimer;
    private float animationTime = 0f;
    private float dashDirection = -1f;

    // Projéteis periódicos em leque e rajada
    private final Array<BossProjectile> projectiles;
    private float shootTimer;
    private boolean useFanPattern = true; // Alterna entre disparo em leque e disparo em rajada

    // Controle de rajada sequencial em linha (burst)
    private int burstShotsRemaining = 0;
    private float burstCooldown = 0f;
    private float burstTargetX = 0f;
    private float burstTargetY = 0f;

    // Animações reais carregadas dos arquivos de assets/sprites/Python/
    private final SpriteAnimation moveAnim;
    private final SpriteAnimation attackAnim;
    private final SpriteAnimation hurtAnim;

    // Textura isolada idle ("python.png")
    private final Texture idleTexture;

    // Recursos gráficos de telegrafo e fallback
    private final Texture prepTexture;
    private final Texture fallbackTexture;

    public BossPython(float startX, float startY) {
        this(startX, startY, 1.0f);
    }

    /**
     * Construtor com multiplicador de dificuldade.
     * @param speedMult 0.5=Fácil, 1.0=Médio, 1.5=Difícil
     */
    public BossPython(float startX, float startY, float speedMult) {
        super("A Serpente da Indentação [Python]", startX, startY, BOSS_WIDTH, BOSS_HEIGHT, INITIAL_HP);
        this.speedMult = speedMult;
        this.stateTimer = BASE_IDLE_DURATION / speedMult;
        this.shootTimer = (BASE_SHOOT_INTERVAL / speedMult) * 0.5f; // Primeiro disparo na metade do intervalo

        // 1. Carrega as animações do Boss Python via JSON/PNG
        this.moveAnim = AnimationLoader.loadAnimation(
                "sprites/Python/python-animated-spritesheet.json",
                "sprites/Python/python-animated-spritesheet.png",
                Animation.PlayMode.LOOP
        );
        this.attackAnim = AnimationLoader.loadAnimation(
                "sprites/Python/python-attack-spritesheet.json",
                "sprites/Python/python-attack-spritesheet.png",
                Animation.PlayMode.NORMAL
        );
        this.hurtAnim = AnimationLoader.loadAnimation(
                "sprites/Python/python-hurt-spritesheet.json",
                "sprites/Python/python-hurt-spritesheet.png",
                Animation.PlayMode.NORMAL
        );

        // 2. Carrega a textura isolada idle
        this.idleTexture = AssetLoader.loadTextureWithFallback(
                "sprites/Python/python.png",
                PlaceholderFactory::createBossTexture
        );

        // 3. Textura avermelhada de telegrafo e fallback roxo nativo (AGENTS.md)
        this.prepTexture = PlaceholderFactory.createColoredTexture(new Color(0.9f, 0.2f, 0.2f, 1f));
        this.fallbackTexture = PlaceholderFactory.createBossTexture();

        this.projectiles = new Array<>();
    }

    @Override
    protected void updateBoss(float delta, Player player, Rectangle groundBounds) {
        animationTime += delta;

        // Gravidade física básica
        velocity.y += GRAVITY * delta;
        if (velocity.y < -900f) {
            velocity.y = -900f;
        }

        // Máquina de Estados da investida (durations escalonadas pelo multiplicador de dificuldade)
        stateTimer -= delta;

        switch (currentState) {
            case IDLE:
                velocity.x = 0f;
                facingRight = (player.getPosition().x > position.x);

                if (stateTimer <= 0f) {
                    currentState = State.PREPARING;
                    stateTimer = BASE_PREPARE_DURATION / speedMult;
                    dashDirection = (player.getPosition().x >= position.x) ? 1f : -1f;
                    facingRight = (dashDirection > 0f);
                }
                break;

            case PREPARING:
                velocity.x = 0f;
                if (stateTimer <= 0f) {
                    currentState = State.DASHING;
                    stateTimer = BASE_DASH_DURATION / speedMult;
                    velocity.x = dashDirection * BASE_DASH_SPEED * speedMult;
                }
                break;

            case DASHING:
                if (stateTimer <= 0f) {
                    currentState = State.RECOVERING;
                    stateTimer = BASE_RECOVER_DURATION / speedMult;
                    velocity.x = 0f;
                }
                break;

            case RECOVERING:
                velocity.x = 0f;
                facingRight = (player.getPosition().x > position.x);
                if (stateTimer <= 0f) {
                    currentState = State.IDLE;
                    stateTimer = BASE_IDLE_DURATION / speedMult;
                }
                break;
        }

        // Atualização da física
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Colisão com o solo da arena
        if (bounds.overlaps(groundBounds)) {
            position.y = groundBounds.y + groundBounds.height;
            velocity.y = 0f;
        }

        bounds.setPosition(position.x, position.y);

        // Atualização e disparo de projéteis periódicos
        updateProjectiles(delta, player);
    }

    private void updateProjectiles(float delta, Player player) {
        // Atualiza os projéteis existentes e remove inativos
        for (int i = projectiles.size - 1; i >= 0; i--) {
            BossProjectile p = projectiles.get(i);
            p.update(delta);
            if (!p.isActive()) {
                p.dispose();
                projectiles.removeIndex(i);
            }
        }

        if (isDead()) {
            return;
        }

        // Processa rajada sequencial em linha (burst) caso ativa
        if (burstShotsRemaining > 0) {
            burstCooldown -= delta;
            if (burstCooldown <= 0f) {
                float startX = position.x + (width / 2f);
                float startY = position.y + (height / 2f);
                projectiles.add(new BossProjectile(startX, startY, burstTargetX, burstTargetY, 0f));
                burstShotsRemaining--;
                burstCooldown = 0.18f / speedMult; // Mais rápido em dificuldades maiores
            }
            return;
        }

        // Se estiver em IDLE ou RECOVERING, acumula para o próximo disparo
        if (currentState == State.IDLE || currentState == State.RECOVERING) {
            shootTimer -= delta;
            if (shootTimer <= 0f) {
                shootTimer = BASE_SHOOT_INTERVAL / speedMult;

                float startX = position.x + (width / 2f);
                float startY = position.y + (height / 2f);
                float targetX = player.getPosition().x + (player.getWidth() / 2f);
                float targetY = player.getPosition().y + (player.getHeight() / 2f);

                if (useFanPattern) {
                    // PADRÃO 1: Leque de 3 projéteis amarelos (direto, +16° e -16°)
                    projectiles.add(new BossProjectile(startX, startY, targetX, targetY, 0f));
                    projectiles.add(new BossProjectile(startX, startY, targetX, targetY, 16f));
                    projectiles.add(new BossProjectile(startX, startY, targetX, targetY, -16f));
                } else {
                    // PADRÃO 2: Rajada de 3 tiros rápidos sequenciais em linha
                    burstTargetX = targetX;
                    burstTargetY = targetY;
                    burstShotsRemaining = 3;
                    burstCooldown = 0f;
                }

                // Alterna o padrão para o próximo ciclo
                useFanPattern = !useFanPattern;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (isDead()) {
            return;
        }

        // 1. Renderiza os projéteis amarelos
        for (BossProjectile p : projectiles) {
            p.render(batch);
        }

        // 2. Determinação do quadro de animação atual
        TextureRegion currentFrame = null;

        if (invulnerabilityTimer > 0f && hurtAnim != null) {
            currentFrame = hurtAnim.getKeyFrame(animationTime, false);
        } else if ((currentState == State.PREPARING || currentState == State.DASHING) && attackAnim != null) {
            currentFrame = attackAnim.getKeyFrame(animationTime, true);
        } else if (moveAnim != null) {
            currentFrame = moveAnim.getKeyFrame(animationTime, true);
        }

        if (currentFrame != null) {
            float renderWidth = SPRITE_RENDER_WIDTH;
            float renderHeight = SPRITE_RENDER_HEIGHT;
            float renderX = position.x + (width / 2f) - (renderWidth / 2f);
            float renderY = position.y;

            if (invulnerabilityTimer > 0.12f) {
                // Flash de dano
                batch.draw(hitFlashTexture, position.x, position.y, width, height);
            } else if (facingRight) {
                batch.draw(currentFrame, renderX, renderY, renderWidth, renderHeight);
            } else {
                batch.draw(currentFrame, renderX + renderWidth, renderY, -renderWidth, renderHeight);
            }
        } else if (idleTexture != null) {
            float renderWidth = SPRITE_RENDER_WIDTH;
            float renderHeight = SPRITE_RENDER_HEIGHT;
            float renderX = position.x + (width / 2f) - (renderWidth / 2f);
            float renderY = position.y;

            if (invulnerabilityTimer > 0.12f) {
                batch.draw(hitFlashTexture, position.x, position.y, width, height);
            } else if (currentState == State.PREPARING) {
                batch.draw(prepTexture, position.x, position.y, width, height);
            } else if (facingRight) {
                batch.draw(idleTexture, renderX, renderY, renderWidth, renderHeight);
            } else {
                batch.draw(idleTexture, renderX + renderWidth, renderY, -renderWidth, renderHeight);
            }
        } else {
            // Fallback nativo: Roxo via Pixmap (AGENTS.md)
            if (invulnerabilityTimer > 0.12f) {
                batch.draw(hitFlashTexture, position.x, position.y, width, height);
            } else if (currentState == State.PREPARING) {
                batch.draw(prepTexture, position.x, position.y, width, height);
            } else {
                batch.draw(fallbackTexture, position.x, position.y, width, height);
            }
        }
    }

    public Array<BossProjectile> getProjectiles() {
        return projectiles;
    }

    public boolean isDashing() {
        return currentState == State.DASHING;
    }

    public State getCurrentState() {
        return currentState;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (moveAnim != null) moveAnim.dispose();
        if (attackAnim != null) attackAnim.dispose();
        if (hurtAnim != null) hurtAnim.dispose();

        if (idleTexture != null) idleTexture.dispose();
        prepTexture.dispose();
        fallbackTexture.dispose();

        for (BossProjectile p : projectiles) {
            p.dispose();
        }
        projectiles.clear();
    }
}
