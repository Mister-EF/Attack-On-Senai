package io.github.AOS.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Gerencia a renderização de fundo com efeito parallax em múltiplas camadas sutis,
 * criando profundidade em relação ao deslocamento horizontal da câmera.
 */
public class ParallaxBackground {

    // Texturas de fundo e camadas
    private final Texture solidBackgroundTexture;
    private final Texture distantStripeTexture;
    private final Texture midStripeTexture;

    // Fatores de deslocamento parallax
    private static final float DISTANT_PARALLAX_FACTOR = 0.15f;
    private static final float MID_PARALLAX_FACTOR = 0.35f;

    // Dimensões do padrão de repetição (em pixels)
    private static final float DISTANT_STRIPE_WIDTH = 90f;
    private static final float DISTANT_STRIPE_SPACING = 300f;

    private static final float MID_STRIPE_WIDTH = 45f;
    private static final float MID_STRIPE_SPACING = 200f;

    public ParallaxBackground() {
        // Fundo preto base conforme AGENTS.md
        this.solidBackgroundTexture = PlaceholderFactory.createBackgroundTexture();

        // Camada 1: Listras largas distantes com tom cinza bem escuro
        this.distantStripeTexture = PlaceholderFactory.createColoredTexture(
                new Color(0.12f, 0.12f, 0.16f, 1f)
        );

        // Camada 2: Listras intermediárias ligeiramente mais claras
        this.midStripeTexture = PlaceholderFactory.createColoredTexture(
                new Color(0.18f, 0.18f, 0.22f, 1f)
        );
    }

    /**
     * Renderiza o fundo preto e as camadas de listras com rolagem em paralaxe.
     *
     * @param batch  SpriteBatch já inicializado com a matriz de projeção da câmera.
     * @param camera Câmera ortográfica atual do jogo.
     */
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        float viewWidth = camera.viewportWidth;
        float viewHeight = camera.viewportHeight;

        float camLeft = camera.position.x - (viewWidth / 2f);
        float camBottom = camera.position.y - (viewHeight / 2f);

        // 1. Fundo preto preenchendo toda a visão da câmera
        batch.draw(solidBackgroundTexture, camLeft, camBottom, viewWidth, viewHeight);

        // 2. Camada distante de listras/pilares (parallax lento: 0.15x)
        renderStripeLayer(
                batch,
                camera,
                distantStripeTexture,
                DISTANT_PARALLAX_FACTOR,
                DISTANT_STRIPE_WIDTH,
                DISTANT_STRIPE_SPACING,
                viewHeight * 0.90f
        );

        // 3. Camada intermediária de listras/estruturas (parallax médio: 0.35x)
        renderStripeLayer(
                batch,
                camera,
                midStripeTexture,
                MID_PARALLAX_FACTOR,
                MID_STRIPE_WIDTH,
                MID_STRIPE_SPACING,
                viewHeight * 0.75f
        );
    }

    /**
     * Desenha uma camada de listras repetidas com deslocamento proporcional à câmera.
     */
    private void renderStripeLayer(
            SpriteBatch batch,
            OrthographicCamera camera,
            Texture texture,
            float parallaxFactor,
            float stripeWidth,
            float spacing,
            float stripeHeight
    ) {
        float viewWidth = camera.viewportWidth;
        float camLeft = camera.position.x - (viewWidth / 2f);
        float camRight = camera.position.x + (viewWidth / 2f);

        // Deslocamento da camada baseado na posição horizontal da câmera
        float parallaxOffset = (camera.position.x * parallaxFactor) % spacing;

        // Calcula a primeira posição visível à esquerda da câmera
        float startX = camLeft - parallaxOffset - spacing;
        float endX = camRight + spacing;

        for (float x = startX; x <= endX; x += spacing) {
            batch.draw(texture, x, 0f, stripeWidth, stripeHeight);
        }
    }

    /**
     * Libera as texturas alocadas.
     */
    public void dispose() {
        solidBackgroundTexture.dispose();
        distantStripeTexture.dispose();
        midStripeTexture.dispose();
    }
}
