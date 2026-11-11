package io.github.AOS.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Entidade Jogador para o estilo plataforma 2D.
 * Gerencia física, combate, pulo, ataques com cooldowns individuais, saúde, Modo Deus e buffs de IA.
 */
public class Player {

    // Constantes físicas e dimensionais
    public static final float WIDTH = 64f;
    public static final float HEIGHT = 64f;
    private static final float BASE_MOVE_SPEED = 320f;
    private static final float GRAVITY = -1400f;
    private static final float JUMP_VELOCITY = 620f;

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

    // Estados
    private boolean isGrounded;
    private boolean facingRight = true;
    private boolean moveLeft;
    private boolean moveRight;

    // Sistema de ataques (1: J, 2: K, 3: L)
    private int currentAttack = 0;
    private float attackTimer = 0f;

    // Recursos gráficos (Placeholders via Pixmap)
    private final Texture playerTexture;
    private final Texture attackTexture;
    private final Texture shieldTexture;
    private final Texture auraTexture;

    public Player(float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0f, 0f);
        this.bounds = new Rectangle(startX, startY, WIDTH, HEIGHT);
        this.attackBounds = new Rectangle(0f, 0f, 0f, 0f);

        this.isGrounded = false;

        // Textura do jogador (Verde nativo do AGENTS.md)
        this.playerTexture = PlaceholderFactory.createPlayerTexture();
        // Textura de ataque
        this.attackTexture = PlaceholderFactory.createColoredTexture(new Color(1f, 1f, 0.4f, 0.8f));
        // Textura de escudo protetor (Gemini)
        this.shieldTexture = PlaceholderFactory.createColoredTexture(new Color(0.2f, 0.65f, 1f, 0.5f));
        // Textura de aura de overclock (ChatGPT)
        this.auraTexture = PlaceholderFactory.createColoredTexture(new Color(0.1f, 0.85f, 0.5f, 0.45f));
    }

    /**
     * Atualiza a física, movimentação, cooldowns de ataques e temporizadores de buffs a cada quadro.
     */
    public void update(float delta) {
        // Atualização dos cooldowns individuais de cada golpe
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

        // Atualização de temporizadores de buffs de IA
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

        // Atualiza invulnerabilidade pós-dano normal
        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer < 0f) invulnerabilityTimer = 0f;
        }

        handleMovementInput();

        // Gravidade
        velocity.y += GRAVITY * delta;
        if (velocity.y < -900f) {
            velocity.y = -900f;
        }

        // Atualização de posição
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        bounds.setPosition(position.x, position.y);

        // Atualização de ataque ativo
        if (attackTimer > 0f) {
            attackTimer -= delta;
            updateAttackHitbox();
            if (attackTimer <= 0f) {
                currentAttack = 0;
                attackBounds.set(0f, 0f, 0f, 0f);
            }
        }
    }

    /**
     * Processa movimentação contínua, aplicando aumento de velocidade se buff ChatGPT estiver ativo.
     */
    private void handleMovementInput() {
        boolean left = moveLeft || Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT);
        boolean right = moveRight || Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT);

        float speed = (chatGptBuffTimer > 0f) ? BASE_MOVE_SPEED * 1.45f : BASE_MOVE_SPEED;

        if (left && !right) {
            velocity.x = -speed;
            facingRight = false;
        } else if (right && !left) {
            velocity.x = speed;
            facingRight = true;
        } else {
            velocity.x = 0f;
        }
    }

    public void jump() {
        if (isGrounded) {
            velocity.y = JUMP_VELOCITY;
            isGrounded = false;
        }
    }

    /**
     * Inicia um dos ataques se não estiver em cooldown ou executando outro golpe (impede spam).
     */
    public boolean attack(int attackType) {
        if (attackTimer > 0f) {
            return false; // Já está desferindo um golpe
        }

        switch (attackType) {
            case 1:
                if (cooldownTimer1 > 0f) return false; // Em cooldown
                cooldownTimer1 = COOLDOWN_ATTACK_1;
                attackTimer = 0.16f;
                this.currentAttack = 1;
                break;

            case 2:
                if (cooldownTimer2 > 0f) return false; // Em cooldown
                cooldownTimer2 = COOLDOWN_ATTACK_2;
                attackTimer = 0.26f;
                this.currentAttack = 2;
                break;

            case 3:
                if (cooldownTimer3 > 0f) return false; // Em cooldown (máx 2.5s)
                cooldownTimer3 = COOLDOWN_ATTACK_3;
                attackTimer = 0.38f;
                this.currentAttack = 3;
                break;

            default:
                return false;
        }

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
                reach = 42f;
                attackHeight = 28f;
                break;
            case 2:
                reach = 65f;
                attackHeight = 42f;
                break;
            case 3:
                reach = 95f;
                attackHeight = 60f;
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
        if (bounds.overlaps(platform)) {
            if (velocity.y <= 0f && position.y + (HEIGHT * 0.5f) > platform.y + platform.height) {
                position.y = platform.y + platform.height;
                velocity.y = 0f;
                isGrounded = true;
                bounds.setPosition(position.x, position.y);
            }
        }
    }

    /**
     * Renderiza o jogador, efeitos de ataque e auras visuais de buffs e cheats.
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
            float auraPad = 8f;
            batch.draw(auraTexture, position.x - auraPad, position.y - auraPad, WIDTH + (auraPad * 2), HEIGHT + (auraPad * 2));
        }

        // Corpo do jogador (verde nativo)
        batch.draw(playerTexture, position.x, position.y, WIDTH, HEIGHT);

        // Escudo protetor do Gemini ou God Mode
        if (geminiShieldTimer > 0f || godMode) {
            float shieldPad = 10f;
            batch.draw(shieldTexture, position.x - shieldPad, position.y - shieldPad, WIDTH + (shieldPad * 2), HEIGHT + (shieldPad * 2));
        }

        // Projeção visual de ataque ativo
        if (attackTimer > 0f && currentAttack > 0) {
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
        playerTexture.dispose();
        attackTexture.dispose();
        shieldTexture.dispose();
        auraTexture.dispose();
    }
}
