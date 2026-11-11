package io.github.AOS.input;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.AOS.screens.MainMenuScreen;

/**
 * Processador de entrada dedicado para a tela de Menu Principal.
 * Mapeia cliques de mouse e movimentação do cursor para o espaço virtual.
 */
public class MenuInputHandler extends InputAdapter {

    private final MainMenuScreen menuScreen;
    private final Viewport viewport;
    private final Vector3 mouseCoords;

    public MenuInputHandler(MainMenuScreen menuScreen, Viewport viewport) {
        this.menuScreen = menuScreen;
        this.viewport = viewport;
        this.mouseCoords = new Vector3();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Buttons.LEFT) {
            // Converte coordenadas da tela para as coordenadas virtuais (1280x720)
            viewport.unproject(mouseCoords.set(screenX, screenY, 0));
            return menuScreen.handleClick(mouseCoords.x, mouseCoords.y);
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // Converte coordenadas do mouse para destacar botões (hover)
        viewport.unproject(mouseCoords.set(screenX, screenY, 0));
        menuScreen.handleMouseMove(mouseCoords.x, mouseCoords.y);
        return false;
    }
}
