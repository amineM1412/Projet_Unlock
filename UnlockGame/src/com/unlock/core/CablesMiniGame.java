package com.unlock.core;

/**
 * Mini-jeu 1 : Relier les câbles (Carte 29)
 * 
 * Le joueur doit connecter 4 câbles de couleur aux bons connecteurs.
 * Ordre correct : Rouge→1, Jaune→2, Bleu→3, Vert→4
 * (Indice donné par la carte 36 — Schéma de câblage)
 */
public class CablesMiniGame implements MiniGame {

    private static final long serialVersionUID = 1L;

    public static final String GAME_ID = "cables";
    public static final String[] CABLE_COLORS = {"Rouge", "Jaune", "Bleu", "Vert"};
    public static final int NUM_CABLES = 4;

    // Solution : câble 0 (Rouge)→connecteur 0, câble 1 (Jaune)→connecteur 1, etc.
    private final int[] solution = {0, 1, 2, 3};

    // Réponse actuelle du joueur (-1 = non connecté)
    private int[] playerConnections;
    private boolean completed;

    public CablesMiniGame() {
        this.playerConnections = new int[]{-1, -1, -1, -1};
        this.completed = false;
    }

    /**
     * Connecte un câble à un connecteur
     * @param cableIndex index du câble (0=Rouge, 1=Jaune, 2=Bleu, 3=Vert)
     * @param connectorIndex index du connecteur (0-3)
     */
    public void connect(int cableIndex, int connectorIndex) {
        if (cableIndex >= 0 && cableIndex < NUM_CABLES &&
            connectorIndex >= 0 && connectorIndex < NUM_CABLES) {

            // Déconnecter tout câble déjà sur ce connecteur
            for (int i = 0; i < NUM_CABLES; i++) {
                if (playerConnections[i] == connectorIndex) {
                    playerConnections[i] = -1;
                }
            }
            playerConnections[cableIndex] = connectorIndex;
        }
    }

    /**
     * Déconnecte un câble
     */
    public void disconnect(int cableIndex) {
        if (cableIndex >= 0 && cableIndex < NUM_CABLES) {
            playerConnections[cableIndex] = -1;
        }
    }

    /**
     * Vérifie si toutes les connexions sont correctes
     * @return true si le mini-jeu est résolu
     */
    public boolean validate() {
        for (int i = 0; i < NUM_CABLES; i++) {
            if (playerConnections[i] != solution[i]) {
                return false;
            }
        }
        completed = true;
        return true;
    }

    /**
     * Retourne le nombre de connexions correctes (pour feedback partiel)
     */
    public int getCorrectCount() {
        int count = 0;
        for (int i = 0; i < NUM_CABLES; i++) {
            if (playerConnections[i] == solution[i]) count++;
        }
        return count;
    }

    public int[] getPlayerConnections() {
        return playerConnections.clone();
    }

    // ===== Interface MiniGame =====

    @Override
    public String getId() { return GAME_ID; }

    @Override
    public String getName() { return "Relier les câbles"; }

    @Override
    public String getInstructions() {
        return "Connectez chaque câble de couleur au bon connecteur.\n" +
               "Indice : consultez le schéma de câblage (carte 36).";
    }

    @Override
    public boolean isCompleted() { return completed; }

    @Override
    public void reset() {
        playerConnections = new int[]{-1, -1, -1, -1};
        completed = false;
    }
}
