package io.github.AOS.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * Utilitário responsável por carregar texturas de imagem da pasta assets/ com
 * fallback automático para os placeholders gerados via Pixmap caso os arquivos não existam.
 */
public final class AssetLoader {

    public interface TextureFallbackSupplier {
        Texture get();
    }

    private AssetLoader() {
    }

    /**
     * Tenta carregar uma textura do caminho especificado em assets/.
     * Se o arquivo existir e for carregado com sucesso, retorna a textura do arquivo.
     * Caso contrário, executa o fallback via Pixmap e retorna a textura padrão do AGENTS.md.
     *
     * @param internalPath     Caminho do arquivo de imagem relativo à pasta assets (ex: "sprites/player.png").
     * @param fallbackSupplier Função geradora do placeholder caso a imagem não exista.
     * @return Texture carregada da imagem ou gerada via Pixmap.
     */
    public static Texture loadTextureWithFallback(String internalPath, TextureFallbackSupplier fallbackSupplier) {
        if (internalPath != null && Gdx.files != null) {
            try {
                if (Gdx.files.internal(internalPath).exists()) {
                    return new Texture(Gdx.files.internal(internalPath));
                }
            } catch (Exception e) {
                Gdx.app.log("AssetLoader", "Não foi possível carregar " + internalPath + ", acionando fallback: " + e.getMessage());
            }
        }
        return fallbackSupplier.get();
    }
}
