package com.unlock.gui.javafx;

import com.unlock.core.*;
import com.unlock.model.Card;
import com.unlock.model.CardType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

import java.util.*;

/**
 * Controleur JavaFX pour la table de jeu Unlock!
 * - Un seul acte visible a la fois
 * - Cartes invisibles jusqu'a leur decouverte
 * - La pause bloque toute interaction (anti-triche)
 */
public class TableJeuController {

    @FXML private Pane boardContainer;
    @FXML private ScrollPane boardScrollPane;
    @FXML private HBox combinationZone;
    @FXML private ProgressBar oxygenBar;
    @FXML private Label oxygenLabel;
    @FXML private Label actLabel;
    @FXML private VBox indicesPanel;
    @FXML private Label indicesContent;
    @FXML private VBox inventoryContent;

    private Label comboInstructionLabel;
    private GameEngine engine;
    private GameBoardPane gameBoard;

    // Carte en attente de combinaison (drag & drop)
    private Card cardToCombine1 = null;

    // Suivi de l'etat
    private List<String>   discoveredHints    = new ArrayList<>();
    private Set<Integer>   knownVisibleCards  = new HashSet<>();
    private int            knownAct           = 1;

    // Timer de rafraichissement periodique (pour sync AppCompanion)
    private Timeline refreshTimeline;

    // ================================================================
    //  INITIALISATION
    // ================================================================

    public void setGameEngine(GameEngine engine) {
        this.engine = engine;
        initBoard();
        refreshTable();
        startPeriodicRefresh();
    }

    @FXML
    public void initialize() {
        comboInstructionLabel = (Label) combinationZone.getChildren().get(0);
        setupDragAndDrop();
    }

    /**
     * Initialise le plateau : seules les cartes DEJA VISIBLES sont affichees.
     * (Au depart, seulement la carte 10.)
     */
    private void initBoard() {
        gameBoard = new GameBoardPane();
        boardContainer.getChildren().add(gameBoard);
        boardContainer.setPrefSize(GameBoardPane.BOARD_WIDTH, GameBoardPane.BOARD_HEIGHT);
        boardContainer.setMinSize(GameBoardPane.BOARD_WIDTH, GameBoardPane.BOARD_HEIGHT);

        // Seules les cartes visibles ET ayant une position dans le layout Acte 1 sont affichees
        for (Card card : engine.getDeck().values()) {
            if (card.isVisible() && gameBoard.hasPositionForCard(card.getId())) {
                BoardCardView view = gameBoard.addOrUpdateCard(card);
                if (view != null) {
                    attachHandlers(view);
                    knownVisibleCards.add(card.getId());
                }
            }
        }
    }

    // ================================================================
    //  RAFRAICHISSEMENT
    // ================================================================

    @FXML
    public void handleRefresh() {
        cardToCombine1 = null;
        comboInstructionLabel.setText(">> Glissez deux cartes ici pour les combiner <<");
        comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #484f58;");
        refreshTable();
    }

    private void startPeriodicRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            if (engine != null) refreshTable();
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    /**
     * Rafraichit l'affichage complet.
     * Detecte les nouvelles cartes visibles et les changements d'acte.
     */
    public void refreshTable() {
        if (engine == null || gameBoard == null) return;

        // Quand le jeu est fini (timer a 0, oxygene vide, ou victoire) :
        // afficher l'ecran de fin et stopper TOUTE interaction
        if (engine.isGameWon())  { showVictory();  refreshTimeline.stop(); return; }
        if (engine.isGameLost()) { showGameOver(); refreshTimeline.stop(); return; }

        int currentAct = engine.getCurrentAct();

        // --- Changement d'acte ---
        if (currentAct != knownAct) {
            knownAct = currentAct;
            // Vider le suivi completement : addVisibleCardsOfCurrentAct le reconstruira
            knownVisibleCards.clear();
            // Transition animee vers le nouvel acte
            gameBoard.transitionToAct(currentAct, this::addVisibleCardsOfCurrentAct);
        } else {
            // Detecter les nouvelles cartes visibles dans le layout de l'acte courant
            for (Card card : engine.getDeck().values()) {
                if (!gameBoard.hasPositionForCard(card.getId())) continue;

                if (card.isVisible() && !knownVisibleCards.contains(card.getId())) {
                    // Nouvelle carte decouverte !
                    autoCollectSosComponent(card);
                    BoardCardView view = gameBoard.getCardView(card.getId());
                    if (view != null) {
                        view.flipToFront();
                    } else {
                        view = gameBoard.addCardWithFlip(card);
                        if (view != null) attachHandlers(view);
                    }
                    knownVisibleCards.add(card.getId());

                } else if (!card.isVisible() && knownVisibleCards.contains(card.getId())) {
                    gameBoard.removeCard(card.getId());
                    knownVisibleCards.remove(card.getId());
                }
            }
        }

        updateOxygenDisplay();
        if (actLabel != null) actLabel.setText("Acte " + currentAct);
        updateIndicesDisplay();
        updateInventoryDisplay();
    }

