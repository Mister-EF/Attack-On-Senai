package io.github.AOS.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Inimigo Básico (Minion) estável que patrulha estritamente os limites da plataforma onde nasceu.
 * Sem inteligência de aggro e sem saltos para fora das plataformas, impedindo quedas no cenário.
 * Representado pelo placeholder Vermelho (Color.RED) conforme o AGENTS.md.
 */
public class Minion {

    public static final float WIDTH = 48f;
    public static final float HEIGHT = 48f;
    private static final float PATROL_SPEED = 90f;

    private final Vector2 position;
    private final Rectangle bounds;

    private final float patrolMinX;
    private final float patrolMaxX;
    private final float fixedY; // Altura fixa e segura sobre a plataforma

    private int hp = 30;
    private float invulnerabilityTimer = 0f;
    private boolean movingRight = true;

    private final Texture minionTexture;
    private final Texture hitFlashTexture;

    public Minion(float startX, float startY, float patrolMinX, float patrolMaxX) {
        this.fixedY = startY;
        this.position = new Vector2(startX, fixedY);
        this.bounds = new Rectangle(startX, fixedY, WIDTH, HEIGHT);

        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;

        // Placeholder Vermelho nativo conforme AGENTS.md
        this.minionTexture = PlaceholderFactory.createMinionTexture();
        this.hitFlashTexture = PlaceholderFactory.createColoredTexture(Color.WHITE);
    }

    /**
     * Atualiza a patrulha estritamente contida nos limites horizontais da plataforma.
     */
    public void update(float delta) {
        if (isDead()) {
            return;
        }

        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer < 0f) invulnerabilityTimer = 0f;
        }

        // Movimentação horizontal estritamente limitada à plataforma
        if (movingRight) {
            position.x += PATROL_SPEED * delta;
            if (position.x >= patrolMaxX) {
                position.x = patrolMaxX;
                movingRight = false;
            }
        } else {
            position.x -= PATROL_SPEED * delta;
            if (position.x <= patrolMinX) {
                position.x = patrolMinX;
                movingRight = true;
            }
        }

        // Garante que o minion nunca saia da plataforma nem caia em Y
        position.x = MathUtils.clamp(position.x, patrolMinX, patrolMaxX);
        position.y = fixedY;

        bounds.setPosition(position.x, position.y);
    }

    /**
     * Aplica dano ao minion.
     */
    public boolean takeDamage(int amount) {
        if (invulnerabilityTimer > 0f || isDead()) {
            return false;
        }

        hp -= amount;
        if (hp < 0) hp = 0;
        invulnerabilityTimer = 0.20f;
        return true;
    }

    public void render(SpriteBatch batch) {
        if (isDead()) {
            return;
        }

        if (invulnerabilityTimer > 0.08f) {
            batch.draw(hitFlashTexture, position.x, position.y, WIDTH, HEIGHT);
        } else {
            batch.draw(minionTexture, position.x, position.y, WIDTH, HEIGHT);
        }
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getPosition() {
        return position;
    }

    public int getHp() {
        return hp;
    }

    public void dispose() {
        minionTexture.dispose();
        hitFlashTexture.dispose();
    }
}
