package com.unlock.core;

/**
 * Mini-jeu 2 : Aligner les fréquences radio (Carte 25)
 * 
 * Le joueur doit régler 3 molettes pour atteindre la fréquence 4721 Hz.
 * Molette 1 = milliers (0-9), Molette 2 = centaines (0-9), Molette 3 = dizaines+unités (00-99)
 * (Indice donné par la carte 11 — Journal du Dr. Vasquez : "fréquence 4721")
 */
public class RadioMiniGame implements MiniGame {

    private static final long serialVersionUID = 1L;

    public static final String GAME_ID = "radio";
    public static final int TARGET_FREQUENCY = 4721;

    // Valeurs des 3 molettes
    private int dialThousands;   // 0-9  (milliers)
    private int dialHundreds;    // 0-9  (centaines)
    private int dialTensUnits;   // 0-99 (dizaines + unités)
    private boolean completed;

    public RadioMiniGame() {
        // Départ aléatoire pour plus de challenge
        this.dialThousands = 1;
        this.dialHundreds = 0;
        this.dialTensUnits = 0;
        this.completed = false;
    }

    /**
     * Ajuste la molette des milliers
     */
    public void setDialThousands(int value) {
        this.dialThousands = Math.max(0, Math.min(9, value));
    }

    /**
     * Ajuste la molette des centaines
     */
    public void setDialHundreds(int value) {
        this.dialHundreds = Math.max(0, Math.min(9, value));
    }

    /**
     * Ajuste la molette des dizaines+unités
     */
    public void setDialTensUnits(int value) {
        this.dialTensUnits = Math.max(0, Math.min(99, value));
    }

    /**
     * Retourne la fréquence actuellement réglée
     */
    public int getCurrentFrequency() {
        return dialThousands * 1000 + dialHundreds * 100 + dialTensUnits;
    }

    /**
     * Retourne la force du signal (0-100)
     * Plus on est proche de la cible, plus le signal est fort.
     */
    public int getSignalStrength() {
        int distance = Math.abs(getCurrentFrequency() - TARGET_FREQUENCY);
        return Math.max(0, 100 - (distance / 5));
    }

    /**
     * Retourne la distance à la fréquence cible
     */
    public int getDistanceToTarget() {
        return Math.abs(getCurrentFrequency() - TARGET_FREQUENCY);
    }

    /**
     * Vérifie si la fréquence est correcte
     */
    public boolean validate() {
        if (getCurrentFrequency() == TARGET_FREQUENCY) {
            completed = true;
            return true;
        }
        return false;
    }

    // Getters pour l'UI
    public int getDialThousands() { return dialThousands; }
    public int getDialHundreds() { return dialHundreds; }
    public int getDialTensUnits() { return dialTensUnits; }

    // ===== Interface MiniGame =====

    @Override
    public String getId() { return GAME_ID; }

    @Override
    public String getName() { return "Transmission radio"; }

    @Override
    public String getInstructions() {
        return "Réglez les molettes pour capter la bonne fréquence.\n" +
               "L'oscilloscope indique la force du signal.\n" +
               "Indice : consultez le journal du Dr. Vasquez (carte 11).";
    }

    @Override
    public boolean isCompleted() { return completed; }

    @Override
    public void reset() {
        dialThousands = 1;
        dialHundreds = 0;
        dialTensUnits = 0;
        completed = false;
    }
}
