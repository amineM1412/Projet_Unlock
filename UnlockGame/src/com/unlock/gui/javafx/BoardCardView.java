package com.unlock.gui.javafx;

import com.unlock.model.Card;
import com.unlock.model.CardType;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Vue d'une carte sur le plateau de jeu.
 * Supporte l'animation de retournement (flip),
 * les etats face cachee / face visible,
 * et les effets de lueur selon le type.
 */
public class BoardCardView extends StackPane {

    public static final EventType<Event> CARD_SELECTED = new EventType<>(Event.ANY, "BOARD_CARD_SELECTED");

    private static final double CARD_WIDTH = 100;
    private static final double CARD_HEIGHT = 135;

    private final Card card;
    private final VBox frontFace;
    private final StackPane backFace;
    private boolean isFaceUp = false;

    // Couleurs par type
    private String colorHex;
    private Color glowColor;

    // Animation de lueur pulsante
    private Timeline glowTimeline;

    public BoardCardView(Card card) {
        this.card = card;
        this.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        this.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        this.setMinSize(CARD_WIDTH, CARD_HEIGHT);

        resolveColors();

        // --- FACE ARRIERE (dos de carte) ---
        backFace = createBackFace();

        // --- FACE AVANT (contenu) ---
        frontFace = createFrontFace();

        this.getChildren().addAll(frontFace, backFace);

        // Etat initial
        if (card.isVisible()) {
            showFrontImmediately();
        } else {
            showBackImmediately();
        }

        // Double-clic
        this.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && isFaceUp) {
                this.fireEvent(new Event(CARD_SELECTED));
            }
        });

        // Hover effect
        this.setOnMouseEntered(e -> {
            if (isFaceUp) {
                this.setScaleX(1.08);
                this.setScaleY(1.08);
                this.toFront();
            }
        });
        this.setOnMouseExited(e -> {
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });

        // Tooltip
        Tooltip tip = new Tooltip(card.getDescription());
        tip.setFont(Font.font("Arial", 11));
        tip.setWrapText(true);
        tip.setMaxWidth(250);
        Tooltip.install(this, tip);
    }

    /**
     * Determine les couleurs en fonction du type de carte
     */
    private void resolveColors() {
        switch (card.getType()) {
            case ROUGE:
                colorHex = "#E53935";
                glowColor = Color.web("#E53935", 0.7);
                break;
            case BLEU:
                colorHex = "#1E88E5";
                glowColor = Color.web("#1E88E5", 0.7);
                break;
            case MACHINE:
                colorHex = "#43A047";
                glowColor = Color.web("#43A047", 0.7);
                break;
            case CODE:
                colorHex = "#FFC107";
                glowColor = Color.web("#FFC107", 0.7);
                break;
            case RESULTAT:
                colorHex = "#FFD700";
                glowColor = Color.web("#FFD700", 0.7);
                break;
            case PENALITE:
                colorHex = "#9C27B0";
                glowColor = Color.web("#9C27B0", 0.7);
                break;
            case NEUTRE:
            default:
                colorHex = "#78909C";
                glowColor = Color.web("#78909C", 0.5);
                break;
        }
    }

    /**
     * Cree la face arriere (dos de carte)
     */
    private StackPane createBackFace() {
        StackPane back = new StackPane();
        back.setPrefSize(CARD_WIDTH, CARD_HEIGHT);

        Rectangle bg = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        bg.setArcWidth(12);
        bg.setArcHeight(12);
        bg.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a1a2e")),
                new Stop(0.5, Color.web("#16213e")),
                new Stop(1, Color.web("#0f3460"))
        ));
        bg.setStroke(Color.web("#334466"));
        bg.setStrokeWidth(2);

        // Numero de la carte
        Label numLabel = new Label(String.valueOf(card.getId()));
        numLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        numLabel.setTextFill(Color.web("#4fc3f7"));

        // Point d'interrogation
        Label questionMark = new Label("?");
        questionMark.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        questionMark.setTextFill(Color.web("#4fc3f7", 0.4));
        StackPane.setAlignment(questionMark, Pos.BOTTOM_CENTER);
        questionMark.setPadding(new Insets(0, 0, 10, 0));

        back.getChildren().addAll(bg, numLabel, questionMark);
        return back;
    }

    /**
     * Cree la face avant (contenu de la carte)
     */
    private VBox createFrontFace() {
        VBox front = new VBox(3);
        front.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        front.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        front.setPadding(new Insets(6));
        front.setAlignment(Pos.TOP_CENTER);

        String bgGradient;
        switch (card.getType()) {
            case ROUGE:  bgGradient = "linear-gradient(to bottom, #ffebee, #ffffff)"; break;
            case BLEU:   bgGradient = "linear-gradient(to bottom, #e3f2fd, #ffffff)"; break;
            case MACHINE: bgGradient = "linear-gradient(to bottom, #e8f5e9, #ffffff)"; break;
            case CODE:   bgGradient = "linear-gradient(to bottom, #fff8e1, #ffffff)"; break;
            case RESULTAT: bgGradient = "linear-gradient(to bottom, #fffde7, #fff9c4)"; break;
            case PENALITE: bgGradient = "linear-gradient(to bottom, #f3e5f5, #ffffff)"; break;
            default:     bgGradient = "linear-gradient(to bottom, #eceff1, #ffffff)"; break;
        }

        front.setStyle(
                "-fx-background-color: " + bgGradient + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: " + colorHex + ";" +
                "-fx-border-width: 2;"
        );

        // ID
        Label idLabel = new Label(String.valueOf(card.getId()));
        idLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        idLabel.setTextFill(Color.web(colorHex));
        idLabel.setMaxWidth(Double.MAX_VALUE);
        idLabel.setAlignment(Pos.CENTER);

        // Badge type
        Label typeLabel = new Label(getTypeTag(card.getType()));
        typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        typeLabel.setTextFill(Color.web("#666666"));
        typeLabel.setMaxWidth(Double.MAX_VALUE);
        typeLabel.setAlignment(Pos.CENTER);

        // Description (courte)
        String desc = card.getDescription();
        if (desc.contains("\u2014")) {
            desc = desc.substring(0, desc.indexOf("\u2014")).trim();
        }
        if (desc.length() > 40) {
            desc = desc.substring(0, 37) + "...";
        }
        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font("Arial", 9));
        descLabel.setTextFill(Color.web("#333333"));
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(CARD_WIDTH - 12);
        descLabel.setMaxHeight(60);

        front.getChildren().addAll(idLabel, typeLabel, descLabel);

        // Drop shadow
        DropShadow ds = new DropShadow();
        ds.setRadius(6);
        ds.setOffsetX(2);
        ds.setOffsetY(2);
        ds.setColor(Color.rgb(0, 0, 0, 0.35));
        front.setEffect(ds);

        return front;
    }

    /**
     * Anime le retournement de la carte (face cachee -> face visible)
     */
    public void flipToFront() {
        if (isFaceUp) return;

        ScaleTransition hideBack = new ScaleTransition(Duration.millis(250), this);
        hideBack.setFromX(1.0);
        hideBack.setToX(0.0);
        hideBack.setOnFinished(e -> {
            backFace.setVisible(false);
            frontFace.setVisible(true);
            isFaceUp = true;

            ScaleTransition showFront = new ScaleTransition(Duration.millis(250), this);
            showFront.setFromX(0.0);
            showFront.setToX(1.0);
            showFront.setOnFinished(ev -> startGlowIfNeeded());
            showFront.play();
        });
        hideBack.play();
    }

    /**
     * Affiche directement la face avant (sans animation)
     */
    public void showFrontImmediately() {
        backFace.setVisible(false);
        frontFace.setVisible(true);
        isFaceUp = true;
        startGlowIfNeeded();
    }

    /**
     * Affiche directement la face arriere (sans animation)
     */
    public void showBackImmediately() {
        frontFace.setVisible(false);
        backFace.setVisible(true);
        isFaceUp = false;
    }

    /**
     * Demarre la lueur pulsante pour les cartes interactives (MACHINE, CODE)
     */
    private void startGlowIfNeeded() {
        if (card.getType() == CardType.MACHINE || card.getType() == CardType.CODE) {
            Glow glow = new Glow(0.0);
            frontFace.setEffect(glow);

            glowTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.0)),
                new KeyFrame(Duration.millis(1200), new KeyValue(glow.levelProperty(), 0.5)),
                new KeyFrame(Duration.millis(2400), new KeyValue(glow.levelProperty(), 0.0))
            );
            glowTimeline.setCycleCount(Timeline.INDEFINITE);
            glowTimeline.play();
        }
    }

    /**
     * Arrete les animations
     */
    public void stopAnimations() {
        if (glowTimeline != null) {
            glowTimeline.stop();
        }
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

    public boolean isFaceUp() {
        return isFaceUp;
    }

    public String getColorHex() {
        return colorHex;
    }
}
