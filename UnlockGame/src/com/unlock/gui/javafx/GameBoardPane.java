package com.unlock.gui.javafx;

import com.unlock.model.Card;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;

/**
 * Plateau de jeu visuel pour Unlock! - "Signal Fantome"
 * Affiche les cartes dans 3 zones correspondant aux 3 actes
 * de la station spatiale, avec un fond spatial etoile.
 */
public class GameBoardPane extends Pane {

    // Dimensions du plateau
    public static final double BOARD_WIDTH = 1050;
    public static final double BOARD_HEIGHT = 550;

    // Zones de jeu (positions)
    private static final double ZONE_WIDTH = 300;
    private static final double ZONE_HEIGHT = 420;
    private static final double ZONE_Y = 70;
    private static final double ZONE1_X = 25;
    private static final double ZONE2_X = 365;
    private static final double ZONE3_X = 705;

    // Stockage des vues de cartes par ID
    private Map<Integer, BoardCardView> cardViews = new HashMap<>();

    // Positions precalculees pour chaque carte dans sa zone
    private Map<Integer, double[]> cardPositions = new HashMap<>();

    // Canvas pour le fond spatial
    private Canvas backgroundCanvas;

    // Rectangles de zones
    private Rectangle zone1Rect, zone2Rect, zone3Rect;
    private Label zone1Title, zone2Title, zone3Title;

    // Etoiles animees
    private List<Circle> stars = new ArrayList<>();
    private Timeline starsTimeline;

    public GameBoardPane() {
        this.setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        this.setMinSize(BOARD_WIDTH, BOARD_HEIGHT);

        // Fond spatial
        drawBackground();

        // Etoiles scintillantes
        createStars();

        // 3 zones de jeu
        createZones();

        // Fleches entre les zones
        createConnections();

        // Calculer les positions des cartes
        calculateCardPositions();
    }

    // ================================================================
    //  FOND SPATIAL
    // ================================================================