    /**
     * Apres une transition d'acte, ajoute toutes les cartes visibles
     * qui ont une position dans le nouveau layout.
     */
    private void addVisibleCardsOfCurrentAct() {
        for (Card card : engine.getDeck().values()) {
            if (card.isVisible() && gameBoard.hasPositionForCard(card.getId())
                    && !knownVisibleCards.contains(card.getId())) {
                autoCollectSosComponent(card);
                BoardCardView view = gameBoard.addCardWithFlip(card);
                if (view != null) attachHandlers(view);
                knownVisibleCards.add(card.getId());
            }
        }
    }

    /**
     * Auto-collecte les composants SOS (cartes 5, 7, 20) dans l'inventaire
     * des qu'ils deviennent visibles. Card 50 verifie l'inventaire pour valider.
     */
    private void autoCollectSosComponent(Card card) {
        int id = card.getId();
        if ((id == 5 || id == 7 || id == 20) && !engine.getInventory().hasItem(id)) {
            engine.getInventory().addItem(card);
        }
    }

    // ================================================================
    //  AFFICHAGE
    // ================================================================

    private void updateOxygenDisplay() {
        if (oxygenBar == null || oxygenLabel == null) return;
        double pct = engine.getOxygenPercentage() / 100.0;
        oxygenBar.setProgress(pct);
        oxygenLabel.setText(engine.getOxygenPercentage() + "%");
        if (pct > 0.5) {
            oxygenLabel.setStyle("-fx-text-fill: #00e676; -fx-font-size: 13px; -fx-font-weight: bold;");
            oxygenBar.setStyle("-fx-accent: #00e676;");
        } else if (pct > 0.25) {
            oxygenLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 13px; -fx-font-weight: bold;");
            oxygenBar.setStyle("-fx-accent: #ff9800;");
        } else {
            oxygenLabel.setStyle("-fx-text-fill: #ff1744; -fx-font-size: 13px; -fx-font-weight: bold;");
            oxygenBar.setStyle("-fx-accent: #ff1744;");
        }
    }

