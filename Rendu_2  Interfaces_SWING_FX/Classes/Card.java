package com.unlock.model;

public class Card {

    private int id; // Ex: 10, 6, 24, 30...
    private CardType type; // NEUTRE, BLEU, ROUGE...
    private String description; // acttion de la carte

    // Indique si la carte est

    public Card(int id, CardType type, String description) {
        this.id = id;

        this.description = desc

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
    }ic boolean i

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