    private void drawBackground() {
        backgroundCanvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();

        // Gradient de fond : espace profond
        gc.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0a1a")),
                new Stop(0.3, Color.web("#0d1b2a")),
                new Stop(0.6, Color.web("#1b2838")),
                new Stop(1.0, Color.web("#0a0e17"))
        ));
        gc.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Nebuleuse subtile (halos de couleur)
        gc.setFill(new RadialGradient(0, 0, 150, 200, 200, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a237e", 0.15)),
                new Stop(1, Color.TRANSPARENT)
        ));
        gc.fillOval(50, 100, 400, 400);

        gc.setFill(new RadialGradient(0, 0, 750, 150, 200, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a148c", 0.1)),
                new Stop(1, Color.TRANSPARENT)
        ));
        gc.fillOval(600, 50, 350, 350);

        gc.setFill(new RadialGradient(0, 0, 500, 400, 150, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#006064", 0.1)),
                new Stop(1, Color.TRANSPARENT)
        ));
        gc.fillOval(400, 300, 300, 300);

        this.getChildren().add(backgroundCanvas);
    }

    // ================================================================
    //  ETOILES SCINTILLANTES
    // ================================================================

    private void createStars() {
        Random rand = new Random(42); // seed fixe pour reproductibilite
        for (int i = 0; i < 80; i++) {
            double x = rand.nextDouble() * BOARD_WIDTH;
            double y = rand.nextDouble() * BOARD_HEIGHT;
            double size = 0.5 + rand.nextDouble() * 1.5;

            Circle star = new Circle(x, y, size);
            star.setFill(Color.web("#ffffff", 0.3 + rand.nextDouble() * 0.5));
            stars.add(star);
            this.getChildren().add(star);
        }

        // Animation de scintillement
        starsTimeline = new Timeline();
        for (int i = 0; i < stars.size(); i++) {
            Circle star = stars.get(i);
            double delay = new Random().nextDouble() * 4000;
            starsTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(delay),
                    new KeyValue(star.opacityProperty(), star.getOpacity())),
                new KeyFrame(Duration.millis(delay + 1500),
                    new KeyValue(star.opacityProperty(), 0.1)),
                new KeyFrame(Duration.millis(delay + 3000),
                    new KeyValue(star.opacityProperty(), star.getOpacity()))
            );
        }
        starsTimeline.setCycleCount(Timeline.INDEFINITE);
        starsTimeline.play();
    }

    // ================================================================
    //  ZONES DE JEU
    // ================================================================

    private void createZones() {
        // Zone 1 - Sas de Decompression
        zone1Rect = createZoneRectangle(ZONE1_X, ZONE_Y, ZONE_WIDTH, ZONE_HEIGHT,
                "#E53935", "Acte 1");
        zone1Title = createZoneTitle("SAS DE DECOMPRESSION", ZONE1_X, ZONE_Y - 5, ZONE_WIDTH,
                "#E53935");

        // Zone 2 - Laboratoire & Communications
        zone2Rect = createZoneRectangle(ZONE2_X, ZONE_Y, ZONE_WIDTH, ZONE_HEIGHT,
                "#1E88E5", "Acte 2");
        zone2Title = createZoneTitle("LABORATOIRE & COMMS", ZONE2_X, ZONE_Y - 5, ZONE_WIDTH,
                "#1E88E5");

        // Zone 3 - Reacteur & Finale
        zone3Rect = createZoneRectangle(ZONE3_X, ZONE_Y, ZONE_WIDTH, ZONE_HEIGHT,
                "#43A047", "Acte 3");
        zone3Title = createZoneTitle("REACTEUR & FINALE", ZONE3_X, ZONE_Y - 5, ZONE_WIDTH,
                "#43A047");
    }

    private Rectangle createZoneRectangle(double x, double y, double w, double h,
                                           String colorHex, String actLabel) {
        Rectangle rect = new Rectangle(x, y, w, h);
        rect.setArcWidth(16);
        rect.setArcHeight(16);
        rect.setFill(Color.web(colorHex, 0.08));
        rect.setStroke(Color.web(colorHex, 0.35));
        rect.setStrokeWidth(2);

        DropShadow ds = new DropShadow();
        ds.setRadius(15);
        ds.setColor(Color.web(colorHex, 0.2));
        rect.setEffect(ds);

        this.getChildren().add(rect);

        // Label de l'acte (en haut a droite de la zone)
        Label actLbl = new Label(actLabel);
        actLbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        actLbl.setTextFill(Color.web(colorHex, 0.6));
        actLbl.setLayoutX(x + w - 50);
        actLbl.setLayoutY(y + 5);
        this.getChildren().add(actLbl);

        return rect;
    }

    private Label createZoneTitle(String title, double x, double y, double width,
                                   String colorHex) {
        Label lbl = new Label(title);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(colorHex, 0.8));
        lbl.setLayoutX(x + 10);
        lbl.setLayoutY(y + 8);
        lbl.setPrefWidth(width - 60);
        this.getChildren().add(lbl);
        return lbl;
    }

    // ================================================================
    //  CONNEXIONS VISUELLES (fleches entre zones)
    // ================================================================

    private void createConnections() {
        // Fleche Zone1 -> Zone2
        createArrow(ZONE1_X + ZONE_WIDTH + 5, ZONE_Y + ZONE_HEIGHT / 2,
                    ZONE2_X - 5, ZONE_Y + ZONE_HEIGHT / 2, "#4fc3f7");

        // Fleche Zone2 -> Zone3
        createArrow(ZONE2_X + ZONE_WIDTH + 5, ZONE_Y + ZONE_HEIGHT / 2,
                    ZONE3_X - 5, ZONE_Y + ZONE_HEIGHT / 2, "#4fc3f7");
    }

    private void createArrow(double x1, double y1, double x2, double y2, String colorHex) {
        // Ligne principale (pointillee)
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.web(colorHex, 0.5));
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(8.0, 4.0);

        // Pointe de fleche
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowSize = 10;
        Polygon arrowHead = new Polygon();
        arrowHead.getPoints().addAll(
            x2, y2,
            x2 - arrowSize * Math.cos(angle - Math.PI / 6),
            y2 - arrowSize * Math.sin(angle - Math.PI / 6),
            x2 - arrowSize * Math.cos(angle + Math.PI / 6),
            y2 - arrowSize * Math.sin(angle + Math.PI / 6)
        );
        arrowHead.setFill(Color.web(colorHex, 0.5));

        this.getChildren().addAll(line, arrowHead);
    }

    // ================================================================
    //  POSITIONS DES CARTES
    // ================================================================

    /**
     * Precalcule les positions de chaque carte dans sa zone.
     * Disposition en grille 3 colonnes x N lignes dans chaque zone.
     */
    private void calculateCardPositions() {
        // Padding a l'interieur des zones
        double padX = 12;
        double padY = 30;
        double cardW = 95;
        double cardH = 130;
        double gapX = 4;
        double gapY = 8;

        // ACTE 1 : Cartes 10, 12, 13, 6, 24, 30
        int[] act1Cards = {10, 12, 13, 6, 24, 30};
        layoutCardsInZone(act1Cards, ZONE1_X, ZONE_Y, padX, padY, cardW, cardH, gapX, gapY, 3);

        // ACTE 2 : Cartes 45, 14, 17, 8, 21, 7, 18, 11, 29, 25, 38, 33, 40, 27
        int[] act2Cards = {45, 14, 17, 8, 21, 7, 18, 11, 29, 25, 38, 33, 40, 27};
        layoutCardsInZone(act2Cards, ZONE2_X, ZONE_Y, padX, padY, cardW, cardH, gapX, gapY, 3);

        // ACTE 3 : Cartes 15, 9, 26, 35, 3, 22, 16, 5, 20, 19, 36, 44, 42, 50, 99
        int[] act3Cards = {15, 9, 26, 35, 3, 22, 16, 5, 20, 19, 36, 44, 42, 50, 99};
        layoutCardsInZone(act3Cards, ZONE3_X, ZONE_Y, padX, padY, cardW, cardH, gapX, gapY, 3);
    }

    private void layoutCardsInZone(int[] cardIds, double zoneX, double zoneY,
                                    double padX, double padY,
                                    double cardW, double cardH,
                                    double gapX, double gapY, int cols) {
        for (int i = 0; i < cardIds.length; i++) {
            int col = i % cols;
            int row = i / cols;
            double x = zoneX + padX + col * (cardW + gapX);
            double y = zoneY + padY + row * (cardH + gapY);
            cardPositions.put(cardIds[i], new double[]{x, y});
        }
    }

    // ================================================================
    //  GESTION DES CARTES
    // ================================================================

    /**
     * Ajoute ou met a jour une carte sur le plateau.
     * Si la carte n'etait pas visible avant et qu'elle l'est maintenant,
     * joue l'animation de retournement.
     */
    public BoardCardView addOrUpdateCard(Card card) {
        BoardCardView existing = cardViews.get(card.getId());

        if (existing != null) {
            // Carte deja sur le plateau
            if (card.isVisible() && !existing.isFaceUp()) {
                existing.flipToFront();
            } else if (!card.isVisible()) {
                // Carte retiree — on enleve la vue
                this.getChildren().remove(existing);
                cardViews.remove(card.getId());
                return null;
            }
            return existing;
        }

        // Nouvelle carte a ajouter
        double[] pos = cardPositions.get(card.getId());
        if (pos == null) return null; // carte inconnue dans le layout

        BoardCardView view = new BoardCardView(card);
        view.setLayoutX(pos[0]);
        view.setLayoutY(pos[1]);

        cardViews.put(card.getId(), view);
        this.getChildren().add(view);

        // Si visible, animer le retournement (sauf au 1er chargement)
        if (card.isVisible()) {
            view.showFrontImmediately();
        }

        return view;
    }

    /**
     * Ajoute une nouvelle carte avec animation de retournement
     */
    public BoardCardView addCardWithFlip(Card card) {
        double[] pos = cardPositions.get(card.getId());
        if (pos == null) return null;

        // Supprimer l'ancienne vue si elle existe
        BoardCardView existing = cardViews.get(card.getId());
        if (existing != null) {
            this.getChildren().remove(existing);
        }

        BoardCardView view = new BoardCardView(card);
        view.setLayoutX(pos[0]);
        view.setLayoutY(pos[1]);

        cardViews.put(card.getId(), view);
        this.getChildren().add(view);

        // Animation : apparition + flip
        view.setOpacity(0);
        view.showBackImmediately();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), view);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> view.flipToFront());
        fadeIn.play();

        return view;
    }

    /**
     * Retourne la vue d'une carte par son ID
     */
    public BoardCardView getCardView(int cardId) {
        return cardViews.get(cardId);
    }

    /**
     * Retire une carte du plateau
     */
    public void removeCard(int cardId) {
        BoardCardView view = cardViews.remove(cardId);
        if (view != null) {
            view.stopAnimations();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), view);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> this.getChildren().remove(view));
            fadeOut.play();
        }
    }

    /**
     * Vide toutes les cartes du plateau (pas le fond)
     */
    public void clearCards() {
        for (BoardCardView view : cardViews.values()) {
            view.stopAnimations();
            this.getChildren().remove(view);
        }
        cardViews.clear();
    }

    /**
     * Retourne toutes les vues de cartes
     */
    public Collection<BoardCardView> getAllCardViews() {
        return cardViews.values();
    }

    /**
     * Met en surbrillance une zone (quand l'acte change)
     */
    public void highlightZone(int actNumber) {
        // Reset all
        zone1Rect.setStrokeWidth(2);
        zone2Rect.setStrokeWidth(2);
        zone3Rect.setStrokeWidth(2);
        zone1Rect.setOpacity(1.0);
        zone2Rect.setOpacity(1.0);
        zone3Rect.setOpacity(1.0);

        // Highlight active zone
        Rectangle activeZone;
        switch (actNumber) {
            case 1: activeZone = zone1Rect; break;
            case 2: activeZone = zone2Rect; break;
            case 3: activeZone = zone3Rect; break;
            default: return;
        }
        activeZone.setStrokeWidth(3);
    }

    /**
     * Arrete toutes les animations
     */
    public void stopAllAnimations() {
        if (starsTimeline != null) starsTimeline.stop();
        for (BoardCardView view : cardViews.values()) {
            view.stopAnimations();
        }
    }
}
