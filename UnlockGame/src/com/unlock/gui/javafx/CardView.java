package com.unlock.gui.javafx;

import com.unlock.model.Card;
import com.unlock.model.CardType;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue graphique d'une Carte (JavaFX).
 * Supporte tous les types de cartes avec les bonnes couleurs.
 */
public class CardView extends VBox {

    public static final EventType<Event> CARD_SELECTED = new EventType<>(Event.ANY, "CARD_SELECTED");

    private final Card card;
    private final String baseStyle;

    public CardView(Card card) {
        this.card = card;

        this.setPrefSize(130, 190);
        this.setPadding(new Insets(8));
        this.setSpacing(4);
        this.setAlignment(Pos.TOP_CENTER);

        String colorHex;
        String bgGradient;
        switch (card.getType()) {
            case ROUGE:
                colorHex = "#E53935";
                bgGradient = "linear-gradient(to bottom, #ffebee, #ffffff)";
                break;
            case BLEU:
                colorHex = "#1E88E5";
                bgGradient = "linear-gradient(to bottom, #e3f2fd, #ffffff)";
                break;
            case MACHINE:
                colorHex = "#43A047";
                bgGradient = "linear-gradient(to bottom, #e8f5e9, #ffffff)";
                break;
            case CODE:
                colorHex = "#FFC107";
                bgGradient = "linear-gradient(to bottom, #fff8e1, #ffffff)";
                break;
            case RESULTAT:
                colorHex = "#FFD700";
                bgGradient = "linear-gradient(to bottom, #fffde7, #fff9c4)";
                break;
            case PENALITE:
                colorHex = "#9C27B0";
                bgGradient = "linear-gradient(to bottom, #f3e5f5, #ffffff)";
                break;
            case NEUTRE:
            default:
                colorHex = "#78909C";
                bgGradient = "linear-gradient(to bottom, #eceff1, #ffffff)";
                break;
        }

        this.baseStyle =
                "-fx-background-color: " + bgGradient + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: " + colorHex + ";" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 2, 2);";
        this.setStyle(baseStyle);

        // Label du Numero (ID)
        Label idLabel = new Label(String.valueOf(card.getId()));
        idLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        idLabel.setTextFill(Color.web(colorHex));
        idLabel.setMaxWidth(Double.MAX_VALUE);
        idLabel.setAlignment(Pos.CENTER);

        // Badge du type
        String typeTag = getTypeTag(card.getType());
        Label typeLabel = new Label(typeTag);
        typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        typeLabel.setTextFill(Color.web("#666666"));
        typeLabel.setMaxWidth(Double.MAX_VALUE);
        typeLabel.setAlignment(Pos.CENTER);

        // Zone centrale : Image ou Texte
        if (card.getImagePath() != null && !card.getImagePath().isEmpty()) {
            try {
                String fullPath = "/com/unlock/resources/images/" + card.getImagePath();
                Image img = new Image(getClass().getResourceAsStream(fullPath));
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(110);
                imgView.setFitHeight(110);
                imgView.setPreserveRatio(true);
                this.getChildren().addAll(idLabel, typeLabel, imgView);
            } catch (Exception ex) {
                Label descLabel = createDescriptionLabel(card.getDescription());
                this.getChildren().addAll(idLabel, typeLabel, descLabel);
            }
        } else {
            Label descLabel = createDescriptionLabel(card.getDescription());
            this.getChildren().addAll(idLabel, typeLabel, descLabel);
        }

        final String finalColorHex = colorHex;

        // Hover effect
        this.setOnMouseEntered(e -> this.setStyle(
                baseStyle +
                "-fx-border-color: " + finalColorHex + ";" +
                "-fx-border-width: 3;" +
                "-fx-effect: dropshadow(three-pass-box, " + finalColorHex + "44, 10, 0, 0, 0);"));
        this.setOnMouseExited(e -> this.setStyle(baseStyle));

        // Double-clic : CARD_SELECTED
        this.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                this.fireEvent(new Event(CARD_SELECTED));
            }
        });
    }

    private Label createDescriptionLabel(String text) {
        String displayText = text;
        if (text.contains("\u2014")) {
            displayText = text.substring(text.indexOf("\u2014") + 1).trim();
        }
        if (displayText.length() > 60) {
            displayText = displayText.substring(0, 57) + "...";
        }

        Label descLabel = new Label(displayText);
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font("Arial", 10));
        descLabel.setMaxWidth(Double.MAX_VALUE);
        descLabel.setMaxHeight(Double.MAX_VALUE);
        descLabel.setAlignment(Pos.TOP_CENTER);
        descLabel.setStyle("-fx-text-fill: #333333;");
        return descLabel;
    }

    private String getTypeTag(CardType type) {
        switch (type) {
            case BLEU: return "[BLEU] Objet";
            case ROUGE: return "[ROUGE] Objet";
            case MACHINE: return "[VERT] Machine";
            case CODE: return "[JAUNE] Code";
            case RESULTAT: return "[RESULTAT]";
            case PENALITE: return "[PENALITE]";
            default: return "[GRIS] Lieu";
        }
    }

    public Card getCard() {
        return card;
    }
}