    private void updateIndicesDisplay() {
        if (indicesContent == null) return;
        if (discoveredHints.isEmpty()) {
            indicesContent.setText("(aucun indice)");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String h : discoveredHints) sb.append(h).append("\n\n");
            indicesContent.setText(sb.toString().trim());
        }
    }

    private void updateInventoryDisplay() {
        if (inventoryContent == null) return;
        inventoryContent.getChildren().clear();
        List<Card> items = engine.getInventory().getItems();
        if (items.isEmpty()) {
            Label e = new Label("(vide)");
            e.setStyle("-fx-text-fill: #484f58; -fx-font-size: 11px;");
            inventoryContent.getChildren().add(e);
        } else {
            for (Card item : items) {
                String hex = item.getType() == CardType.BLEU ? "#58a6ff"
                           : item.getType() == CardType.ROUGE ? "#f85149" : "#8b949e";
                String name = item.getDescription();
                if (name.contains("\u2014")) name = name.substring(0, name.indexOf("\u2014")).trim();
                Label lbl = new Label("[" + item.getId() + "] " + name);
                lbl.setStyle("-fx-text-fill:" + hex + "; -fx-font-size:10px;"
                        + "-fx-background-color:rgba(255,255,255,0.05);"
                        + "-fx-padding:3 6; -fx-background-radius:4;");
                lbl.setWrapText(true);
                lbl.setMaxWidth(200);
                inventoryContent.getChildren().add(lbl);
            }
        }
    }

    private void addDiscoveredHint(Card card) {
        String txt = "Carte " + card.getId() + " : " + card.getHint();
        if (!discoveredHints.contains(txt)) discoveredHints.add(txt);
    }

    // ================================================================
    //  INTERACTIONS CARTES
    // ================================================================

    /**
     * Attache les handlers de double-clic et drag-drop a une vue de carte.
     */
    private void attachHandlers(BoardCardView view) {
        enableDragDropOnCard(view);
        view.addEventHandler(BoardCardView.CARD_SELECTED, e -> handleCardInteraction(view.getCard()));
    }

    /**
     * Gere l'interaction sur une carte (double-clic).
     * BLOQUEE si le jeu est en pause ou termine.
     */
    private void handleCardInteraction(Card card) {
        if (engine.isGameOver()) return;

        // *** PAUSE = AUCUNE INTERACTION ***
        if (engine.isPaused()) {
            comboInstructionLabel.setText("[PAUSE] Relancez le timer dans l'App Compagnon pour jouer !");
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9800; -fx-font-weight: bold;");
            return;
        }

        switch (card.getType()) {
            case NEUTRE:   handleFouille(card);   break;
            case BLEU:
            case ROUGE:
                // Double-clic = message d'aide uniquement, jamais de penalite
                comboInstructionLabel.setText("[OBJET] Carte " + card.getId()
                        + " — Glissez-la dans la zone du bas pour la combiner avec une autre.");
                comboInstructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64ffda;");
                break;
            case MACHINE:  handleMiniGame(card);  break;
            case CODE:     handleCodeCard(card);  break;
            case RESULTAT: if (engine.isGameWon()) showVictory(); break;
            case PENALITE:
                comboInstructionLabel.setText("[ATTENTION] " + card.getDescription());
                comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff5555;");
                break;
        }
    }

    private void handleFouille(Card card) {
        if (card.getHint() != null) {
            addDiscoveredHint(card);
            comboInstructionLabel.setText("[INDICE] " + card.getHint());
            comboInstructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffd54f;");
            updateIndicesDisplay();
            if (card.getRevealsCardIds() == null || card.getRevealsCardIds().length == 0) {
                card.setVisible(false);
                refreshTable();
                return;
            }
        }

        int result = engine.handleFouille(card.getId());
        if (result > 0) {
            // Afficher les IDs des cartes decouvertes (utile pour carte 38)
            StringBuilder ids = new StringBuilder();
            if (card.getRevealsCardIds() != null) {
                for (int id : card.getRevealsCardIds()) ids.append(id).append(" ");
            }
            comboInstructionLabel.setText("[FOUILLE] " + result + " carte(s) decouverte(s) !"
                    + (ids.length() > 0 ? " (IDs: " + ids.toString().trim() + ")" : ""));
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e676;");
            card.setVisible(false);
            refreshTable();
        } else if (result == -1 && card.getHint() == null) {
            comboInstructionLabel.setText("Rien de plus a trouver ici... (-10s O2)");
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9800;");
            updateOxygenDisplay();
        }
    }

    private void handleMiniGame(Card card) {
        String id = card.getMiniGameId();
        if (id == null) return;

        if (engine.getState().isMiniGameCompleted(id)) {
            comboInstructionLabel.setText("[OK] Mini-jeu deja reussi !");
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e676;");
            return;
        }

        if ("final_sos".equals(id)) { handleFinalSOS(card); return; }

        Runnable onSuccess = () -> {
            Card revealed = engine.onMiniGameSuccess(id);
            if (revealed != null) {
                comboInstructionLabel.setText("[SUCCES] Mini-jeu reussi ! Carte " + revealed.getId() + " debloquee !");
                comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e676;");
            }
            // Retirer la carte MACHINE via le deck (reference directe)
            engine.getDeck().get(card.getId()).setVisible(false);
            refreshTable();
        };

        Runnable onFailure = () -> {
            engine.onMiniGameFailure();
            updateOxygenDisplay();
        };

        switch (id) {
            case CablesMiniGame.GAME_ID:
                new CablesMiniGameView(engine.getCablesMiniGame(), onSuccess, onFailure).show();
                break;
            case RadioMiniGame.GAME_ID:
                new RadioMiniGameView(engine.getRadioMiniGame(), onSuccess, onFailure).show();
                break;
            case PipesMiniGame.GAME_ID:
                new PipesMiniGameView(engine.getPipesMiniGame(), onSuccess, onFailure).show();
                break;
        }
    }

    private void handleCodeCard(Card card) {
        if (card.requiresCode()) {
            comboInstructionLabel.setText("[CODE] Entrez le code sur l'App Compagnon pour la carte " + card.getId());
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffc107;");
        }
    }

    private void handleFinalSOS(Card card) {
        String missing = engine.getMissingSosComponents();
        if (missing.contains("carte")) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Station SOS");
            a.setHeaderText("Composants manquants !");
            a.setContentText("Il vous manque :\n" + missing + "\nCombinez les cartes BLEU + ROUGE !");
            a.showAndWait();
            return;
        }
        if (engine.tryFinalSOS()) { refreshTable(); showVictory(); }
    }

    // ================================================================
    //  DRAG & DROP (combinaison de cartes)
    // ================================================================

    private void enableDragDropOnCard(BoardCardView view) {
        view.setOnDragDetected(event -> {
            // *** PAUSE = PAS DE DRAG ***
            if (engine.isPaused() || !view.isFaceUp()) { event.consume(); return; }
            Dragboard db = view.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(String.valueOf(view.getCard().getId()));
            db.setContent(cc);
            event.consume();
        });
    }

    private void setupDragAndDrop() {
        combinationZone.setOnDragOver(event -> {
            if (event.getGestureSource() != combinationZone && event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });

        combinationZone.setOnDragDropped(event -> {
            boolean ok = false;
            Dragboard db = event.getDragboard();

            if (db.hasString()) {
                // *** PAUSE = PAS DE COMBINAISON ***
                if (engine.isPaused()) {
                    comboInstructionLabel.setText("[PAUSE] Relancez le timer pour jouer !");
                    comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9800; -fx-font-weight: bold;");
                    event.setDropCompleted(false);
                    event.consume();
                    return;
                }

                int cardId = Integer.parseInt(db.getString());
                Card dropped = engine.getDeck().get(cardId);

                if (cardToCombine1 == null) {
                    cardToCombine1 = dropped;
                    comboInstructionLabel.setText("Carte " + cardId + " selectionnee. Glissez la 2eme carte.");
                    comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64ffda;");
                } else {
                    Card result = engine.combineCards(cardToCombine1.getId(), dropped.getId());
                    if (result != null) {
                        comboInstructionLabel.setText("[SUCCES] " + cardToCombine1.getId()
                                + " + " + dropped.getId() + " = Carte " + result.getId() + " !");
                        comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e676;");
                        cardToCombine1.setVisible(false);
                        dropped.setVisible(false);
                    } else {
                        comboInstructionLabel.setText("[ERREUR] Combinaison invalide ! (-30s O2)");
                        comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff5555;");
                    }
                    cardToCombine1 = null;
                    refreshTable();
                }
                ok = true;
            }
            event.setDropCompleted(ok);
            event.consume();
        });
    }

    // ================================================================
    //  FIN DE PARTIE
    // ================================================================

    private void showVictory() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("VICTOIRE !");
        a.setHeaderText("SOS ENVOYE - SECOURS EN ROUTE !");
        int t = engine.getTimeRemaining();
        a.setContentText(
            "Felicitations ! Vous avez sauve l'equipage !\n\n"
            + "Temps restant : " + (t / 60) + "min " + (t % 60) + "s\n"
            + "Oxygene restant : " + engine.getOxygenPercentage() + "%\n\n"
            + "Score : " + calculateScore(t) + " / 5 etoiles");
        a.showAndWait();
    }

    private void showGameOver() {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("GAME OVER");
        a.setHeaderText("OXYGENE EPUISE !");
        a.setContentText("Vous n'avez pas reussi a envoyer le SOS a temps...\n"
                + "La station Heliox-7 restera silencieuse pour toujours.");
        a.showAndWait();
    }

    private int calculateScore(int timeLeft) {
        if (timeLeft > 2400) return 5;
        if (timeLeft > 1800) return 4;
        if (timeLeft > 1200) return 3;
        if (timeLeft > 600)  return 2;
        return 1;
    }
}
