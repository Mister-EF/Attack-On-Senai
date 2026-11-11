package io.github.AOS.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Entidade de Power-up temático de Inteligência Artificial.
 * Aparece na arena como bloco temporário que concede melhorias ao jogador ao encostar.
 */
public class PowerUp {

    public enum Type {
        GEMINI("Gemini [Escudo]", new Color(0.20f, 0.65f, 1.0f, 1.0f), "G"),
        CLAUDE("Claude [Slow-Motion]", new Color(0.95f, 0.55f, 0.15f, 1.0f), "C"),
        CHATGPT("ChatGPT [Overclock Dano]", new Color(0.10f, 0.85f, 0.50f, 1.0f), "AI");

        private final String displayName;
        private final Color color;
        private final String badge;

        Type(String displayName, Color color, String badge) {
            this.displayName = displayName;
            this.color = color;
            this.badge = badge;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getColor() {
            return color;
        }

        public String getBadge() {
            return badge;
        }
    }

    public static final float SIZE = 44f;

    private final Type type;
    private final Vector2 basePosition;
    private final Vector2 currentPosition;
    private final Rectangle bounds;

    private float floatTimer = 0f;
    private boolean collected = false;

    private final Texture texture;
    private final Texture borderTexture;

    public PowerUp(Type type, float x, float y) {
        this.type = type;
        this.basePosition = new Vector2(x, y);
        this.currentPosition = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, SIZE, SIZE);

        // Textura com cor temática do modelo de IA
        this.texture = PlaceholderFactory.createColoredTexture(type.getColor());
        this.borderTexture = PlaceholderFactory.createColoredTexture(Color.WHITE);
    }

    /**
     * Atualiza a flutuação do power-up na arena.
     */
    public void update(float delta) {
        if (collected) {
            return;
        }

        floatTimer += delta;
        // Efeito clássico de levitação vertical suave
        float offsetY = MathUtils.sin(floatTimer * 4.5f) * 6f;
        currentPosition.y = basePosition.y + offsetY;
        bounds.setPosition(currentPosition.x, currentPosition.y);
    }

    /**
     * Aplica o efeito do power-up sobre o jogador.
     */
    public void apply(Player player) {
        if (collected) {
            return;
        }

        collected = true;

        switch (type) {
            case GEMINI:
                // Escudo protetor total por 10 segundos
                player.applyGeminiShield(10.0f);
                break;
            case CLAUDE:
                // Efeito slow-motion / foco temporal por 8 segundos
                player.applyClaudeSlow(8.0f);
                break;
            case CHATGPT:
                // Overclock de dano dobrado e maior velocidade por 8 segundos
                player.applyChatGptBuff(8.0f);
                break;
        }
    }

    /**
     * Renderiza o bloco flutuante do power-up na arena.
     */
    public void render(SpriteBatch batch, BitmapFont font, GlyphLayout glyphLayout) {
        if (collected) {
            return;
        }

        // Borda branca externa destacando o item
        float borderOffset = 3f;
        batch.draw(borderTexture, currentPosition.x - borderOffset, currentPosition.y - borderOffset, SIZE + (borderOffset * 2), SIZE + (borderOffset * 2));

        // Bloco interno colorido do power-up
        batch.draw(texture, currentPosition.x, currentPosition.y, SIZE, SIZE);

        // Letra/Sigla de identificação no centro do bloco
        font.setColor(Color.WHITE);
        glyphLayout.setText(font, type.getBadge());
        float textX = currentPosition.x + (SIZE - glyphLayout.width) / 2f;
        float textY = currentPosition.y + (SIZE + glyphLayout.height) / 2f;
        font.draw(batch, glyphLayout, textX, textY);
    }

    public Type getType() {
        return type;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isCollected() {
        return collected;
    }

    public void dispose() {
        texture.dispose();
        borderTexture.dispose();
    }
}
