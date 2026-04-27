package com.unlock.model;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * État sérialisable complet de la partie Unlock!
 * Permet la sauvegarde et le chargement de l'état du jeu.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SAVE_FILE = "unlock_save.dat";

    private HashMap<Integer, Card> deck;
    private Inventory inventory;
    private int oxygenRemaining;         // Secondes d'oxygène restantes
    private int timeRemaining;           // Timer classique (secondes)
    private int currentAct;              // Acte actuel (1, 2, 3)
    private Set<String> completedMiniGames; // IDs des mini-jeux terminés
    private boolean gameWon;
    private boolean gameLost;

    public GameState() {
        this.deck = new HashMap<>();
        this.inventory = new Inventory();
        this.oxygenRemaining = 2400;     // 40 minutes
        this.timeRemaining = 3600;       // 60 minutes
        this.currentAct = 1;
        this.completedMiniGames = new HashSet<>();
        this.gameWon = false;
        this.gameLost = false;
    }

    // ========== DECK ==========

    public HashMap<Integer, Card> getDeck() {
        return deck;
    }

    public void setDeck(HashMap<Integer, Card> deck) {
        this.deck = deck;
    }

    public Card getCard(int id) {
        return deck.get(id);
    }

    public void addCard(int id, Card card) {
        deck.put(id, card);
    }

    // ========== INVENTAIRE ==========

    public Inventory getInventory() {
        return inventory;
    }

    // ========== OXYGÈNE ==========

    public int getOxygenRemaining() {
        return oxygenRemaining;
    }

    public void setOxygenRemaining(int oxygenRemaining) {
        this.oxygenRemaining = Math.max(0, oxygenRemaining);
    }

    public void reduceOxygen(int seconds) {
        this.oxygenRemaining = Math.max(0, this.oxygenRemaining - seconds);
    }

    public boolean isOxygenDepleted() {
        return oxygenRemaining <= 0;
    }

    /**
     * Retourne le pourcentage d'oxygène restant (0–100)
     */
    public int getOxygenPercentage() {
        return (int) ((oxygenRemaining / 2400.0) * 100);
    }

    // ========== TIMER ==========

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = Math.max(0, timeRemaining);
    }

    public void reduceTime(int seconds) {
        this.timeRemaining = Math.max(0, this.timeRemaining - seconds);
    }

    // ========== ACTE ==========

    public int getCurrentAct() {
        return currentAct;
    }

    public void setCurrentAct(int currentAct) {
        this.currentAct = currentAct;
    }

    // ========== MINI-JEUX ==========

    public boolean isMiniGameCompleted(String miniGameId) {
        return completedMiniGames.contains(miniGameId);
    }

    public void completeMiniGame(String miniGameId) {
        completedMiniGames.add(miniGameId);
    }

    public Set<String> getCompletedMiniGames() {
        return completedMiniGames;
    }

    // ========== ÉTAT DE JEU ==========

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public boolean isGameLost() {
        return gameLost;
    }

    public void setGameLost(boolean gameLost) {
        this.gameLost = gameLost;
    }

    public boolean isGameOver() {
        return gameWon || gameLost;
    }

    // ========== SAUVEGARDE / CHARGEMENT ==========

    public void saveToFile(String directory) throws IOException {
        File file = new File(directory, SAVE_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    public static GameState loadFromFile(String directory) throws IOException, ClassNotFoundException {
        File file = new File(directory, SAVE_FILE);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameState) ois.readObject();
        }
    }

    public static boolean saveExists(String directory) {
        return new File(directory, SAVE_FILE).exists();
    }
    
}
