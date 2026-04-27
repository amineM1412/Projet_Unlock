package com.unlock.gui.javafx;

import com.unlock.core.CablesMiniGame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Vue JavaFX interactive pour le mini-jeu des câbles.
 * Le joueur glisse les câbles vers les connecteurs correspondants.
 */
public class CablesMiniGameView {

    private final CablesMiniGame game;
    private final Runnable onSuccess;
    private final Runnable onFailure;
    private Stage stage;

    // Couleurs des câbles
    private static final Color[] CABLE_COLORS = {
        Color.web("#E53935"),  // Rouge
        Color.web("#FDD835"),  // Jaune
        Color.web("#1E88E5"),  // Bleu
        Color.web("#43A047")   // Vert
    };
    private static final String[] CABLE_NAMES = {"Rouge", "Jaune", "Bleu", "Vert"};

    // Éléments UI
    private Circle[] cableCircles;
    private Circle[] connectorCircles;
    private Line[] connectionLines;
    private int selectedCable = -1;
    private Label statusLabel;

    public CablesMiniGameView(CablesMiniGame game, Runnable onSuccess, Runnable onFailure) {
        this.game = game;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    /**
     * Affiche la fenêtre du mini-jeu
     */
    public void show() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Mini-jeu : Relier les cables");
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setPadding(new Insets(20));

        // ===== TITRE ET INSTRUCTIONS =====
        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);
        Label title = new Label("BOITIER D'ALIMENTATION");
        title.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label instructions = new Label("Cliquez sur un cable (gauche), puis sur un connecteur (droite)");
        instructions.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        topBox.getChildren().addAll(title, instructions);
        root.setTop(topBox);
        BorderPane.setMargin(topBox, new Insets(0, 0, 15, 0));

        // ===== ZONE DE JEU =====
        Pane gamePane = new Pane();
        gamePane.setPrefSize(500, 320);
        gamePane.setMinHeight(320);
        gamePane.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; " +
                          "-fx-border-color: #0f3460; -fx-border-width: 2; -fx-border-radius: 10;");

        cableCircles = new Circle[4];
        connectorCircles = new Circle[4];
        connectionLines = new Line[4];

        for (int i = 0; i < 4; i++) {
            // Câbles (côté gauche)
            double cableY = 40 + i * 65;
            Circle cable = new Circle(60, cableY, 20, CABLE_COLORS[i]);
            cable.setStroke(Color.WHITE);
            cable.setStrokeWidth(2);
            cable.setCursor(javafx.scene.Cursor.HAND);
            final int cableIdx = i;
            cable.setOnMouseClicked(e -> selectCable(cableIdx));

            Label cableLabel = new Label(CABLE_NAMES[i]);
            cableLabel.setLayoutX(10);
            cableLabel.setLayoutY(cableY - 8);
            cableLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
            cableLabel.setMouseTransparent(true);

            cableCircles[i] = cable;

            // Connecteurs (côté droit)
            Circle connector = new Circle(440, cableY, 20);
            connector.setFill(Color.web("#333333"));
            connector.setStroke(Color.web("#666666"));
            connector.setStrokeWidth(2);
            connector.setCursor(javafx.scene.Cursor.HAND);
            final int connIdx = i;
            connector.setOnMouseClicked(e -> connectTo(connIdx));

            Label connLabel = new Label(String.valueOf(i + 1));
            connLabel.setLayoutX(460);
            connLabel.setLayoutY(cableY - 8);
            connLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            connLabel.setMouseTransparent(true);

            connectorCircles[i] = connector;

            // Ligne de connexion (invisible par défaut)
            Line line = new Line(80, cableY, 420, cableY);
            line.setStroke(CABLE_COLORS[i]);
            line.setStrokeWidth(3);
            line.setVisible(false);
            line.setMouseTransparent(true);
            line.getStrokeDashArray().addAll(10d, 5d);
            connectionLines[i] = line;

            gamePane.getChildren().addAll(line, cable, cableLabel, connector, connLabel);
        }

        root.setCenter(gamePane);

        // ===== BOUTONS EN BAS =====
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));

        statusLabel = new Label("Selectionnez un cable...");
        statusLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

        Button validateBtn = new Button("Valider");
        validateBtn.setStyle("-fx-background-color: #43A047; -fx-text-fill: white; -fx-font-size: 14px; " +
                             "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        validateBtn.setOnAction(e -> handleValidation());

        Button resetBtn = new Button("Reinitialiser");
        resetBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; " +
                          "-fx-padding: 8 20; -fx-background-radius: 5;");
        resetBtn.setOnAction(e -> handleReset());

        bottomBox.getChildren().addAll(statusLabel, resetBtn, validateBtn);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 540, 480);
        stage.setScene(scene);
        stage.show();
    }

    private void selectCable(int index) {
        selectedCable = index;
        statusLabel.setText("Cable " + CABLE_NAMES[index] + " selectionne -> cliquez un connecteur");
        statusLabel.setStyle("-fx-text-fill: " + toHexString(CABLE_COLORS[index]) + "; -fx-font-size: 13px;");

        // Highlight
        for (int i = 0; i < 4; i++) {
            cableCircles[i].setStrokeWidth(i == index ? 4 : 2);
        }
    }

    private void connectTo(int connectorIndex) {
        if (selectedCable < 0) {
            statusLabel.setText("Selectionnez d'abord un cable !");
            statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 13px;");
            return;
        }

        game.connect(selectedCable, connectorIndex);

        // Mettre à jour les lignes visuelles
        refreshLines();

        statusLabel.setText("Cable " + CABLE_NAMES[selectedCable] + " -> Connecteur " + (connectorIndex + 1));
        statusLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        selectedCable = -1;
        for (Circle c : cableCircles) c.setStrokeWidth(2);
    }

    private void refreshLines() {
        int[] connections = game.getPlayerConnections();
        for (int i = 0; i < 4; i++) {
            if (connections[i] >= 0) {
                double endY = 40 + connections[i] * 65;
                connectionLines[i].setEndY(endY);
                connectionLines[i].setVisible(true);
            } else {
                connectionLines[i].setVisible(false);
            }
        }
    }

    private void handleValidation() {
        if (game.validate()) {
            statusLabel.setText("SUCCES ! Cables correctement relies !");
            statusLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 15px; -fx-font-weight: bold;");

            // Fermer après un court délai
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                stage.close();
                if (onSuccess != null) onSuccess.run();
            });
            pause.play();
        } else {
            int correct = game.getCorrectCount();
            statusLabel.setText("ERREUR ! " + correct + "/4 cables corrects. Penalite -30s O2");
            statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 13px;");
            if (onFailure != null) onFailure.run();
        }
    }

    private void handleReset() {
        game.reset();
        selectedCable = -1;
        for (Line l : connectionLines) l.setVisible(false);
        for (Circle c : cableCircles) c.setStrokeWidth(2);
        statusLabel.setText("Selectionnez un cable...");
        statusLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }
}
