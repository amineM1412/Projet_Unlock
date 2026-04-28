package com.unlock.gui.javafx;

import com.unlock.model.Card;
import javafx.animation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;

/**
 * Plateau de jeu — affiche UN SEUL acte a la fois.
 * Les cartes sont invisibles jusqu'a leur decouverte.
 * Transition animee lors du changement d'acte.
 */
public class GameBoardPane extends Pane {

    // ================================================================
    //  CONSTANTES
    // ================================================================

    public static final double BOARD_WIDTH  = 940;
    public static final double BOARD_HEIGHT = 660;

    private static final double CARD_W   = 100;
    private static final double CARD_H   = 135;
    private static final double GAP_X    = 14;
    private static final double GAP_Y    = 14;
    private static final double PAD_TOP  = 58;  // espace pour le titre d'acte
    private static final double PAD_SIDE = 20;

    /** IDs des cartes de chaque acte (ordre d'affichage dans la grille) */
    private static final int[][] ACT_CARD_IDS = {
        {},                                                                            // index 0 inutilise
        {10, 12, 13, 6, 24, 30, 45},                                                   // Acte 1 — 7 cartes
        {14, 17, 8, 21, 7, 18, 11, 19, 36, 29, 25, 38, 33, 40, 27},                   // Acte 2 — 15 cartes
        {15, 9, 26, 35, 3, 22, 16, 7, 5, 20, 40, 44, 42, 50, 99}                      // Acte 3 — 15 cartes (7,5,20,40 persistents depuis acte 2)
    };

    /** Nombre de colonnes par acte */
    private static final int[] ACT_COLS = {0, 4, 4, 4};

    /** Titres des actes */
    private static final String[] ACT_TITLES = {
        "",
        "ACTE 1  —  SAS DE DECOMPRESSION",
        "ACTE 2  —  LABORATOIRE & COMMUNICATIONS",
        "ACTE 3  —  REACTEUR & ENIGME FINALE"
    };

    /** Couleurs d'accentuation des actes */
    private static final String[] ACT_COLORS = {
        "", "#E53935", "#1E88E5", "#43A047"
    };

    // ================================================================
    //  ETAT INTERNE
    // ================================================================

    /** Vues actives (uniquement les cartes VISIBLES affichees) */
    private final Map<Integer, BoardCardView> cardViews    = new HashMap<>();

    /** Positions pre-calculees pour le layout de l'acte courant */
    private final Map<Integer, double[]>      cardPositions = new HashMap<>();

    private int currentAct = 1;

    // Elements visuels
    private Canvas    backgroundCanvas;
    private Rectangle actZoneRect;
    private Label     actTitleLabel;
    private final List<Circle> stars = new ArrayList<>();
    private Timeline starsTimeline;

    // ================================================================
    //  CONSTRUCTEUR
    // ================================================================

    public GameBoardPane() {
        setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        setMinSize(BOARD_WIDTH, BOARD_HEIGHT);

        drawBackground();
        createStars();
        createActDisplay();
        loadActPositions(1);  // preparer acte 1 par defaut
    }

    // ================================================================
    //  FOND SPATIAL
    // ================================================================

