package com.unlock.gui.javafx;

import com.unlock.model.Card;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue graphique d'une Carte (JavaFX).
 */
public class CardView extends VBox {

    // Événement personnalisé déclenché lors d'un double-clic sur la carte
    public static final EventType<Event> CARD_SELECTED = new EventType<>(Event.ANY, "CARD_SELECTED");

    private final Card card;

    // Style de base conservé pour le hover (évite la corruption du style CSS)
    private final String baseStyle;

    public CardView(Card card) {
        this.card = card;

        // Style de base de la carte (120x180 pixels)
        this.setPrefSize(120, 180);
        this.setPadding(new Insets(10));
        this.setSpacing(5);
        this.baseStyle =
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: #888888;" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 2, 2);";
        this.setStyle(baseStyle);

        // Couleur selon le type de carte
        String colorHex;
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
            case CODE:
                colorHex = "#FF9800";
                break;
            case PENALITE:
                colorHex = "#9C27B0";
                break;
            case NEUTRE:
            default:
                colorHex = "#9E9E9E";
                break;
        }

        // Label du Numéro (ID)
        Label idLabel = new Label(String.valueOf(card.getId()));
        idLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        idLabel.setTextFill(Color.web(colorHex));
        idLabel.setMaxWidth(Double.MAX_VALUE);
        idLabel.setStyle("-fx-alignment: center;");

        // Zone centrale : Image ou Texte
        if (card.getImagePath() != null && !card.getImagePath().isEmpty()) {
            try {
                String fullPath = "/com/unlock/resources/images/" + card.getImagePath();
                Image img = new Image(getClass().getResourceAsStream(fullPath));
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(100);
                imgView.setFitHeight(115);
                imgView.setPreserveRatio(true);
                this.getChildren().addAll(idLabel, imgView);
            } catch (Exception ex) {
                // Fallback texte si image introuvable
                Label descLabel = new Label(card.getDescription() + "\n(Image indispo)");
                descLabel.setWrapText(true);
                descLabel.setFont(Font.font("Arial", 11));
                descLabel.setStyle("-fx-alignment: top-center;");
                this.getChildren().addAll(idLabel, descLabel);
            }
        } else {
            Label descLabel = new Label(card.getDescription());
            descLabel.setWrapText(true);
            descLabel.setFont(Font.font("Arial", 12));
            descLabel.setMaxWidth(Double.MAX_VALUE);
            descLabel.setMaxHeight(Double.MAX_VALUE);
            descLabel.setStyle("-fx-alignment: top-center;");
            this.getChildren().addAll(idLabel, descLabel);
        }

        final String finalColorHex = colorHex;

        // Interaction Hover : utilise baseStyle pour éviter la corruption du CSS
        this.setOnMouseEntered(e -> this.setStyle(
                baseStyle +
                "-fx-border-color: " + finalColorHex + ";" +
                "-fx-border-width: 3;"));
        this.setOnMouseExited(e -> this.setStyle(baseStyle));

        // Double-clic : déclenche l'événement CARD_SELECTED (capté dans TableJeuController)
        this.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                this.fireEvent(new Event(CARD_SELECTED));
            }
        });
    }

    public Card getCard() {
        return card;
    }
}
