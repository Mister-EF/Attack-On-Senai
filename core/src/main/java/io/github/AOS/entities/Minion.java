package io.github.AOS.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.AOS.utils.AnimationLoader;
import io.github.AOS.utils.AnimationLoader.SpriteAnimation;
import io.github.AOS.utils.AssetLoader;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Inimigo Básico (Minion) estável que patrulha estritamente os limites da plataforma onde nasceu.
 * Sem inteligência de aggro e sem saltos para fora das plataformas, impedindo quedas no cenário.
 * Suporta animações reais de spritesheet (walk, hurt, attack) e idle via PNG com fallback
 * para o placeholder Vermelho (Color.RED) conforme o AGENTS.md.
 */
public class Minion {

    public static final float WIDTH = 48f;
    public static final float HEIGHT = 52f;
    private static final float PATROL_SPEED = 90f;

    // Dimensões de renderização do Sprite (proporção 256x144)
    private static final float SPRITE_RENDER_HEIGHT = 76f;
    private static final float SPRITE_RENDER_WIDTH = SPRITE_RENDER_HEIGHT * (256f / 144f); // ~135.1f

    private final Vector2 position;
    private final Rectangle bounds;

    private final float patrolMinX;
    private final float patrolMaxX;
    private final float fixedY; // Altura fixa e segura sobre a plataforma

    private int hp = 30;
    private float invulnerabilityTimer = 0f;
    private boolean movingRight = true;
    private float stateTime = 0f;

    // Animações dos arquivos de assets/sprites/genericenemy/
    private final SpriteAnimation walkAnim;
    private final SpriteAnimation hurtAnim;
    private final SpriteAnimation attackAnim;

    // Textura isolada idle ("enemy.png")
    private final Texture idleTexture;

    // Fallbacks e efeitos visuais
    private final Texture minionFallbackTexture;
    private final Texture hitFlashTexture;

    public Minion(float startX, float startY, float patrolMinX, float patrolMaxX) {
        this.fixedY = startY;
        this.position = new Vector2(startX, fixedY);
        this.bounds = new Rectangle(startX, fixedY, WIDTH, HEIGHT);

        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;

        // 1. Carrega as animações do Minion via JSON/PNG
        this.walkAnim = AnimationLoader.loadAnimation(
                "sprites/genericenemy/enemy_walk-spritesheet.json",
                "sprites/genericenemy/enemy_walk-spritesheet.png",
                Animation.PlayMode.LOOP
        );
        this.hurtAnim = AnimationLoader.loadAnimation(
                "sprites/genericenemy/enemy_hurt-spritesheet.json",
                "sprites/genericenemy/enemy_hurt-spritesheet.png",
                Animation.PlayMode.NORMAL
        );
        this.attackAnim = AnimationLoader.loadAnimation(
                "sprites/genericenemy/enemy_attack-spritesheet.json",
                "sprites/genericenemy/enemy_attack-spritesheet.png",
                Animation.PlayMode.NORMAL
        );

        // 2. Carrega a textura isolada idle
        this.idleTexture = AssetLoader.loadTextureWithFallback(
                "sprites/genericenemy/enemy.png",
                PlaceholderFactory::createMinionTexture
        );

        // 3. Fallbacks nativos (Vermelho do AGENTS.md e flash branco de dano)
        this.minionFallbackTexture = PlaceholderFactory.createMinionTexture();
        this.hitFlashTexture = PlaceholderFactory.createColoredTexture(Color.WHITE);
    }

    /**
     * Atualiza a patrulha estritamente contida nos limites horizontais da plataforma.
     */
    public void update(float delta) {
        if (isDead()) {
            return;
        }

        stateTime += delta;

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
        invulnerabilityTimer = 0.22f;
        return true;
    }

    public void render(SpriteBatch batch) {
        if (isDead()) {
            return;
        }

        // Seleção do frame de animação
        TextureRegion currentFrame = null;

        if (invulnerabilityTimer > 0f && hurtAnim != null) {
            currentFrame = hurtAnim.getKeyFrame(stateTime, false);
        } else if (walkAnim != null) {
            currentFrame = walkAnim.getKeyFrame(stateTime, true);
        }

        if (currentFrame != null) {
            float renderWidth = SPRITE_RENDER_WIDTH;
            float renderHeight = SPRITE_RENDER_HEIGHT;
            float renderX = position.x + (WIDTH / 2f) - (renderWidth / 2f);
            float renderY = position.y;

            if (invulnerabilityTimer > 0.12f) {
                // Breve flash de impacto
                batch.draw(hitFlashTexture, position.x, position.y, WIDTH, HEIGHT);
            } else if (movingRight) {
                batch.draw(currentFrame, renderX, renderY, renderWidth, renderHeight);
            } else {
                // Espelhado para a esquerda
                batch.draw(currentFrame, renderX + renderWidth, renderY, -renderWidth, renderHeight);
            }
        } else if (idleTexture != null) {
            float renderWidth = SPRITE_RENDER_WIDTH;
            float renderHeight = SPRITE_RENDER_HEIGHT;
            float renderX = position.x + (WIDTH / 2f) - (renderWidth / 2f);
            float renderY = position.y;

            if (invulnerabilityTimer > 0.10f) {
                batch.draw(hitFlashTexture, position.x, position.y, WIDTH, HEIGHT);
            } else if (movingRight) {
                batch.draw(idleTexture, renderX, renderY, renderWidth, renderHeight);
            } else {
                batch.draw(idleTexture, renderX + renderWidth, renderY, -renderWidth, renderHeight);
            }
        } else {
            // Fallback nativo: Vermelho oficial via Pixmap (AGENTS.md)
            if (invulnerabilityTimer > 0.08f) {
                batch.draw(hitFlashTexture, position.x, position.y, WIDTH, HEIGHT);
            } else {
                batch.draw(minionFallbackTexture, position.x, position.y, WIDTH, HEIGHT);
            }
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
        if (walkAnim != null) walkAnim.dispose();
        if (hurtAnim != null) hurtAnim.dispose();
        if (attackAnim != null) attackAnim.dispose();

        if (idleTexture != null) idleTexture.dispose();
        minionFallbackTexture.dispose();
        hitFlashTexture.dispose();
    }
}
