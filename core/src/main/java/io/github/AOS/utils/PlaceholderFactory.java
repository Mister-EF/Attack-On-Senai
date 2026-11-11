package io.github.AOS.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Fabrica utilitária responsável por gerar texturas e placeholders visuais em tempo de execução
 * utilizando Pixmap, conforme as diretrizes do AGENTS.md.
 */
public final class PlaceholderFactory {

    private PlaceholderFactory() {
        // Construtor privado para evitar instanciação de classe utilitária
    }

    /**
     * Cria uma textura retangular sólida com largura, altura e cor especificadas.
     *
     * @param width  Largura da textura em pixels.
     * @param height Altura da textura em pixels.
     * @param color  Cor de preenchimento.
     * @return Uma nova instância de Texture. Lembre-se de descartá-la quando não for mais necessária.
     */
    public static Texture createColoredTexture(int width, int height, Color color) {
        if (width <= 0) width = 1;
        if (height <= 0) height = 1;

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Cria uma textura placeholder com tamanho 1x1 na cor especificada.
     */
    public static Texture createColoredTexture(Color color) {
        return createColoredTexture(1, 1, color);
    }

    /**
     * Cria a textura placeholder do Jogador (Verde - Color.GREEN).
     */
    public static Texture createPlayerTexture(int width, int height) {
        return createColoredTexture(width, height, Color.GREEN);
    }

    public static Texture createPlayerTexture() {
        return createColoredTexture(Color.GREEN);
    }

    /**
     * Cria a textura placeholder de Inimigos Comuns/Minions (Vermelho - Color.RED).
     */
    public static Texture createMinionTexture(int width, int height) {
        return createColoredTexture(width, height, Color.RED);
    }

    public static Texture createMinionTexture() {
        return createColoredTexture(Color.RED);
    }

    /**
     * Cria a textura placeholder de Chão e Plataformas Sólidas (Cinza Claro - Color.LIGHT_GRAY).
     */
    public static Texture createGroundTexture(int width, int height) {
        return createColoredTexture(width, height, Color.LIGHT_GRAY);
    }

    public static Texture createGroundTexture() {
        return createColoredTexture(Color.LIGHT_GRAY);
    }

    /**
     * Cria a textura placeholder de Chefes/Bosses (Roxo - Color.PURPLE).
     */
    public static Texture createBossTexture(int width, int height) {
        return createColoredTexture(width, height, Color.PURPLE);
    }

    public static Texture createBossTexture() {
        return createColoredTexture(Color.PURPLE);
    }

    /**
     * Cria a textura placeholder de Fundo do Cenário (Preto - Color.BLACK).
     */
    public static Texture createBackgroundTexture(int width, int height) {
        return createColoredTexture(width, height, Color.BLACK);
    }

    public static Texture createBackgroundTexture() {
        return createColoredTexture(Color.BLACK);
    }

    /**
     * Cria a textura placeholder de Projéteis/Bullets do Boss (Amarelo - Color.YELLOW).
     */
    public static Texture createProjectileTexture(int width, int height) {
        return createColoredTexture(width, height, Color.YELLOW);
    }

    public static Texture createProjectileTexture() {
        return createColoredTexture(Color.YELLOW);
    }

    /**
     * Cria a textura placeholder de Espinhos/Obstáculos Letais com triângulos serrilhados.
     */
    public static Texture createSpikeTexture(int width, int height) {
        if (width <= 0) width = 32;
        if (height <= 0) height = 32;

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0f));
        pixmap.fill();

        // Desenha dentes/triângulos de espinho perigosos (vermelho escuro com ponta metálica)
        pixmap.setColor(new Color(0.85f, 0.2f, 0.2f, 1f));
        int numSpikes = Math.max(1, width / 16);
        int spikeW = width / numSpikes;

        for (int i = 0; i < numSpikes; i++) {
            int x1 = i * spikeW;
            int x2 = x1 + (spikeW / 2);
            int x3 = x1 + spikeW;
            pixmap.fillTriangle(x1, height, x2, 0, x3, height);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
