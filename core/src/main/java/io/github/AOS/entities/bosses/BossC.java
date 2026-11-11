package io.github.AOS.entities.bosses;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.github.AOS.entities.BossProjectile;
import io.github.AOS.entities.Player;
import io.github.AOS.utils.PlaceholderFactory;

/**
 * Chefe: O Ponteiro Perdido (Inspirado na linguagem C).
 * Implementa máquina de estados cíclica (investida horizontal veloz) e
 * disparo periódico de projéteis (bullets amarelas) em direção ao jogador.
 */
public class BossC extends Boss {

    public enum State {
        IDLE,        // Fica parado observando e mirando no jogador (2.0s)
        PREPARING,   // Preparação visual rápida antes do arranque (0.4s)
        DASHING,     // Investida em alta velocidade na direção do jogador (0.75s)
        RECOVERING   // Breve pausa para recuperação pós-investida (0.6s)
    }

    private static final float BOSS_WIDTH = 96f;
    private static final float BOSS_HEIGHT = 96f;
    private static final int INITIAL_HP = 200;

    private static final float GRAVITY = -1400f;
    private static final float DASH_SPEED = 650f;
    private static final float SHOOT_INTERVAL = 1.8f;

    private State currentState = State.IDLE;
    private float stateTimer = 2.0f; // Inicia com 2 segundos de espera
    private float dashDirection = -1f; // -1 para esquerda, +1 para direita

    // Projéteis periódicos disparados pelo chefe
    private final Array<BossProjectile> projectiles;
    private float shootTimer = 1.2f;

    // Textura adicional para estado de investida / telegraph
    private final Texture prepTexture;

    public BossC(float startX, float startY) {
        super("O Ponteiro Perdido [C]", startX, startY, BOSS_WIDTH, BOSS_HEIGHT, INITIAL_HP);
        this.prepTexture = PlaceholderFactory.createColoredTexture(new Color(0.9f, 0.2f, 0.2f, 1f));
        this.projectiles = new Array<>();
    }

    @Override
    protected void updateBoss(float delta, Player player, Rectangle groundBounds) {
        // Gravidade
        velocity.y += GRAVITY * delta;
        if (velocity.y < -900f) {
            velocity.y = -900f;
        }

        // Máquina de Estados da investida
        stateTimer -= delta;

        switch (currentState) {
            case IDLE:
                velocity.x = 0f;
                facingRight = (player.getPosition().x > position.x);

                if (stateTimer <= 0f) {
                    currentState = State.PREPARING;
                    stateTimer = 0.4f;
                    dashDirection = (player.getPosition().x >= position.x) ? 1f : -1f;
                    facingRight = (dashDirection > 0f);
                }
                break;

            case PREPARING:
                velocity.x = 0f;
                if (stateTimer <= 0f) {
                    currentState = State.DASHING;
                    stateTimer = 0.75f;
                    velocity.x = dashDirection * DASH_SPEED;
                }
                break;

            case DASHING:
                velocity.x = dashDirection * DASH_SPEED;
                if (stateTimer <= 0f) {
                    currentState = State.RECOVERING;
                    stateTimer = 0.6f;
                    velocity.x = 0f;
                }
                break;

            case RECOVERING:
                velocity.x = 0f;
                if (stateTimer <= 0f) {
                    currentState = State.IDLE;
                    stateTimer = 2.0f;
                }
                break;
        }

        // Movimento do boss
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Colisão com o chão
        if (bounds.overlaps(groundBounds)) {
            if (velocity.y <= 0f && position.y + (height * 0.5f) > groundBounds.y + groundBounds.height) {
                position.y = groundBounds.y + groundBounds.height;
                velocity.y = 0f;
                isGrounded = true;
            }
        }

        // Disparo de projéteis amarelos periódicos em direção ao jogador
        updateShooting(delta, player);

        // Atualiza a posição de todos os projéteis ativos
        for (int i = projectiles.size - 1; i >= 0; i--) {
            BossProjectile p = projectiles.get(i);
            p.update(delta);
            if (p.isDestroyed()) {
                p.dispose();
                projectiles.removeIndex(i);
            }
        }
    }

    /**
     * Dispara periodicamente uma bullet amarela direcionada ao centro do jogador.
     */
    private void updateShooting(float delta, Player player) {
        float distanceToPlayer = Math.abs(player.getPosition().x - position.x);

        // Dispara apenas quando o jogador estiver no alcance da arena do Boss (< 1500px)
        if (distanceToPlayer < 1500f) {
            shootTimer -= delta;
            if (shootTimer <= 0f) {
                shootTimer = SHOOT_INTERVAL;

                float startX = position.x + (width / 2f);
                float startY = position.y + (height / 2f);
                float targetX = player.getPosition().x + (player.getWidth() / 2f);
                float targetY = player.getPosition().y + (player.getHeight() / 2f);

                projectiles.add(new BossProjectile(startX, startY, targetX, targetY));
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (isDead()) {
            return;
        }

        // Renderiza primeiro as bullets amarelas do Boss
        for (BossProjectile p : projectiles) {
            p.render(batch);
        }

        // Renderiza o corpo do Boss (Roxo nativo)
        if (invulnerabilityTimer > 0.12f) {
            batch.draw(hitFlashTexture, position.x, position.y, width, height);
        } else if (currentState == State.PREPARING) {
            batch.draw(prepTexture, position.x, position.y, width, height);
        } else {
            batch.draw(bossTexture, position.x, position.y, width, height);
        }
    }

    public Array<BossProjectile> getProjectiles() {
        return projectiles;
    }

    public boolean isDashing() {
        return currentState == State.DASHING;
    }

    public State getCurrentState() {
        return currentState;
    }

    @Override
    public void dispose() {
        super.dispose();
        prepTexture.dispose();
        for (BossProjectile p : projectiles) {
            p.dispose();
        }
        projectiles.clear();
    }
}
