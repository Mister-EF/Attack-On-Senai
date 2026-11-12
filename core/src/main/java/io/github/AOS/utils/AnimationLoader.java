package io.github.AOS.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Utilitário responsável por carregar e processar animações a partir de arquivos JSON de spritesheet
 * e suas respectivas texturas PNG, gerando instâncias de Animation<TextureRegion> do LibGDX.
 * Implementa fallback seguro: caso arquivos não existam ou ocorra falha, retorna null para que
 * a entidade utilize os placeholders originais via Pixmap (AGENTS.md).
 */
public final class AnimationLoader {

    public static class SpriteAnimation {
        public final Animation<TextureRegion> animation;
        public final Texture sheetTexture;
        public final int frameWidth;
        public final int frameHeight;

        public SpriteAnimation(Animation<TextureRegion> animation, Texture sheetTexture, int frameWidth, int frameHeight) {
            this.animation = animation;
            this.sheetTexture = sheetTexture;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
        }

        public TextureRegion getKeyFrame(float stateTime, boolean looping) {
            return animation.getKeyFrame(stateTime, looping);
        }

        public void dispose() {
            if (sheetTexture != null) {
                sheetTexture.dispose();
            }
        }
    }

    private AnimationLoader() {
    }

    /**
     * Carrega uma animação a partir de um JSON e seu PNG correspondente.
     *
     * @param jsonPath Caminho relativo em assets/ (ex: "sprites/hero/hero-run-spritesheet.json")
     * @param pngPath  Caminho relativo em assets/ (ex: "sprites/hero/hero-run-spritesheet.png")
     * @param playMode Modo de reprodução (LOOP, NORMAL, etc.)
     * @return Objeto SpriteAnimation ou null em caso de ausência/erro
     */
    public static SpriteAnimation loadAnimation(String jsonPath, String pngPath, Animation.PlayMode playMode) {
        if (jsonPath == null || pngPath == null || Gdx.files == null) {
            return null;
        }

        try {
            FileHandle jsonHandle = Gdx.files.internal(jsonPath);
            FileHandle pngHandle = Gdx.files.internal(pngPath);

            if (!jsonHandle.exists() || !pngHandle.exists()) {
                Gdx.app.log("AnimationLoader", "Arquivos de animação não encontrados: " + jsonPath + " | " + pngPath);
                return null;
            }

            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(jsonHandle);
            if (root == null) {
                return null;
            }

            JsonValue frames = root.get("frames");
            if (frames == null) {
                return null;
            }

            Texture sheetTexture = new Texture(pngHandle);
            sheetTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            Array<TextureRegion> keyFrames = new Array<>();
            float frameDuration = 0.083f; // ~12 FPS por padrão

            int frameWidth = 256;
            int frameHeight = 144;

            JsonValue meta = root.get("meta");
            if (meta != null) {
                JsonValue animgen = meta.get("animgen");
                if (animgen != null) {
                    if (animgen.has("frameDurationMs")) {
                        frameDuration = animgen.getInt("frameDurationMs") / 1000f;
                    }
                    if (animgen.has("frameWidth")) {
                        frameWidth = animgen.getInt("frameWidth");
                    }
                    if (animgen.has("frameHeight")) {
                        frameHeight = animgen.getInt("frameHeight");
                    }
                }
            }

            for (JsonValue frameEntry = frames.child; frameEntry != null; frameEntry = frameEntry.next) {
                JsonValue f = frameEntry.get("frame");
                if (f != null) {
                    int x = f.getInt("x");
                    int y = f.getInt("y");
                    int w = f.getInt("w");
                    int h = f.getInt("h");
                    keyFrames.add(new TextureRegion(sheetTexture, x, y, w, h));
                }
            }

            if (keyFrames.size == 0) {
                sheetTexture.dispose();
                return null;
            }

            Animation<TextureRegion> anim = new Animation<>(frameDuration, keyFrames, playMode);
            return new SpriteAnimation(anim, sheetTexture, frameWidth, frameHeight);

        } catch (Exception e) {
            Gdx.app.log("AnimationLoader", "Falha ao processar animação de " + jsonPath + ": " + e.getMessage());
            return null;
        }
    }
}
