package com.unlock.model;

import java.io.Serializable;

public class Card {

    private int id; // Ex: 10, 6, 24, 30...
    private CardType type; // NEUTRE, BLEU, ROUGE...
    private String description; // acttion de la carte

    // Indique si la carte est

    public Card(int id, CardType type, String description) {
        this.id = id;

        this.description = description;
        this.isVisible = false; // Initialisation explicite pour la clarté
        this.imagePath = null; // Par défaut, pas d'image
    }
    

    public int getId() {
        return id;
    }

    public CardType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }


    
    public boolean isVisible(){
        return isVisible
        
    

    }

    public void setVisible(boolean vsible) {
    
        this.isVisible = visible ;   
    }
        
    

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}