    private void drawBackground() {
        backgroundCanvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();

        gc.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0a1a")),
                new Stop(0.5, Color.web("#0d1b2a")),
                new Stop(1.0, Color.web("#0a0e17"))
        ));
        gc.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Nebuleuses subtiles
        gc.setFill(new RadialGradient(0, 0, 220, 320, 280, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a237e", 0.12)), new Stop(1, Color.TRANSPARENT)));
        gc.fillOval(80, 170, 560, 560);

        gc.setFill(new RadialGradient(0, 0, 720, 180, 220, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a148c", 0.08)), new Stop(1, Color.TRANSPARENT)));
        gc.fillOval(620, 80, 440, 440);

        getChildren().add(backgroundCanvas);
    }

    // ================================================================
    //  ETOILES SCINTILLANTES
    // ================================================================

    private void createStars() {
        Random rand = new Random(42);
        for (int i = 0; i < 100; i++) {
            Circle star = new Circle(
                rand.nextDouble() * BOARD_WIDTH,
                rand.nextDouble() * BOARD_HEIGHT,
                0.5 + rand.nextDouble() * 1.5
            );
            star.setFill(Color.web("#ffffff", 0.25 + rand.nextDouble() * 0.55));
            stars.add(star);
            getChildren().add(star);
        }

        starsTimeline = new Timeline();
        Random r2 = new Random();
        for (Circle star : stars) {
            double delay = r2.nextDouble() * 4000;
            starsTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(delay),
                    new KeyValue(star.opacityProperty(), star.getOpacity())),
                new KeyFrame(Duration.millis(delay + 1500),
                    new KeyValue(star.opacityProperty(), 0.08)),
                new KeyFrame(Duration.millis(delay + 3000),
                    new KeyValue(star.opacityProperty(), star.getOpacity()))
            );
        }
        starsTimeline.setCycleCount(Timeline.INDEFINITE);
        starsTimeline.play();
    }

    // ================================================================
    //  ZONE D'ACTE
    // ================================================================

    private void createActDisplay() {
        // Cadre de zone
        actZoneRect = new Rectangle(PAD_SIDE, 44, BOARD_WIDTH - PAD_SIDE * 2, BOARD_HEIGHT - 54);
        actZoneRect.setArcWidth(18);
        actZoneRect.setArcHeight(18);
        actZoneRect.setFill(Color.web(ACT_COLORS[1], 0.07));
        actZoneRect.setStroke(Color.web(ACT_COLORS[1], 0.30));
        actZoneRect.setStrokeWidth(2);

        DropShadow ds = new DropShadow();
        ds.setRadius(18);
        ds.setColor(Color.web(ACT_COLORS[1], 0.20));
        actZoneRect.setEffect(ds);
        getChildren().add(actZoneRect);

        // Titre d'acte
        actTitleLabel = new Label(ACT_TITLES[1]);
        actTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        actTitleLabel.setTextFill(Color.web(ACT_COLORS[1]));
        actTitleLabel.setLayoutX(PAD_SIDE + 12);
        actTitleLabel.setLayoutY(14);
        getChildren().add(actTitleLabel);
    }

    // ================================================================
    //  CHARGEMENT DES POSITIONS PAR ACTE
    // ================================================================

    /**
     * Pre-calcule les positions de grille de toutes les cartes de l'acte.
     * (Les cartes non encore visibles auront quand meme une position reservee.)
     */
    public void loadActPositions(int actNumber) {
        cardPositions.clear();
        currentAct = actNumber;
        if (actNumber < 1 || actNumber > 3) return;

        int[] ids  = ACT_CARD_IDS[actNumber];
        int   cols = ACT_COLS[actNumber];

        // Centrage horizontal
        double totalW = cols * CARD_W + (cols - 1) * GAP_X;
        double startX = (BOARD_WIDTH - totalW) / 2.0;

        // Calcul du nombre de lignes pour verifier que ca rentre
        int rows = (int) Math.ceil((double) ids.length / cols);
        // Hauteur totale des cartes
        double totalH = rows * CARD_H + (rows - 1) * GAP_Y;
        // Zone disponible
        double availH = BOARD_HEIGHT - PAD_TOP - PAD_SIDE;
        // Si ca deborde, reduire l'ecart vertical
        double actualGapY = GAP_Y;
        if (totalH > availH) {
            actualGapY = Math.max(4, (availH - rows * CARD_H) / Math.max(1, rows - 1));
        }

        for (int i = 0; i < ids.length; i++) {
            int col = i % cols;
            int row = i / cols;
            double x = startX + col * (CARD_W + GAP_X);
            double y = PAD_TOP + row * (CARD_H + actualGapY);
            cardPositions.put(ids[i], new double[]{x, y});
        }
    }

    // ================================================================
    //  TRANSITION ENTRE ACTES
    // ================================================================

    /**
     * Effectue une transition animee vers le nouvel acte.
     * Retire les cartes actuelles avec un fondu, met a jour le titre,
     * puis appelle onComplete (qui ajoutera les nouvelles cartes visibles).
     */
    public void transitionToAct(int newAct, Runnable onComplete) {
        List<BoardCardView> toRemove = new ArrayList<>(cardViews.values());

        if (toRemove.isEmpty()) {
            applyActTransition(newAct, onComplete);
            return;
        }

        int[] doneCount = {0};
        for (BoardCardView view : toRemove) {
            view.stopAnimations();
            FadeTransition ft = new FadeTransition(Duration.millis(300), view);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(ev -> {
                getChildren().remove(view);
                doneCount[0]++;
                if (doneCount[0] == toRemove.size()) {
                    cardViews.clear();
                    applyActTransition(newAct, onComplete);
                }
            });
            ft.play();
        }
    }

    private void applyActTransition(int newAct, Runnable onComplete) {
        loadActPositions(newAct);

        // Mise a jour visuelle du cadre et du titre
        String color = ACT_COLORS[newAct];
        actTitleLabel.setText(ACT_TITLES[newAct]);
        actTitleLabel.setTextFill(Color.web(color));
        actZoneRect.setFill(Color.web(color, 0.07));
        actZoneRect.setStroke(Color.web(color, 0.30));
        DropShadow ds = new DropShadow();
        ds.setRadius(18);
        ds.setColor(Color.web(color, 0.20));
        actZoneRect.setEffect(ds);

        // Animer le titre (pulse)
        ScaleTransition pulse = new ScaleTransition(Duration.millis(400), actTitleLabel);
        pulse.setFromX(0.8); pulse.setToX(1.0);
        pulse.setFromY(0.8); pulse.setToY(1.0);
        pulse.play();

        if (onComplete != null) onComplete.run();
    }

    // ================================================================
    //  GESTION DES CARTES
    // ================================================================

    /**
     * Ajoute une carte visible sur le plateau (sans animation).
     * Si la carte n'est PAS visible, elle n'est PAS affichee.
     */
    public BoardCardView addOrUpdateCard(Card card) {
        BoardCardView existing = cardViews.get(card.getId());

        if (existing != null) {
            if (card.isVisible() && !existing.isFaceUp()) {
                existing.flipToFront();
            } else if (!card.isVisible()) {
                removeCard(card.getId());
                return null;
            }
            return existing;
        }

        // Ne pas afficher les cartes non decouvertes
        if (!card.isVisible()) return null;

        double[] pos = cardPositions.get(card.getId());
        if (pos == null) return null;  // carte pas dans cet acte

        BoardCardView view = new BoardCardView(card);
        view.setLayoutX(pos[0]);
        view.setLayoutY(pos[1]);
        cardViews.put(card.getId(), view);
        getChildren().add(view);
        view.showFrontImmediately();

        return view;
    }

    /**
     * Ajoute une carte avec animation de retournement (decouverte en cours de partie).
     */
    public BoardCardView addCardWithFlip(Card card) {
        if (!card.isVisible()) return null;

        double[] pos = cardPositions.get(card.getId());
        if (pos == null) return null;

        // Supprimer l'ancienne vue si necessaire
        BoardCardView existing = cardViews.get(card.getId());
        if (existing != null) {
            existing.stopAnimations();
            getChildren().remove(existing);
        }

        BoardCardView view = new BoardCardView(card);
        view.setLayoutX(pos[0]);
        view.setLayoutY(pos[1]);
        view.setOpacity(0);
        view.showBackImmediately();

        cardViews.put(card.getId(), view);
        getChildren().add(view);

        // Fondu + retournement
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), view);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> view.flipToFront());
        fadeIn.play();

        return view;
    }

    /**
     * Retire une carte du plateau avec fondu.
     */
    public void removeCard(int cardId) {
        BoardCardView view = cardViews.remove(cardId);
        if (view != null) {
            view.stopAnimations();
            FadeTransition ft = new FadeTransition(Duration.millis(350), view);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> getChildren().remove(view));
            ft.play();
        }
    }

    /** Retourne la vue d'une carte par son ID. */
    public BoardCardView getCardView(int cardId) {
        return cardViews.get(cardId);
    }

    /** Retourne toutes les vues actives. */
    public Collection<BoardCardView> getAllCardViews() {
        return cardViews.values();
    }

    /** Vide toutes les cartes (sans animation). */
    public void clearCards() {
        for (BoardCardView v : cardViews.values()) {
            v.stopAnimations();
            getChildren().remove(v);
        }
        cardViews.clear();
    }

    /** Arrete toutes les animations. */
    public void stopAllAnimations() {
        if (starsTimeline != null) starsTimeline.stop();
        for (BoardCardView v : cardViews.values()) v.stopAnimations();
    }

    // ================================================================
    //  METHODE LEGACY (gardee pour compatibilite)
    // ================================================================

    /**
     * @param actNumber *  @deprecated Remplace par transitionToAct(). */
    public void highlightZone(int actNumber) {
        // No-op : une seule zone affichee a la fois
    }

    public int getCurrentAct() {
        return currentAct;
    }

    /**
     * Retourne true si la carte a une position pre-calculee dans l'acte courant.
     * Permet de filtrer correctement les cartes sans se fier uniquement a actNumber
     * (ex: carte 45 qui a actNumber=2 mais apparait dans le layout de l'acte 1).
     */
    public boolean hasPositionForCard(int cardId) {
        return cardPositions.containsKey(cardId);
    }
}
