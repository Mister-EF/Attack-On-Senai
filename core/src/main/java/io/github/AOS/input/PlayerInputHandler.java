package io.github.AOS.input;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import io.github.AOS.entities.Player;

/**
 * Processador de entrada dedicado para o jogador, comandos gerais, pausa e inserção de texto.
 */
public class PlayerInputHandler extends InputAdapter {

    public interface ArenaInputListener {
        void onEscapePressed();
        void onRestartPressed();
        boolean onCheatKeyPressed(int keycode);
        boolean onScreenClicked(int screenX, int screenY);
        void onScreenMouseMoved(int screenX, int screenY);
        boolean onKeyTyped(char character);
        boolean onEnterPressed();
    }

    private final Player player;
    private ArenaInputListener arenaListener;

    public PlayerInputHandler(Player player) {
        this.player = player;
    }

    public void setArenaListener(ArenaInputListener arenaListener) {
        this.arenaListener = arenaListener;
    }

    @Override
    public boolean keyDown(int keycode) {
        // Se a tela estiver em modo de inserção de apelido, Enter confirma
        if (keycode == Keys.ENTER || keycode == Keys.NUMPAD_ENTER) {
            if (arenaListener != null && arenaListener.onEnterPressed()) {
                return true;
            }
        }

        switch (keycode) {
            case Keys.A:
            case Keys.LEFT:
                player.setMoveLeft(true);
                return true;

            case Keys.D:
            case Keys.RIGHT:
                player.setMoveRight(true);
                return true;

            case Keys.SPACE:
            case Keys.W:
            case Keys.UP:
                player.jump();
                return true;

            case Keys.J:
                player.attack(1);
                return true;

            case Keys.K:
                player.attack(2);
                return true;

            case Keys.L:
                player.attack(3);
                return true;

            case Keys.ESCAPE:
                if (arenaListener != null) {
                    arenaListener.onEscapePressed();
                    return true;
                }
                return false;

            case Keys.R:
                if (arenaListener != null) {
                    arenaListener.onRestartPressed();
                    return true;
                }
                return false;

            default:
                if (arenaListener != null && arenaListener.onCheatKeyPressed(keycode)) {
                    return true;
                }
                return false;
        }
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.A:
            case Keys.LEFT:
                player.setMoveLeft(false);
                return true;

            case Keys.D:
            case Keys.RIGHT:
                player.setMoveRight(false);
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean keyTyped(char character) {
        if (arenaListener != null && arenaListener.onKeyTyped(character)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Buttons.LEFT && arenaListener != null) {
            return arenaListener.onScreenClicked(screenX, screenY);
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (arenaListener != null) {
            arenaListener.onScreenMouseMoved(screenX, screenY);
        }
        return false;
    }
}
