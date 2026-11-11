package io.github.AOS.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Plataforma sólida suspensa para a etapa de progressão vertical.
 * Renderizada com placeholder Cinza Claro conforme o AGENTS.md.
 */
public class Platform {

    private final Rectangle bounds;
    private final Texture texture;

    public Platform(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.texture = PlaceholderFactory.createGroundTexture();
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }
}
