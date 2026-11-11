package io.github.AOS.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Obstáculo letal fixo (espinhos/armadilhas) que exige saltos precisos nas seções de plataforma.
 */
public class Hazard {

    private final Rectangle bounds;
    private final Texture spikeTexture;
    private final int damage;

    public Hazard(float x, float y, float width, float height, int damage) {
        this.bounds = new Rectangle(x, y, width, height);
        this.damage = damage;
        this.spikeTexture = PlaceholderFactory.createSpikeTexture((int) width, (int) height);
    }

    public Hazard(float x, float y, float width, float height) {
        this(x, y, width, height, 35);
    }

    public void render(SpriteBatch batch) {
        batch.draw(spikeTexture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getDamage() {
        return damage;
    }

    public void dispose() {
        spikeTexture.dispose();
    }
}
