package io.github.AOS.systems;

import com.badlogic.gdx.Input.Keys;
import io.github.AOS.entities.Player;
import io.github.AOS.entities.bosses.Boss;
import io.github.AOS.screens.BossArenaScreen;

/**
 * Gerenciador de códigos e atalhos de depuração/demonstração (Cheats) para apresentações.
 * Controlado globalmente pelo botão secreto no Menu Principal.
 */
public class CheatManager {

    // Controle de exibição visual dos cheats (oculto por padrão)
    public static boolean cheatsDisplayEnabled = false;

    private String lastNotification = "";
    private float notificationTimer = 0f;

    public static void toggleCheatsDisplay() {
        cheatsDisplayEnabled = !cheatsDisplayEnabled;
    }

    public static boolean isCheatsDisplayEnabled() {
        return cheatsDisplayEnabled;
    }

    /**
     * Processa teclas de atalho de cheats se o display estiver ativado.
     *
     * @param keycode Código da tecla pressionada.
     * @param player  Instância do jogador.
     * @param boss    Instância do chefe atual.
     * @param arena   Instância da tela da arena.
     * @return true se uma tecla de cheat foi processada, false caso contrário.
     */
    public boolean handleCheatKey(int keycode, Player player, Boss boss, BossArenaScreen arena) {
        // Se a exibição dos cheats não estiver ativada pelo botão secreto, não processa os atalhos F1-F4
        if (!cheatsDisplayEnabled) {
            return false;
        }

        switch (keycode) {
            case Keys.F1:
                // F1: Modo Deus (Invencibilidade total)
                player.toggleGodMode();
                showNotification(player.isGodMode() ? "[F1] MODO DEUS: ATIVADO!" : "[F1] MODO DEUS: DESATIVADO!");
                return true;

            case Keys.F2:
                // F2: Instakill (Zera vida do Boss imediatamente)
                if (boss != null && !boss.isDead()) {
                    boss.takeDamage(999999);
                    showNotification("[F2] INSTAKILL EXECUTADO NO CHEFE!");
                }
                return true;

            case Keys.F3:
                // F3: Spawna imediatamente um Power-up de IA na posição do jogador
                arena.spawnPowerUpAtPlayer();
                showNotification("[F3] POWER-UP DE IA GERADO!");
                return true;

            case Keys.F4:
                // F4: Força a vitória da fase
                arena.forceVictory();
                showNotification("[F4] VITÓRIA FORÇADA COM SUCESSO!");
                return true;

            default:
                return false;
        }
    }

    public void update(float delta) {
        if (notificationTimer > 0f) {
            notificationTimer -= delta;
            if (notificationTimer <= 0f) {
                lastNotification = "";
            }
        }
    }

    public void showNotification(String message) {
        this.lastNotification = message;
        this.notificationTimer = 3.0f;
    }

    public String getLastNotification() {
        return lastNotification;
    }

    public boolean hasActiveNotification() {
        return cheatsDisplayEnabled && notificationTimer > 0f && !lastNotification.isEmpty();
    }
}
