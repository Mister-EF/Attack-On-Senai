package io.github.AOS.systems;

/**
 * Enum global de Dificuldade do jogo "Attack on SENAI".
 * Define três níveis com multiplicadores aplicados à velocidade e agressividade do Boss Python.
 *
 * - FACIL  (Fácil)  : Boss a 0.5x da velocidade/cadência padrão.
 * - MEDIO  (Médio)  : Boss à velocidade padrão 1.0x.
 * - DIFICIL(Difícil): Boss a 1.5x — mais veloz e agressivo.
 */
public enum Difficulty {

    FACIL("Fácil", 0.5f),
    MEDIO("Médio", 1.0f),
    DIFICIL("Difícil", 1.5f);

    private final String displayName;
    private final float speedMultiplier;

    Difficulty(String displayName, float speedMultiplier) {
        this.displayName = displayName;
        this.speedMultiplier = speedMultiplier;
    }

    /** Nome legível para exibição na tela. */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Multiplicador de velocidade/cadência aplicado ao Boss Python.
     * 0.5x = Fácil, 1.0x = Médio, 1.5x = Difícil.
     */
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }
}
