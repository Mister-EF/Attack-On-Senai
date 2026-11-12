package io.github.AOS.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.AOS.utils.AnimationLoader;
import io.github.AOS.utils.AnimationLoader.SpriteAnimation;
import io.github.AOS.utils.AssetLoader;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Entidade Jogador para o estilo plataforma 2D.
 * Gerencia física, combate, pulo, ataques com cooldowns individuais, saúde, Modo Deus,
 * buffs de IA e suporte a animações reais via JSON/PNG de spritesheets com fallback automático
 * para os placeholders do AGENTS.md.
 */
public class Player {

    // Constantes físicas e dimensionais (Hitbox balanceado para a proporção dos sprites)
    public static final float WIDTH = 56f;
    public static final float HEIGHT = 68f;
    private static final float BASE_MOVE_SPEED = 320f;
    private static final float GRAVITY = -1400f;
    private static final float JUMP_VELOCITY = 620f;

    // Dimensões de renderização do Sprite (mantendo proporção 256x144)
    private static final float SPRITE_RENDER_HEIGHT = 96f;
    private static final float SPRITE_RENDER_WIDTH = SPRITE_RENDER_HEIGHT * (256f / 144f); // ~170.66f

    // Intervalos e restrições de cooldowns dos ataques (em segundos)
    public static final float COOLDOWN_ATTACK_1 = 0.35f; // Ataque 1: Rápido (350ms)
    public static final float COOLDOWN_ATTACK_2 = 0.80f; // Ataque 2: Médio (800ms)
    public static final float COOLDOWN_ATTACK_3 = 2.20f; // Ataque 3: Pesado (máx 2.5s)

    private float cooldownTimer1 = 0f;
    private float cooldownTimer2 = 0f;
    private float cooldownTimer3 = 0f;

    // Sistema de Vida e Combate
    private static final int MAX_HP = 100;
    private int currentHp = MAX_HP;
    private float invulnerabilityTimer = 0f;

    // Sistema de Cheats & Power-ups
    private boolean godMode = false;
    private float geminiShieldTimer = 0f;
    private float claudeSlowTimer = 0f;
    private float chatGptBuffTimer = 0f;

    // Posição, velocidade e colisão
    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle bounds;
    private final Rectangle attackBounds;
    private float previousY;

    // Estados de movimentação
    private boolean isGrounded = true;
    private boolean facingRight = true;
    private boolean moveLeft;
    private boolean moveRight;

    // Sistema de ataques (1: J, 2: K, 3: L)
    private int currentAttack = 0;
    private float attackTimer = 0f;
    private float attackStateTime = 0f;

    // Controle de tempo para as animações
    private float stateTime = 0f;

    // Animações reais carregadas dos arquivos spritesheets/JSON
    private final SpriteAnimation runAnim;
    private final SpriteAnimation jumpAnim;
    private final SpriteAnimation attack1Anim;
    private final SpriteAnimation attack2Anim;
    private final SpriteAnimation attack3Anim;

    // Textura isolada idle
    private final Texture idleTexture;

    // Recursos gráficos de fallback e efeitos visuais
    private final Texture playerFallbackTexture;
    private final Texture attackTexture;
    private final Texture shieldTexture;
    private final Texture auraTexture;

    public Player(float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0f, 0f);
        this.bounds = new Rectangle(startX, startY, WIDTH, HEIGHT);
        this.attackBounds = new Rectangle(0f, 0f, 0f, 0f);
        this.previousY = startY;
        this.isGrounded = true;

