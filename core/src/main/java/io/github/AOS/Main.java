package io.github.AOS;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.AOS.screens.MainMenuScreen;

/**
 * Classe principal do jogo Attack on SENAI.
 * Gerencia o ciclo de vida da aplicação e a troca de telas via Game/Screen.
 */
public class Main extends Game {

    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // Inicializa exibindo o Menu Principal
        setScreen(new MainMenuScreen(this));
    }

    /**
     * Retorna a instância compartilhada do SpriteBatch para otimização de renderização.
     *
     * @return SpriteBatch principal da aplicação.
     */
    public SpriteBatch getBatch() {
        return batch;
    }

    @Override
    public void render() {
        // Delega o ciclo de renderização para a tela ativa (Screen.render)
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null) {
            batch.dispose();
        }
    }
}
