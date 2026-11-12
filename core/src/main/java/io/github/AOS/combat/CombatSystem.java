package io.github.AOS.combat;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;
import io.github.AOS.entities.BossProjectile;
import io.github.AOS.entities.Player;
import io.github.AOS.entities.bosses.Boss;
import io.github.AOS.entities.bosses.BossC;
import io.github.AOS.entities.bosses.BossPython;
import io.github.AOS.systems.ScoreManager;

/**
 * Sistema de combate responsável por gerenciar colisões de ataque, investidas e projéteis
 * utilizando exclusivamente a classe nativa Intersector do LibGDX, sem Box2D.
 */
public class CombatSystem {

    public void update(float delta, Player player, Boss boss) {
        update(delta, player, boss, null);
    }

    /**
     * Processa interações de combate e computa pontuação de dano infligido.
     */
    public void update(float delta, Player player, Boss boss, ScoreManager scoreManager) {
        if (player.isDead() || boss.isDead()) {
            return;
        }

        // 1. Ataque do Jogador contra o Chefe
        if (player.isAttacking() && Intersector.overlaps(player.getAttackBounds(), boss.getBounds())) {
            int damage = player.getAttackDamage();
            if (damage > 0) {
                boolean damaged = boss.takeDamage(damage);
                if (damaged && scoreManager != null) {
                    scoreManager.addDamagePoints(damage, true);
                }
            }
        }

        // 2. Colisão / Investida direta do Chefe contra o Jogador
        if (Intersector.overlaps(player.getBounds(), boss.getBounds())) {
            if (!player.isInvulnerable()) {
                boolean isDashing = false;
                if (boss instanceof BossPython) {
                    isDashing = ((BossPython) boss).isDashing();
                } else if (boss instanceof BossC) {
                    isDashing = ((BossC) boss).isDashing();
                }

                int bossDamage = isDashing ? 30 : 15;
                float knockbackForce = isDashing ? 450f : 250f;

                float knockbackDir = (boss.getPosition().x < player.getPosition().x) ? 1f : -1f;

                player.takeDamage(bossDamage);
                player.applyKnockback(knockbackDir * knockbackForce, 300f);
            }
        }

        // 3. Colisão dos Projéteis (Bullets Amarelas) do Chefe contra o Jogador
        Array<BossProjectile> projectiles = null;
        if (boss instanceof BossPython) {
            projectiles = ((BossPython) boss).getProjectiles();
        } else if (boss instanceof BossC) {
            projectiles = ((BossC) boss).getProjectiles();
        }

        if (projectiles != null) {
            for (BossProjectile p : projectiles) {
                if (!p.isDestroyed() && Intersector.overlaps(player.getBounds(), p.getBounds())) {
                    if (!player.isInvulnerable()) {
                        player.takeDamage(p.getDamage());
                        float knockDir = (p.getBounds().x < player.getPosition().x) ? 1f : -1f;
                        player.applyKnockback(knockDir * 200f, 180f);
                    }
                    p.setDestroyed(true);
                }
            }
        }
    }
}
