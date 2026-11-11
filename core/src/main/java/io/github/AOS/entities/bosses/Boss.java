package io.github.AOS.entities.bosses;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.AOS.entities.Player;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Classe base abstrata para todos os chefes (Bosses) do Boss Rush.
 * Gerencia ciclo de vida, atributos de vida, colisão e renderização placeholder roxa.
 */
public abstract class Boss {

    protected final String name;
    protected final Vector2 position;
    protected final Vector2 velocity;
    protected final float width;
    protected final float height;
    protected final Rectangle bounds;

    protected final int maxHp;
    protected int currentHp;
    protected float invulnerabilityTimer = 0f;

    protected boolean isGrounded;
    protected boolean facingRight = false;

    protected final Texture bossTexture;
    protected final Texture hitFlashTexture;

    public Boss(String name, float startX, float startY, float width, float height, int maxHp) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.maxHp = maxHp;
        this.currentHp = maxHp;

        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0f, 0f);
        this.bounds = new Rectangle(startX, startY, width, height);
        this.isGrounded = false;

        // Placeholder visual Roxo conforme o AGENTS.md
        this.bossTexture = PlaceholderFactory.createBossTexture();
        // Textura clara para feedback instantâneo de impacto
        this.hitFlashTexture = PlaceholderFactory.createColoredTexture(Color.WHITE);
    }

    /**
     * Atualização comum do chefe (física básica, contadores e chamada à IA do chefe).
     */
    public void update(float delta, Player player, Rectangle groundBounds) {
        if (isDead()) {
            return;
        }

        // Atualiza invulnerabilidade pós-hit
        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer < 0f) {
                invulnerabilityTimer = 0f;
            }
        }

        // Atualização da lógica de comportamento específica (IA e Máquina de Estados)
        updateBoss(delta, player, groundBounds);

        // Atualização da caixa de colisão
        bounds.setPosition(position.x, position.y);
    }

    /**
     * Lógica e Máquina de Estados específica implementada por cada chefe concreto.
     */
    protected abstract void updateBoss(float delta, Player player, Rectangle groundBounds);

    /**
     * Aplica dano ao chefe com tempo de invulnerabilidade rápida para evitar múltiplos hits por quadro.
     */
    public boolean takeDamage(int amount) {
        if (invulnerabilityTimer > 0f || isDead()) {
            return false;
        }

        currentHp -= amount;
        if (currentHp < 0) {
            currentHp = 0;
        }

        invulnerabilityTimer = 0.25f;
        return true;
    }

    /**
     * Renderiza o chefe com a textura roxa oficial e efeito de impacto.
     */
    public void render(SpriteBatch batch) {
        if (isDead()) {
            return;
        }

        // Se acabou de levar hit, pisca com flash branco
        if (invulnerabilityTimer > 0.12f) {
            batch.draw(hitFlashTexture, position.x, position.y, width, height);
        } else {
            batch.draw(bossTexture, position.x, position.y, width, height);
        }
    }

    public boolean isDead() {
        return currentHp <= 0;
    }

    public String getName() {
        return name;
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

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void dispose() {
        bossTexture.dispose();
        hitFlashTexture.dispose();
    }
}