        // 1. Carrega as animações do Protagonista via JSON/PNG em assets/sprites/hero/
        this.runAnim = AnimationLoader.loadAnimation(
                "sprites/hero/hero-run-spritesheet.json",
                "sprites/hero/hero-run-spritesheet.png",
                Animation.PlayMode.LOOP
        );
        this.jumpAnim = AnimationLoader.loadAnimation(
                "sprites/hero/hero-jump-spritesheet.json",
                "sprites/hero/hero-jump-spritesheet.png",
                Animation.PlayMode.NORMAL
        );
        this.attack1Anim = AnimationLoader.loadAnimation(
                "sprites/hero/hero-attack-1-spritesheet.json",
                "sprites/hero/hero-attack-1-spritesheet.png",
                Animation.PlayMode.NORMAL
        );
        this.attack2Anim = AnimationLoader.loadAnimation(
                "sprites/hero/hero-attack-2-spritesheet.json",
                "sprites/hero/hero-attack-2-spritesheet.png",
                Animation.PlayMode.NORMAL
        );
        this.attack3Anim = AnimationLoader.loadAnimation(
                "sprites/hero/hero-attack-3-spritesheet.json",
                "sprites/hero/hero-attack-3-spritesheet.png",
                Animation.PlayMode.NORMAL
        );

        // 2. Carrega a textura idle isolada ("hero.png") com fallback seguro
        this.idleTexture = AssetLoader.loadTextureWithFallback(
                "sprites/hero/hero.png",
                PlaceholderFactory::createPlayerTexture
        );

