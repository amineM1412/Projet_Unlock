package com.unlock.model;

import java.io.Serializable;

/**
 * Modèle de données pour une Carte d'Unlock.
 * Implémente Serializable pour communiquer entre Swing et FX.
 */
public class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id; // Ex: 10, 6, 24, 30...
    private CardType type; // NEUTRE, BLEU, ROUGE...
    private String description;
    
    // Indique si la carte est visible sur la table (JavaFX)
    private boolean isVisible = false;
    private String imagePath; // Chemin vers l'image de la carte (ex: "resources/images/carte10.jpg")

    public Card(int id, CardType type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.isVisible = false; // Initialisation explicite pour la clarté
        this.imagePath = null; // Par défaut, pas d'image
    }

    public int getId() { return id; }
    public CardType getType() { return type; }
    public String getDescription() { return description; }
    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { this.isVisible = visible; }

    @Override
    public String toString() {
        return "[" + id + " " + type + "] " + description;
    }
}
