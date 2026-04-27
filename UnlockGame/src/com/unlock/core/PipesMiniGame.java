package com.unlock.core;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Mini-jeu 3 : Rotation de tuyaux (Carte 16)
 * 
 * Grille 4x4 de tuiles contenant des segments de tuyau.
 * Le joueur clique sur les tuiles pour les tourner de 90°.
 * Objectif : connecter l'entrée (haut-gauche) à la sortie (bas-droite).
 * 
 * Types de tuiles :
 * 0 = Vide (pas de connexion)
 * 1 = Droit horizontal (━) — connecte gauche↔droite
 * 2 = Coude (┓) — connecte haut↔droite (tourne pour d'autres orientations)
 * 3 = T (┳) — connecte 3 directions
 */
public class PipesMiniGame implements MiniGame {

    private static final long serialVersionUID = 1L;

    public static final String GAME_ID = "pipes";
    public static final int GRID_SIZE = 4;

    // Types de tuile
    public static final int EMPTY = 0;
    public static final int STRAIGHT = 1;  // ━ (horizontal par défaut)
    public static final int CURVE = 2;     // ┓ (coude)
    public static final int T_PIECE = 3;   // ┳ (T)

    // Grille : types de tuiles
    private int[][] tileTypes;
    // Grille : rotations actuelles (0=0°, 1=90°, 2=180°, 3=270°)
    private int[][] rotations;
    // Rotations correctes (solution)
    private int[][] solutionRotations;
    private boolean completed;

    public PipesMiniGame() {
        this.completed = false;
        initPuzzle();
    }

    /**
     * Initialise un puzzle prédéfini avec un chemin valide
     * Chemin : (0,0) → (0,1) → (0,2) → (0,3) → (1,3) → (2,3) → (2,2) → (2,1) → (3,1) → (3,2) → (3,3)
     */
    private void initPuzzle() {
        tileTypes = new int[][]{
            {CURVE,    STRAIGHT, STRAIGHT, CURVE},
            {EMPTY,    CURVE,    EMPTY,    STRAIGHT},
            {EMPTY,    CURVE,    STRAIGHT, CURVE},
            {EMPTY,    CURVE,    STRAIGHT, CURVE}
        };

        // Solution (rotations correctes pour que le chemin fonctionne)
        solutionRotations = new int[][]{
            {1, 0, 0, 2},  // (0,0)=┗ (0,1)=━ (0,2)=━ (0,3)=┓
            {0, 0, 0, 1},  // (1,3)=┃ (vertical)
            {0, 3, 0, 0},  // (2,1)=┛ (2,2)=━ (2,3)=┗
            {0, 2, 0, 3}   // (3,1)=┏ (3,2)=━ (3,3)=┛
        };

        // Rotations initiales (mélangées)
        rotations = new int[][]{
            {3, 2, 1, 0},
            {0, 2, 0, 3},
            {0, 1, 2, 2},
            {0, 0, 2, 1}
        };
    }

    /**
     * Tourne une tuile de 90° dans le sens horaire
     */
    public void rotateTile(int row, int col) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            if (tileTypes[row][col] != EMPTY) {
                rotations[row][col] = (rotations[row][col] + 1) % 4;
            }
        }
    }

    /**
     * Retourne les directions ouvertes d'une tuile selon son type et sa rotation
     * Directions : 0=haut, 1=droite, 2=bas, 3=gauche
     */
    public boolean[] getOpenDirections(int row, int col) {
        boolean[] dirs = new boolean[4]; // haut, droite, bas, gauche
        int type = tileTypes[row][col];
        int rot = rotations[row][col];

        if (type == EMPTY) return dirs;

        if (type == STRAIGHT) {
            // Horizontal par défaut : gauche↔droite
            // rot 0: gauche↔droite, rot 1: haut↔bas
            dirs[(1 + rot) % 4] = true;  // droite (puis bas, gauche, haut)
            dirs[(3 + rot) % 4] = true;  // gauche (puis haut, droite, bas)
        } else if (type == CURVE) {
            // Coude par défaut : haut↔droite
            // rot 0: haut+droite, rot 1: droite+bas, rot 2: bas+gauche, rot 3: gauche+haut
            dirs[(0 + rot) % 4] = true;   // haut (puis droite, bas, gauche)
            dirs[(1 + rot) % 4] = true;   // droite (puis bas, gauche, haut)
        } else if (type == T_PIECE) {
            // T par défaut : haut+droite+bas (ouvert sauf gauche)
            dirs[(0 + rot) % 4] = true;
            dirs[(1 + rot) % 4] = true;
            dirs[(2 + rot) % 4] = true;
        }

        return dirs;
    }

    /**
     * Vérifie si deux tuiles adjacentes sont connectées
     */
    private boolean areConnected(int r1, int c1, int r2, int c2) {
        if (r2 < 0 || r2 >= GRID_SIZE || c2 < 0 || c2 >= GRID_SIZE) return false;
        if (tileTypes[r1][c1] == EMPTY || tileTypes[r2][c2] == EMPTY) return false;

        boolean[] dirs1 = getOpenDirections(r1, c1);
        boolean[] dirs2 = getOpenDirections(r2, c2);

        // Déterminer la direction de (r1,c1) vers (r2,c2)
        if (r2 == r1 - 1) return dirs1[0] && dirs2[2]; // haut
        if (c2 == c1 + 1) return dirs1[1] && dirs2[3]; // droite
        if (r2 == r1 + 1) return dirs1[2] && dirs2[0]; // bas
        if (c2 == c1 - 1) return dirs1[3] && dirs2[1]; // gauche

        return false;
    }

    /**
     * Vérifie la connexion entrée→sortie par BFS
     * Entrée : (0,0) en venant de la gauche
     * Sortie : (3,3) sortant par la droite
     */
    public boolean checkConnection() {
        if (tileTypes[0][0] == EMPTY || tileTypes[GRID_SIZE - 1][GRID_SIZE - 1] == EMPTY) {
            return false;
        }

        // Vérifier que l'entrée (0,0) a une ouverture à gauche et la sortie (3,3) à droite
        boolean[] startDirs = getOpenDirections(0, 0);
        boolean[] endDirs = getOpenDirections(GRID_SIZE - 1, GRID_SIZE - 1);
        if (!startDirs[3] && !startDirs[0]) return false; // entrée doit aller quelque part
        if (!endDirs[1] && !endDirs[2]) return false;    // sortie doit aller quelque part

        // BFS depuis (0,0)
        boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{0, 0});
        visited[0][0] = true;

        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}}; // haut, droite, bas, gauche

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0], c = current[1];

            if (r == GRID_SIZE - 1 && c == GRID_SIZE - 1) {
                return true;
            }

            for (int d = 0; d < 4; d++) {
                int nr = r + directions[d][0];
                int nc = c + directions[d][1];
                if (nr >= 0 && nr < GRID_SIZE && nc >= 0 && nc < GRID_SIZE
                        && !visited[nr][nc] && areConnected(r, c, nr, nc)) {
                    visited[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                }
            }
        }
        return false;
    }

    /**
     * Valide le mini-jeu (vérifie la connexion)
     */
    public boolean validate() {
        completed = checkConnection();
        return completed;
    }

    // Getters pour l'UI
    public int[][] getTileTypes() { return tileTypes; }
    public int[][] getRotations() { return rotations; }
    public int getTileType(int row, int col) { return tileTypes[row][col]; }
    public int getRotation(int row, int col) { return rotations[row][col]; }

    // ===== Interface MiniGame =====

    @Override
    public String getId() { return GAME_ID; }

    @Override
    public String getName() { return "Rotation de tuyaux"; }

    @Override
    public String getInstructions() {
        return "Cliquez sur les tuiles pour les tourner de 90°.\n" +
               "Connectez l'entrée (haut-gauche) à la sortie (bas-droite)\n" +
               "pour rétablir le circuit de refroidissement.";
    }

    @Override
    public boolean isCompleted() { return completed; }

    @Override
    public void reset() {
        completed = false;
        initPuzzle();
    }
}
