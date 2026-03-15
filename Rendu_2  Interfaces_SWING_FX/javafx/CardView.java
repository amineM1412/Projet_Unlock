package com.unlock.gui.javafx;

import com.unlock.model.Card;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue graphique d'une Carte
 */
public class CardView {

    private Card card;

    public CardView(Card card) {
        this.card = card;

        // Style de base de la carte (100x150 pixels)
        this.setPrefSize(120, 180);
        this.setPadding(new Insets(10));
        this.setSpacing(5);
        this.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #888888; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 2, 2);");

        // Application de la couleur d'en-tête selon le type (Rouge/Bleu/Neutre/Machine)
        String colorHex = "#9E9E9E"; // Gris neutre
        switch (card.getType()) {
            case ROUGE:
                colorHex = "#F44336";
                break;
            case BLEU:
                colorHex = "#2196F3";
                break;
            case MACHINE:
                colorHex = "#4CAF50";
                break;
            case RESULTAT:
                colorHex = "#FFC107";
                break;
            default:
                break;
        }

    public Card getCard() {
        return card;
    }
}
}
