// Fichier pour le contrôleur JavaFX
package com.unlock.gui.javafx;

import com.unlock.core.GameEngine;
import com.unlock.model.Card;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class TableJeuController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private HBox combinationZone;
    private Label comboInstructionLabel;

    // Instance partagée (ou globale) du moteur
    private GameEngine engine;

    // Cartes en cours de combinaison
    private Card cardToCombine1 = null;

    public void setGameEngine(GameEngine engine) {
        this.engine = engine;
        refreshTable();
    }

    @FXML
    public void initialize() {
        comboInstructionLabel = (Label) combinationZone.getChildren().get(0);
        setupDragAndDrop();
    }

    @FXML
    public void handleRefresh() {
        refreshTable();
    }

    /**
     * Rafraîchit l'affichage en ne montrant que les cartes "visibles" dans le
     * GameEngine.
     */
    public void refreshTable() {
        if (engine == null)
            return;
        cardsContainer.getChildren().clear();

        for (Card c : engine.getDeck().values()) {
            if (c.isVisible()) {
                CardView cv = new CardView(c);
                enableDragDropOnCard(cv);

                // Écoute de l'événement de fouille (Double clic configuré dans CardView)
                cv.addEventHandler(CardView.CARD_SELECTED, e -> {
                    handleCardFouille(cv.getCard());
                });

                cardsContainer.getChildren().add(cv);
            }
        }
    }

    /**
     * Logique de fouille (ex: on double-clic sur le lieu 10, ça dévoile le 6 et le
     * 24)
     */
    private void handleCardFouille(Card cardToFouille) {
        if (cardToFouille.getId() == 10) {
            System.out.println("🔎 Fouille du lieu 10...");
            boolean changed = false;
            // On révèle les objets cachés dans le lieu 10
            Card hidden6 = engine.getDeck().get(6);
            if (hidden6 != null && !hidden6.isVisible()) {
                hidden6.setVisible(true);
                changed = true;
            }
            Card hidden24 = engine.getDeck().get(24);
            if (hidden24 != null && !hidden24.isVisible()) {
                hidden24.setVisible(true);
                changed = true;
            }

            if (changed) {
                comboInstructionLabel.setText("🔎 Vous avez trouvé de nouveaux objets !");
                refreshTable();
            } else {
                comboInstructionLabel.setText("Il n'y a plus rien à trouver ici...");
                engine.applyPenalty(10); // Petite pénalité pour clic abusif
            }
        }
    }

    /**
     * Active le Drag & Drop sur une carte pour l'amener dans la zone de combinaison
     */
    private void enableDragDropOnCard(CardView view) {
        view.setOnDragDetected(event -> {
            Dragboard db = view.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(view.getCard().getId()));
            db.setContent(content);
            event.consume();
        });
    }

    /**
     * Configuration de la zone de Drop en bas de la fenêtre
     */
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
                    comboInstructionLabel.setText("Carte " + cardId + " en attente. Glissez la 2ème carte.");
                } else {
                    // combinaison invalide
                    Card result = engine.combineCards(cardToCombine1.getId(), droppedCard.getId());
                    if (result != null) {
                        comboInstructionLabel.setText("Réussite ! " + cardToCombine1.getId() + " + "
                                + droppedCard.getId() + " = " + result.getId());
                    } else {
                        comboInstructionLabel.setText(
                                "Erreur ! " + cardToCombine1.getId() + " + " + droppedCard.getId() + " => Pénalité !");
                    }
                    cardToCombine1 = null; // Reset
                    refreshTable(); // vide la zone
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
