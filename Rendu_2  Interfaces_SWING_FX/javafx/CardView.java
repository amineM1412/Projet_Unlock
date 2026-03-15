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
 * Vue graphique d'une Carte (en JavaFX).
 */
public class CardView extends VBox {

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

        // Label du Numéro (ID)
        Label idLabel = new Label(String.valueOf(card.getId()));
        idLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        idLabel.setTextFill(Color.web(colorHex));
        idLabel.setMaxWidth(Double.MAX_VALUE);
        idLabel.setStyle("-fx-alignment: center;");

        // Zone centrale : Image ou Texte
        if (card.getImagePath() != null && !card.getImagePath().isEmpty()) {
            try {
                // On utilise le classloader pour charger l'image depuis les ressources
                String fullPath = "/com/unlock/resources/images/" + card.getImagePath();
                Image img = new Image(getClass().getResourceAsStream(fullPath));
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(100);
                imgView.setFitHeight(115);
                imgView.setPreserveRatio(true);
                this.getChildren().addAll(idLabel, imgView);
            } catch (Exception ex) {
                // Fallback texte si introuvable
                Label descLabel = new Label(card.getDescription() + "\n(Image indispo)");
                descLabel.setWrapText(true);
                descLabel.setFont(Font.font("System", 11));
                descLabel.setStyle("-fx-alignment: top-center;");
                this.getChildren().addAll(idLabel, descLabel);
            }
        } else {
            // Label pour le texte/description basique
            Label descLabel = new Label(card.getDescription());
            descLabel.setWrapText(true);
            descLabel.setFont(Font.font("System", 12));
            descLabel.setMaxWidth(Double.MAX_VALUE);
            descLabel.setMaxHeight(Double.MAX_VALUE);
            descLabel.setStyle("-fx-alignment: top-center;");
            this.getChildren().addAll(idLabel, descLabel);
        }

        final String finalColorHex = colorHex;

        // Interaction visuelle (Hover)
        this.setOnMouseEntered(e -> this.setStyle(this.getStyle() + "-fx-border-color: " + finalColorHex + "; -fx-border-width: 3;"));
        this.setOnMouseExited(e -> this.setStyle(this.getStyle().replace("-fx-border-color: " + finalColorHex + "; -fx-border-width: 3;", "")));

        // Action de Fouille (Double-clic pour observer un lieu ou chercher un numéro caché)
        this.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                // Émettre un événement personnalisé qu'on capte dans TableJeuController
                this.fireEvent(new javafx.event.Event(javafx.event.EventType.ROOT));
            }
        });
    }

    public Card getCard() {
        return card;
    }
}
