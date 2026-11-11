package io.github.AOS.combat;

import com.badlogic.gdx.math.Intersector;
import io.github.AOS.entities.BossProjectile;
import io.github.AOS.entities.Player;
import io.github.AOS.entities.bosses.Boss;
import io.github.AOS.entities.bosses.BossC;

/**
 * Sistema de combate responsável por gerenciar colisões de ataque, investidas e projéteis
 * utilizando exclusivamente a classe nativa Intersector do LibGDX, sem Box2D.
 */
public class CombatSystem {

    /**
     * Processa interações de combate e verificações de hit entre o Jogador, o Chefe e suas Bullets.
     *
     * @param delta  Tempo transcorrido desde o último quadro.
     * @param player Instância do jogador.
     * @param boss   Instância do chefe ativo.
     */
    public void update(float delta, Player player, Boss boss) {
        if (player.isDead() || boss.isDead()) {
            return;
        }

        // 1. Ataque do Jogador contra o Chefe
        if (player.isAttacking() && Intersector.overlaps(player.getAttackBounds(), boss.getBounds())) {
            int damage = player.getAttackDamage();
            if (damage > 0) {
                boss.takeDamage(damage);
            }
        }

        // 2. Colisão / Investida direta do Chefe contra o Jogador
        if (Intersector.overlaps(player.getBounds(), boss.getBounds())) {
            if (!player.isInvulnerable()) {
                boolean isDashing = (boss instanceof BossC) && ((BossC) boss).isDashing();
                int bossDamage = isDashing ? 30 : 15;
                float knockbackForce = isDashing ? 450f : 250f;

                float knockbackDir = (boss.getPosition().x < player.getPosition().x) ? 1f : -1f;

                player.takeDamage(bossDamage);
                player.applyKnockback(knockbackDir * knockbackForce, 300f);
            }
        }

        // 3. Colisão dos Projéteis (Bullets Amarelas) do Chefe contra o Jogador
        if (boss instanceof BossC) {
            BossC bossC = (BossC) boss;
            for (BossProjectile p : bossC.getProjectiles()) {
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
