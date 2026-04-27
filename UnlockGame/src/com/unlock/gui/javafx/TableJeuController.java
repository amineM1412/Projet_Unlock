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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;

/**
 * Controleur JavaFX pour la table de jeu Unlock!
 * Gere l'affichage du plateau visuel, la fouille, la combinaison,
 * les mini-jeux, les indices et la jauge d'oxygene.
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

    // Carte en attente de combinaison
    private Card cardToCombine1 = null;

    // Liste des indices decouverts (stockes sous forme de phrase)
    private List<String> discoveredHints = new ArrayList<>();

    // Set des cartes deja connues comme visibles (pour detecter les nouvelles)
    private Set<Integer> knownVisibleCards = new HashSet<>();

    // Timeline pour rafraichir periodiquement (detecter changements depuis AppCompanion)
    private Timeline refreshTimeline;

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
     * Initialise le plateau de jeu visuel
     */
    private void initBoard() {
        gameBoard = new GameBoardPane();
        boardContainer.getChildren().add(gameBoard);

        // Dimensionner le conteneur
        boardContainer.setPrefSize(GameBoardPane.BOARD_WIDTH, GameBoardPane.BOARD_HEIGHT);
        boardContainer.setMinSize(GameBoardPane.BOARD_WIDTH, GameBoardPane.BOARD_HEIGHT);

        // Placer toutes les cartes du deck sur le plateau (face cachee)
        for (Card card : engine.getDeck().values()) {
            BoardCardView view = gameBoard.addOrUpdateCard(card);
            if (view != null) {
                enableDragDropOnCard(view);
                view.addEventHandler(BoardCardView.CARD_SELECTED, e -> {
                    handleCardInteraction(view.getCard());
                });
                // Marquer comme connue si deja visible
                if (card.isVisible()) {
                    knownVisibleCards.add(card.getId());
                }
            }
        }

        // Highlight la zone de l'acte initial
        gameBoard.highlightZone(engine.getCurrentAct());
    }

    @FXML
    public void handleRefresh() {
        // Reset de la zone de combinaison
        cardToCombine1 = null;
        comboInstructionLabel.setText(">> Glissez deux cartes ici pour les combiner <<");
        comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #484f58;");
        refreshTable();
    }

    /**
     * Demarre un rafraichissement periodique toutes les 2 secondes
     * pour detecter les changements effectues depuis l'AppCompanion (codes entres).
     */
    private void startPeriodicRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            if (engine != null) {
                refreshTable();
            }
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    // ================================================================
    //  RAFRAICHISSEMENT DE L'AFFICHAGE
    // ================================================================

    /**
     * Rafraichit l'affichage complet : cartes, oxygene, indices, acte, inventaire.
     */
    public void refreshTable() {
        if (engine == null || gameBoard == null) return;

        // Verifier etat du jeu
        if (engine.isGameWon()) {
            showVictory();
            return;
        }
        if (engine.isGameLost()) {
            showGameOver();
            return;
        }

        // Mettre a jour les cartes sur le plateau
        for (Card card : engine.getDeck().values()) {
            if (card.isVisible() && !knownVisibleCards.contains(card.getId())) {
                // Nouvelle carte visible ! Animer le retournement
                BoardCardView view = gameBoard.getCardView(card.getId());
                if (view != null) {
                    view.flipToFront();
                } else {
                    // Carte pas encore sur le plateau, l'ajouter avec animation
                    view = gameBoard.addCardWithFlip(card);
                    if (view != null) {
                        enableDragDropOnCard(view);
                        final BoardCardView finalView = view;
                        view.addEventHandler(BoardCardView.CARD_SELECTED, e -> {
                            handleCardInteraction(finalView.getCard());
                        });
                    }
                }
                knownVisibleCards.add(card.getId());
            } else if (!card.isVisible() && knownVisibleCards.contains(card.getId())) {
                // Carte retiree (fouillee ou combinee)
                gameBoard.removeCard(card.getId());
                knownVisibleCards.remove(card.getId());
            }
        }

        // Rafraichir l'oxygene
        updateOxygenDisplay();

        // Rafraichir l'acte
        if (actLabel != null) {
            actLabel.setText("Acte " + engine.getCurrentAct());
            gameBoard.highlightZone(engine.getCurrentAct());
        }

        // Rafraichir les indices
        updateIndicesDisplay();

        // Rafraichir l'inventaire
        updateInventoryDisplay();
    }

    /**
     * Met a jour l'affichage de la jauge d'oxygene
     */
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

    /**
     * Met a jour l'affichage des indices decouverts
     */
    private void updateIndicesDisplay() {
        if (indicesContent == null) return;

        if (discoveredHints.isEmpty()) {
            indicesContent.setText("(aucun indice)");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String hint : discoveredHints) {
                sb.append(hint).append("\n\n");
            }
            indicesContent.setText(sb.toString().trim());
        }
    }

    /**
     * Met a jour l'affichage de l'inventaire
     */
    private void updateInventoryDisplay() {
        if (inventoryContent == null) return;
        inventoryContent.getChildren().clear();

        List<Card> items = engine.getInventory().getItems();
        if (items.isEmpty()) {
            Label emptyLabel = new Label("(vide)");
            emptyLabel.setStyle("-fx-text-fill: #484f58; -fx-font-size: 11px;");
            inventoryContent.getChildren().add(emptyLabel);
        } else {
            for (Card item : items) {
                String colorHex;
                switch (item.getType()) {
                    case BLEU: colorHex = "#58a6ff"; break;
                    case ROUGE: colorHex = "#f85149"; break;
                    default: colorHex = "#8b949e"; break;
                }
                String name = item.getDescription();
                if (name.contains("\u2014")) {
                    name = name.substring(0, name.indexOf("\u2014")).trim();
                }
                Label itemLabel = new Label("[" + item.getId() + "] " + name);
                itemLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 10px; " +
                        "-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 3 6; " +
                        "-fx-background-radius: 4;");
                itemLabel.setWrapText(true);
                itemLabel.setMaxWidth(200);
                inventoryContent.getChildren().add(itemLabel);
            }
        }
    }

    /**
     * Ajoute un indice a la liste des indices decouverts
     */
    private void addDiscoveredHint(Card card) {
        String hintText = "Carte " + card.getId() + " : " + card.getHint();
        // Eviter les doublons
        if (!discoveredHints.contains(hintText)) {
            discoveredHints.add(hintText);
        }
    }

    // ================================================================
    //  INTERACTION AVEC LES CARTES (Double-clic)
    // ================================================================

    /**
     * Gere l'interaction double-clic sur une carte selon son type.
     */
    private void handleCardInteraction(Card card) {
        if (engine.isGameOver()) return;

        switch (card.getType()) {
            case NEUTRE:
                handleFouille(card);
                break;
            case BLEU:
            case ROUGE:
                // Les cartes BLEU et ROUGE sont utilisees uniquement pour la combinaison (drag & drop)
                comboInstructionLabel.setText("Glissez cette carte dans la zone de combinaison pour l'utiliser.");
                comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64ffda;");
                break;
            case MACHINE:
                handleMiniGame(card);
                break;
            case CODE:
                handleCodeCard(card);
                break;
            case RESULTAT:
                if (engine.isGameWon()) showVictory();
                break;
            case PENALITE:
                comboInstructionLabel.setText("[ATTENTION] " + card.getDescription());
                comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff5555;");
                break;
        }
    }

    /**
     * Fouille d'une carte NEUTRE pour reveler des cartes cachees
     */
    private void handleFouille(Card card) {
        // Afficher et stocker l'indice s'il y en a un
        if (card.getHint() != null) {
            addDiscoveredHint(card);
            comboInstructionLabel.setText("[INDICE] " + card.getHint());
            comboInstructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffd54f;");
            updateIndicesDisplay();

            // Si la carte n'a que des indices (pas de cartes a reveler), la retirer automatiquement
            if (card.getRevealsCardIds() == null || card.getRevealsCardIds().length == 0) {
                card.setVisible(false);
                refreshTable();
                return;
            }
        }

        int result = engine.handleFouille(card.getId());
        if (result > 0) {
            comboInstructionLabel.setText("[FOUILLE] Vous avez trouve " + result + " nouvelle(s) carte(s) !");
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e676;");
            // Auto-retirer la carte fouillee apres avoir revele les sous-cartes
            card.setVisible(false);
            refreshTable();
        } else if (result == -1 && card.getHint() == null) {
            comboInstructionLabel.setText("Il n'y a plus rien a trouver ici... (-10s O2)");
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9800;");
            updateOxygenDisplay();
        }
    }

    /**
     * Lance un mini-jeu pour une carte MACHINE
     */
    private void handleMiniGame(Card card) {
        String miniGameId = card.getMiniGameId();
        if (miniGameId == null) return;

        // Verifier si deja complete
        if (engine.getState().isMiniGameCompleted(miniGameId)) {
            comboInstructionLabel.setText("[OK] Ce mini-jeu est deja reussi !");
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e676;");
            return;
        }

        // Cas special : enigme finale SOS
        if ("final_sos".equals(miniGameId)) {
            handleFinalSOS(card);
            return;
        }

        // Callbacks pour succes/echec
        Runnable onSuccess = () -> {
            Card revealed = engine.onMiniGameSuccess(miniGameId);
            if (revealed != null) {
                comboInstructionLabel.setText("[SUCCES] Mini-jeu reussi ! Carte " + revealed.getId() + " debloquee !");
                comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e676;");
            }
            // Auto-retirer la carte MACHINE apres succes du mini-jeu
            card.setVisible(false);
            refreshTable();
        };

        Runnable onFailure = () -> {
            engine.onMiniGameFailure();
            updateOxygenDisplay();
        };

        // Lancer le mini-jeu correspondant
        switch (miniGameId) {
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

    /**
     * Gere une carte CODE (demande le code via l'App Compagnon Swing)
     */
    private void handleCodeCard(Card card) {
        if (card.requiresCode()) {
            comboInstructionLabel.setText("[CODE] Entrez le code sur l'App Compagnon pour la carte " + card.getId());
            comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffc107;");
        }
    }

    /**
     * Gere l'enigme finale SOS (carte 50)
     */
    private void handleFinalSOS(Card card) {
        String missing = engine.getMissingSosComponents();
        if (missing.contains("carte")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Station SOS");
            alert.setHeaderText("Composants manquants !");
            alert.setContentText("Il vous manque :\n" + missing +
                    "\nCombinez les cartes BLEU + ROUGE correspondantes !");
            alert.showAndWait();
            return;
        }

        boolean victory = engine.tryFinalSOS();
        if (victory) {
            refreshTable();
            showVictory();
        }
    }

    // ================================================================
    //  DRAG & DROP (Combinaison de cartes)
    // ================================================================

    private void enableDragDropOnCard(BoardCardView view) {
        view.setOnDragDetected(event -> {
            if (view.isFaceUp()) {
                Dragboard db = view.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(view.getCard().getId()));
                db.setContent(content);
            }
            event.consume();
        });
    }

    private void setupDragAndDrop() {
        combinationZone.setOnDragOver(event -> {
            if (event.getGestureSource() != combinationZone && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        combinationZone.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                int cardId = Integer.parseInt(db.getString());
                Card droppedCard = engine.getDeck().get(cardId);

                if (cardToCombine1 == null) {
                    cardToCombine1 = droppedCard;
                    comboInstructionLabel.setText("Carte " + cardId + " en attente. Glissez la 2eme carte.");
                    comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64ffda;");
                } else {
                    Card result = engine.combineCards(cardToCombine1.getId(), droppedCard.getId());
                    if (result != null) {
                        comboInstructionLabel.setText("[SUCCES] " + cardToCombine1.getId() + " + "
                                + droppedCard.getId() + " = Carte " + result.getId() + " !");
                        comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e676;");

                        // Auto-retirer les deux cartes combinees de la table
                        cardToCombine1.setVisible(false);
                        droppedCard.setVisible(false);
                    } else {
                        comboInstructionLabel.setText("[ERREUR] " + cardToCombine1.getId() + " + "
                                + droppedCard.getId() + " = Combinaison invalide ! (-30s O2)");
                        comboInstructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff5555;");
                    }
                    cardToCombine1 = null;
                    refreshTable();
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // ================================================================
    //  FIN DE PARTIE
    // ================================================================

    private void showVictory() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("VICTOIRE !");
        alert.setHeaderText("SOS ENVOYE - SECOURS EN ROUTE !");
        int timeLeft = engine.getTimeRemaining();
        int o2Left = engine.getOxygenRemaining();
        alert.setContentText(
                "Felicitations ! Vous avez sauve l'equipage !\n\n" +
                "Temps restant : " + (timeLeft / 60) + "min " + (timeLeft % 60) + "s\n" +
                "Oxygene restant : " + engine.getOxygenPercentage() + "%\n\n" +
                "Score : " + calculateScore(timeLeft, o2Left) + " / 5 etoiles");
        alert.showAndWait();
    }

    private void showGameOver() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("GAME OVER");
        alert.setHeaderText("OXYGENE EPUISE !");
        alert.setContentText("Vous n'avez pas reussi a envoyer le SOS a temps...\n" +
                "La station Heliox-7 restera silencieuse pour toujours.");
        alert.showAndWait();
    }

    private int calculateScore(int timeLeft, int o2Left) {
        if (timeLeft > 2400) return 5;
        if (timeLeft > 1800) return 4;
        if (timeLeft > 1200) return 3;
        if (timeLeft > 600) return 2;
        return 1;
    }

    // ================================================================
    //  UTILITAIRES
    // ================================================================

    private String getCardShortName(Card card) {
        String desc = card.getDescription();
        if (desc.contains("\u2014")) return desc.substring(0, desc.indexOf("\u2014")).trim();
        return desc;
    }
}