        // 3. Fallbacks e efeitos adicionais (Escudo Gemini, Overclock ChatGPT, etc.)
        this.playerFallbackTexture = PlaceholderFactory.createPlayerTexture();
        this.attackTexture = AssetLoader.loadTextureWithFallback(
                "sprites/attack.png",
                () -> PlaceholderFactory.createColoredTexture(new Color(1f, 1f, 0.4f, 0.8f))
        );
        this.shieldTexture = AssetLoader.loadTextureWithFallback(
                "sprites/shield.png",
                () -> PlaceholderFactory.createColoredTexture(new Color(0.2f, 0.65f, 1f, 0.5f))
        );
        this.auraTexture = AssetLoader.loadTextureWithFallback(
                "sprites/aura.png",
                () -> PlaceholderFactory.createColoredTexture(new Color(0.1f, 0.85f, 0.5f, 0.45f))
        );
    }

    /**
     * Atualiza a física, movimentação, cooldowns de ataques e temporizadores de buffs a cada quadro.
     */
    public void update(float delta) {
        stateTime += delta;

        // Atualização dos temporizadores de buffs de IA
        if (geminiShieldTimer > 0f) {
            geminiShieldTimer -= delta;
            if (geminiShieldTimer < 0f) geminiShieldTimer = 0f;
        }

        if (claudeSlowTimer > 0f) {
            claudeSlowTimer -= delta;
            if (claudeSlowTimer < 0f) claudeSlowTimer = 0f;
        }

        if (chatGptBuffTimer > 0f) {
            chatGptBuffTimer -= delta;
            if (chatGptBuffTimer < 0f) chatGptBuffTimer = 0f;
        }

        // Atualização da invulnerabilidade pós-dano
        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer < 0f) invulnerabilityTimer = 0f;
        }

        // Atualização dos cooldowns individuais de ataque
        if (cooldownTimer1 > 0f) {
            cooldownTimer1 -= delta;
            if (cooldownTimer1 < 0f) cooldownTimer1 = 0f;
        }
        if (cooldownTimer2 > 0f) {
            cooldownTimer2 -= delta;
            if (cooldownTimer2 < 0f) cooldownTimer2 = 0f;
        }
        if (cooldownTimer3 > 0f) {
            cooldownTimer3 -= delta;
            if (cooldownTimer3 < 0f) cooldownTimer3 = 0f;
        }

        // Atualização da duração do ataque ativo
        if (attackTimer > 0f) {
            attackTimer -= delta;
            attackStateTime += delta;
            if (attackTimer <= 0f) {
                attackTimer = 0f;
                currentAttack = 0;
                attackBounds.set(0f, 0f, 0f, 0f);
            } else {
                updateAttackHitbox();
            }
        }

        // Movimentação horizontal com multiplicador de velocidade (ChatGPT buff)
        float currentSpeed = BASE_MOVE_SPEED;
        if (chatGptBuffTimer > 0f) {
            currentSpeed *= 1.4f; // +40% de velocidade de movimento
        }

        if (moveLeft && !moveRight) {
            velocity.x = -currentSpeed;
            facingRight = false;
        } else if (moveRight && !moveLeft) {
            velocity.x = currentSpeed;
            facingRight = true;
        } else {
            velocity.x = 0f;
        }

        // Guarda a posição Y anterior para detecção de colisão anti-tunelamento
        previousY = position.y;

        // Aplicação de gravidade simples
        velocity.y += GRAVITY * delta;
        if (velocity.y < -900f) {
            velocity.y = -900f; // Terminal velocity
        }

        // Integração de movimento
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Atualização da caixa de colisão física
        bounds.setPosition(position.x, position.y);

        // Se estiver caindo, não está no chão
        if (velocity.y < 0f) {
            isGrounded = false;
        }
    }

    /**
     * Executa o salto caso o jogador esteja em contato com o chão.
     */
    public void jump() {
        if (isGrounded) {
            velocity.y = JUMP_VELOCITY;
            isGrounded = false;
        }
    }

    public boolean attack(int attackIndex) {
        return performAttack(attackIndex);
    }

    /**
     * Dispara um ataque (1, 2 ou 3) caso o cooldown específico esteja liberado.
     * Retorna true se o golpe foi disparado com sucesso.
     */
    public boolean performAttack(int attackIndex) {
        if (attackTimer > 0f) {
            return false; // Não interrompe ataque ativo
        }

        switch (attackIndex) {
            case 1:
                if (cooldownTimer1 > 0f) return false;
                cooldownTimer1 = COOLDOWN_ATTACK_1;
                attackTimer = 0.25f;
                this.currentAttack = 1;
                break;

            case 2:
                if (cooldownTimer2 > 0f) return false;
                cooldownTimer2 = COOLDOWN_ATTACK_2;
                attackTimer = 0.40f;
                this.currentAttack = 2;
                break;

            case 3:
                if (cooldownTimer3 > 0f) return false;
                cooldownTimer3 = COOLDOWN_ATTACK_3;
                attackTimer = 0.65f;
                this.currentAttack = 3;
                break;

            default:
                return false;
        }

        this.attackStateTime = 0f;
        updateAttackHitbox();
        return true;
    }

    /**
     * Retorna o dano causado com base no ataque ativo e buffs.
     */
    public int getAttackDamage() {
        int baseDamage;
        switch (currentAttack) {
            case 1:
                baseDamage = 15;
                break;
            case 2:
                baseDamage = 28;
                break;
            case 3:
                baseDamage = 50;
                break;
            default:
                baseDamage = 0;
                break;
        }

        // Dobra o dano caso o buff de IA do ChatGPT esteja ativo
        return (chatGptBuffTimer > 0f) ? baseDamage * 2 : baseDamage;
    }

    /**
     * Aplica dano ao jogador. Bloqueado se God Mode ou Escudo Gemini estiverem ativos.
     */
    public boolean takeDamage(int amount) {
        if (godMode || geminiShieldTimer > 0f || invulnerabilityTimer > 0f || currentHp <= 0) {
            return false;
        }

        currentHp -= amount;
        if (currentHp < 0) {
            currentHp = 0;
        }

        invulnerabilityTimer = 1.0f;
        return true;
    }

    public void applyKnockback(float impulseX, float impulseY) {
        if (godMode || geminiShieldTimer > 0f) {
            return;
        }
        velocity.x = impulseX;
        velocity.y = impulseY;
        isGrounded = false;
    }

    /**
     * Atualiza a caixa de colisão do ataque com alcance e altura bem diferenciados por golpe.
     */
    private void updateAttackHitbox() {
        float reach;
        float attackHeight;

        switch (currentAttack) {
            case 1:
                reach = 48f;
                attackHeight = 36f;
                break;
            case 2:
                reach = 72f;
                attackHeight = 48f;
                break;
            case 3:
                reach = 100f;
                attackHeight = 65f;
                break;
            default:
                attackBounds.set(0f, 0f, 0f, 0f);
                return;
        }

        float attackX = facingRight ? position.x + WIDTH : position.x - reach;
        float attackY = position.y + (HEIGHT - attackHeight) / 2f;
        attackBounds.set(attackX, attackY, reach, attackHeight);
    }

    public void checkPlatformCollision(Rectangle platform) {
        // Verifica se o jogador está alinhado horizontalmente com a plataforma
        boolean horizontalOverlap = (position.x + WIDTH > platform.x + 2f) && (position.x < platform.x + platform.width - 2f);
        if (!horizontalOverlap) {
            return;
        }

        float platformTop = platform.y + platform.height;

        // Se o jogador estiver caindo ou parado verticalmente
        if (velocity.y <= 0f) {
            boolean wasAbove = (previousY >= platformTop - 12f);
            boolean isNowAtOrBelow = (position.y <= platformTop);
            boolean notTooDeep = (position.y >= platform.y - 15f);

            if (wasAbove && isNowAtOrBelow && notTooDeep) {
                position.y = platformTop;
                velocity.y = 0f;
                isGrounded = true;
                bounds.setPosition(position.x, position.y);
                return;
            }

            // Fallback de colisão padrão caso já esteja sobreposto
            if (bounds.overlaps(platform) && position.y + (HEIGHT * 0.25f) >= platformTop) {
                position.y = platformTop;
                velocity.y = 0f;
                isGrounded = true;
                bounds.setPosition(position.x, position.y);
            }
        }
    }

    /**
     * Renderiza o jogador com suas animações de spritesheet reais, espelhamento direcional,
     * efeitos de ataque e auras visuais de buffs e cheats.
     */
    public void render(SpriteBatch batch) {
        // Pisca ao receber dano se não estiver com escudo
        if (invulnerabilityTimer > 0f && geminiShieldTimer <= 0f && !godMode) {
            if (((int) (invulnerabilityTimer * 20f)) % 2 == 0) {
                return;
            }
        }

        // Aura de velocidade/overclock do ChatGPT
        if (chatGptBuffTimer > 0f) {
            float auraPad = 12f;
            batch.draw(auraTexture, position.x - auraPad, position.y - auraPad, WIDTH + (auraPad * 2), HEIGHT + (auraPad * 2));
        }

        // Determinação do quadro de animação atual
        TextureRegion currentFrame = null;

        if (isAttacking()) {
            if (currentAttack == 1 && attack1Anim != null) {
                currentFrame = attack1Anim.getKeyFrame(attackStateTime, false);
            } else if (currentAttack == 2 && attack2Anim != null) {
                currentFrame = attack2Anim.getKeyFrame(attackStateTime, false);
            } else if (currentAttack == 3 && attack3Anim != null) {
                currentFrame = attack3Anim.getKeyFrame(attackStateTime, false);
            }
        } else if (!isGrounded) {
            if (jumpAnim != null) {
                currentFrame = jumpAnim.getKeyFrame(stateTime, false);
            }
        } else if (Math.abs(velocity.x) > 10f || moveLeft || moveRight) {
            if (runAnim != null) {
                currentFrame = runAnim.getKeyFrame(stateTime, true);
            }
        }

        // Se uma animação ativa foi determinada, renderiza o frame com alinhamento nos pés
        if (currentFrame != null) {
            float renderWidth = SPRITE_RENDER_WIDTH;
            float renderHeight = SPRITE_RENDER_HEIGHT;
            // Centraliza o sprite horizontalmente sobre o hitbox do personagem e alinha a base (y)
            float renderX = position.x + (WIDTH / 2f) - (renderWidth / 2f);
            float renderY = position.y;

            if (facingRight) {
                batch.draw(currentFrame, renderX, renderY, renderWidth, renderHeight);
            } else {
                // Desenha espelhado horizontalmente
                batch.draw(currentFrame, renderX + renderWidth, renderY, -renderWidth, renderHeight);
            }
        } else if (idleTexture != null) {
            // Renderiza idle centralizado
            float renderWidth = SPRITE_RENDER_WIDTH;
            float renderHeight = SPRITE_RENDER_HEIGHT;
            float renderX = position.x + (WIDTH / 2f) - (renderWidth / 2f);
            float renderY = position.y;

            if (facingRight) {
                batch.draw(idleTexture, renderX, renderY, renderWidth, renderHeight);
            } else {
                batch.draw(idleTexture, renderX + renderWidth, renderY, -renderWidth, renderHeight);
            }
        } else {
            // Fallback nativo: Verde via Pixmap (AGENTS.md)
            batch.draw(playerFallbackTexture, position.x, position.y, WIDTH, HEIGHT);
        }

        // Escudo protetor do Gemini ou God Mode
        if (geminiShieldTimer > 0f || godMode) {
            float shieldPad = 12f;
            batch.draw(shieldTexture, position.x - shieldPad, position.y - shieldPad, WIDTH + (shieldPad * 2), HEIGHT + (shieldPad * 2));
        }

        // Projeção visual de ataque ativo (apenas como hitbox/efeito auxiliar se fallback)
        if (attackTimer > 0f && currentAttack > 0 && currentFrame == null) {
            batch.draw(attackTexture, attackBounds.x, attackBounds.y, attackBounds.width, attackBounds.height);
        }
    }

    // Cooldowns
    public float getCooldownTimer1() {
        return cooldownTimer1;
    }

    public float getCooldownTimer2() {
        return cooldownTimer2;
    }

    public float getCooldownTimer3() {
        return cooldownTimer3;
    }

    // Aplicação de buffs
    public void applyGeminiShield(float duration) {
        this.geminiShieldTimer = duration;
    }

    public void applyClaudeSlow(float duration) {
        this.claudeSlowTimer = duration;
    }

    public void applyChatGptBuff(float duration) {
        this.chatGptBuffTimer = duration;
    }

    public void toggleGodMode() {
        this.godMode = !this.godMode;
        if (godMode) {
            this.currentHp = MAX_HP;
        }
    }

    public boolean isGodMode() {
        return godMode;
    }

    public boolean isGeminiShieldActive() {
        return geminiShieldTimer > 0f;
    }

    public float getGeminiShieldTimer() {
        return geminiShieldTimer;
    }

    public boolean isClaudeSlowActive() {
        return claudeSlowTimer > 0f;
    }

    public float getClaudeSlowTimer() {
        return claudeSlowTimer;
    }

    public boolean isChatGptBuffActive() {
        return chatGptBuffTimer > 0f;
    }

    public float getChatGptBuffTimer() {
        return chatGptBuffTimer;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Rectangle getAttackBounds() {
        return attackBounds;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public void setGrounded(boolean grounded) {
        isGrounded = grounded;
    }

    public int getCurrentAttack() {
        return currentAttack;
    }

    public boolean isAttacking() {
        return attackTimer > 0f;
    }

    public int getMaxHp() {
        return MAX_HP;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public boolean isDead() {
        return currentHp <= 0;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0f || geminiShieldTimer > 0f || godMode;
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }

    public void dispose() {
        if (runAnim != null) runAnim.dispose();
        if (jumpAnim != null) jumpAnim.dispose();
        if (attack1Anim != null) attack1Anim.dispose();
        if (attack2Anim != null) attack2Anim.dispose();
        if (attack3Anim != null) attack3Anim.dispose();

        if (idleTexture != null) idleTexture.dispose();
        playerFallbackTexture.dispose();
        attackTexture.dispose();
        shieldTexture.dispose();
        auraTexture.dispose();
    }
}
