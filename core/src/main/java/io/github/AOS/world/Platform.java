package io.github.AOS.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Plataforma sólida suspensa para a etapa de progressão vertical.
 * Utiliza a textura compartilhada de solo/tile (dirt.png ou Cinza Claro do AGENTS.md),
 * otimizando o consumo de VRAM e o tempo de carregamento da arena.
 */
public class Platform {

    private final Rectangle bounds;
    private final Texture texture;
    private final boolean ownsTexture;

    public Platform(float x, float y, float width, float height, Texture sharedTexture) {
        this.bounds = new Rectangle(x, y, width, height);
        if (sharedTexture != null) {
            this.texture = sharedTexture;
            this.ownsTexture = false;
        } else {
            this.texture = PlaceholderFactory.createGroundTexture();
            this.ownsTexture = true;
        }
    }

    public Platform(float x, float y, float width, float height) {
        this(x, y, width, height, null);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        if (ownsTexture && texture != null) {
            texture.dispose();
        }
    }
}
