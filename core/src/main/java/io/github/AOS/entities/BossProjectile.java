package io.github.AOS.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Projétil disparado pelo Boss final (Bullet Amarela).
 * Desloca-se em linha reta na direção do jogador e causa dano por colisão.
 */
public class BossProjectile {

    public static final float SIZE = 18f;
    private static final float SPEED = 340f;
    private static final int DAMAGE = 15;

    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle bounds;

    private float lifetime = 5.0f; // Duração máxima de 5 segundos
    private boolean destroyed = false;

    private final Texture texture;

    public BossProjectile(float startX, float startY, float targetX, float targetY) {
        this.position = new Vector2(startX, startY);
        this.bounds = new Rectangle(startX, startY, SIZE, SIZE);

        // Calcula o vetor de direção normalizado apontando para o jogador
        Vector2 dir = new Vector2(targetX - startX, targetY - startY).nor();
        this.velocity = new Vector2(dir.x * SPEED, dir.y * SPEED);

        // Textura placeholder Amarela conforme solicitado
        this.texture = PlaceholderFactory.createProjectileTexture();
    }

    /**
     * Atualiza a posição da bullet no espaço de jogo.
     */
    public void update(float delta) {
        if (destroyed) {
            return;
        }

        lifetime -= delta;
        if (lifetime <= 0f) {
            destroyed = true;
            return;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        bounds.setPosition(position.x, position.y);
    }

    public void render(SpriteBatch batch) {
        if (destroyed) {
            return;
        }
        batch.draw(texture, position.x, position.y, SIZE, SIZE);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getDamage() {
        return DAMAGE;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public void dispose() {
        texture.dispose();
    }
}
