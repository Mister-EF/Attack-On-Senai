package io.github.AOS.systems;

import io.github.AOS.entities.PowerUp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gerenciador de Pontuação e Leaderboard local em memória.
 * Acumula pontos por dano a inimigos/Boss, coleta de Power-ups de IA e vitória,
 * mantendo o ranking ordenado de forma estritamente decrescente.
 */
public class ScoreManager {

    /**
     * Entrada individual no Leaderboard.
     */
    public static class ScoreEntry implements Comparable<ScoreEntry> {
        private final String nickname;
        private final int score;

        public ScoreEntry(String nickname, int score) {
            this.nickname = nickname;
            this.score = score;
        }

        public String getNickname() {
            return nickname;
        }

        public int getScore() {
            return score;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            // Ordenação estritamente decrescente: maior pontuação em 1º lugar
            return Integer.compare(other.score, this.score);
        }
    }

    // Lista estática mantida em memória compartilhada entre telas e partidas
    private static final List<ScoreEntry> LEADERBOARD = new ArrayList<>();

    static {
        // Registros iniciais de referência para demonstração
        addScore("TURING", 18500);
        addScore("LOVELACE", 14200);
        addScore("HOPPER", 11800);
    }

    // Estado da partida atual
    private int currentScore = 0;
    private int damageDealtToBoss = 0;
    private int damageDealtToMinions = 0;
    private int powerUpsCollected = 0;
    private boolean victoryBonusAdded = false;

    public ScoreManager() {
        reset();
    }

    public void reset() {
        this.currentScore = 0;
        this.damageDealtToBoss = 0;
        this.damageDealtToMinions = 0;
        this.powerUpsCollected = 0;
        this.victoryBonusAdded = false;
    }

    /**
     * Pontua com base no dano infligido ao Boss Python ou aos Minions.
     */
    public void addDamagePoints(int damage, boolean isBoss) {
        if (damage <= 0) return;

        if (isBoss) {
            damageDealtToBoss += damage;
            currentScore += damage * 15; // 15 pontos por ponto de dano no Boss Python
        } else {
            damageDealtToMinions += damage;
            currentScore += damage * 8;  // 8 pontos por ponto de dano nos Minions
        }
    }

    /**
     * Pontua com base no tipo de Power-up de IA coletado.
     */
    public void addPowerUpPoints(PowerUp.Type type) {
        powerUpsCollected++;
        switch (type) {
            case GEMINI:
                currentScore += 300; // Escudo defensivo
                break;
            case CLAUDE:
                currentScore += 350; // Slow-motion
                break;
            case CHATGPT:
                currentScore += 450; // Overclock de dano
                break;
            default:
                currentScore += 200;
                break;
        }
    }

    /**
     * Adiciona bônus ao derrotar o Boss Python.
     */
    public void addVictoryBonus(int remainingHp) {
        if (!victoryBonusAdded) {
            victoryBonusAdded = true;
            currentScore += 2000; // Bônus por derrotar o Boss Python
            currentScore += remainingHp * 10; // Bônus de vida conservada
        }
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getDamageDealtToBoss() {
        return damageDealtToBoss;
    }

    public int getDamageDealtToMinions() {
        return damageDealtToMinions;
    }

    public int getPowerUpsCollected() {
        return powerUpsCollected;
    }

    /**
     * Registra uma nova entrada no Leaderboard e reordena decrescentemente.
     */
    public static synchronized void addScore(String nickname, int score) {
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = "ALUNO_SENAI";
        }
        LEADERBOARD.add(new ScoreEntry(nickname.trim().toUpperCase(), score));
        Collections.sort(LEADERBOARD);
    }

    /**
     * Retorna as melhores pontuações limitadas ao tamanho solicitado.
     */
    public static synchronized List<ScoreEntry> getTopScores(int limit) {
        List<ScoreEntry> result = new ArrayList<>();
        int count = Math.min(limit, LEADERBOARD.size());
        for (int i = 0; i < count; i++) {
            result.add(LEADERBOARD.get(i));
        }
        return result;
    }
}
