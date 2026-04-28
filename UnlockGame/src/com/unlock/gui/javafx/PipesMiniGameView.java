package com.unlock.gui.javafx;

import com.unlock.core.PipesMiniGame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Vue JavaFX interactive pour le mini-jeu des tuyaux.
 * Grille 4x4 cliquable avec rotation des tuiles.
 */
public class PipesMiniGameView {

    private final PipesMiniGame game;
    private final Runnable onSuccess;
    private final Runnable onFailure;
    private Stage stage;

    private static final int TILE_SIZE = 80;
    private static final int GRID_PX = TILE_SIZE * PipesMiniGame.GRID_SIZE;

    private Canvas gridCanvas;
    private Label statusLabel;

    public PipesMiniGameView(PipesMiniGame game, Runnable onSuccess, Runnable onFailure) {
        this.game = game;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    public void show() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Mini-jeu : Rotation de tuyaux");
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1b1b2f;");
        root.setPadding(new Insets(15));

        // ===== TITRE =====
        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);
        Label title = new Label("SYSTEME DE REFROIDISSEMENT");
        title.setStyle("-fx-text-fill: #64ffda; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label instr = new Label("Cliquez sur les tuiles pour les tourner - Reliez entree -> sortie");
        instr.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        topBox.getChildren().addAll(title, instr);
        root.setTop(topBox);
        BorderPane.setMargin(topBox, new Insets(0, 0, 10, 0));

        // ===== GRILLE (Canvas) =====
        gridCanvas = new Canvas(GRID_PX + 2, GRID_PX + 2);
        gridCanvas.setStyle("-fx-effect: dropshadow(three-pass-box, #64ffda, 8, 0, 0, 0);");

        gridCanvas.setOnMouseClicked(e -> {
            int col = (int)(e.getX() / TILE_SIZE);
            int row = (int)(e.getY() / TILE_SIZE);
            if (row >= 0 && row < PipesMiniGame.GRID_SIZE && col >= 0 && col < PipesMiniGame.GRID_SIZE) {
                game.rotateTile(row, col);
                drawGrid();
            }
        });

        StackPane canvasPane = new StackPane(gridCanvas);
        canvasPane.setAlignment(Pos.CENTER);

        // Indicateurs d'entrée/sortie
        VBox centerBox = new VBox(5);
        centerBox.setAlignment(Pos.CENTER);

        HBox indicators = new HBox(GRID_PX - 100);
        indicators.setAlignment(Pos.CENTER);
        Label entryLabel = new Label("v ENTREE");
        entryLabel.setStyle("-fx-text-fill: #00e676; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label exitLabel = new Label("SORTIE v");
        exitLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px; -fx-font-weight: bold;");
        indicators.getChildren().addAll(entryLabel, exitLabel);

        centerBox.getChildren().addAll(indicators, canvasPane);
        root.setCenter(centerBox);

        // ===== BOUTONS =====
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));

        statusLabel = new Label("Cliquez sur les tuiles pour les tourner");
        statusLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

        Button validateBtn = new Button("Verifier le circuit");
        validateBtn.setStyle("-fx-background-color: #00897B; -fx-text-fill: white; -fx-font-size: 14px; " +
                             "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        validateBtn.setOnAction(e -> handleValidation());

        Button resetBtn = new Button("Reinitialiser");
        resetBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; " +
                          "-fx-padding: 8 20; -fx-background-radius: 5;");
        resetBtn.setOnAction(e -> { game.reset(); drawGrid();
            statusLabel.setText("Grille reinitialisee.");
            statusLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;"); });

        bottomBox.getChildren().addAll(statusLabel, resetBtn, validateBtn);
        root.setBottom(bottomBox);

        drawGrid();

        Scene scene = new Scene(root, GRID_PX + 80, GRID_PX + 200);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Dessine la grille complète sur le canvas
     */
    private void drawGrid() {
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#162447"));
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        for (int row = 0; row < PipesMiniGame.GRID_SIZE; row++) {
            for (int col = 0; col < PipesMiniGame.GRID_SIZE; col++) {
                drawTile(gc, row, col);
            }
        }

        // Marquer entrée et sortie
        gc.setFill(Color.web("#00e67644"));
        gc.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        gc.setFill(Color.web("#ff525244"));
        gc.fillRect((PipesMiniGame.GRID_SIZE - 1) * TILE_SIZE, (PipesMiniGame.GRID_SIZE - 1) * TILE_SIZE,
                    TILE_SIZE, TILE_SIZE);
    }

    /**
     * Dessine une tuile individuelle
     */
    private void drawTile(GraphicsContext gc, int row, int col) {
        double x = col * TILE_SIZE;
        double y = row * TILE_SIZE;
        int type = game.getTileType(row, col);

        // Fond de la tuile
        gc.setFill(type == PipesMiniGame.EMPTY ? Color.web("#0d1b2a") : Color.web("#1f3044"));
        gc.fillRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);

        // Bordure
        gc.setStroke(Color.web("#2e4057"));
        gc.setLineWidth(1);
        gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);

        if (type == PipesMiniGame.EMPTY) return;

        // Dessiner le tuyau
        boolean[] dirs = game.getOpenDirections(row, col);
        double cx = x + TILE_SIZE / 2.0;
        double cy = y + TILE_SIZE / 2.0;
        double pipeWidth = 14;

        gc.setStroke(Color.web("#64ffda"));
        gc.setLineWidth(pipeWidth);

        // Dessiner les "bras" du tuyau vers le centre
        if (dirs[0]) gc.strokeLine(cx, y + 2, cx, cy);          // haut
        if (dirs[1]) gc.strokeLine(cx, cy, x + TILE_SIZE - 2, cy); // droite
        if (dirs[2]) gc.strokeLine(cx, cy, cx, y + TILE_SIZE - 2); // bas
        if (dirs[3]) gc.strokeLine(x + 2, cy, cx, cy);          // gauche

        // Point central
        gc.setFill(Color.web("#64ffda"));
        gc.fillOval(cx - pipeWidth / 2, cy - pipeWidth / 2, pipeWidth, pipeWidth);

        // Contour intérieur des tuyaux (effet de profondeur)
        gc.setStroke(Color.web("#004d40"));
        gc.setLineWidth(4);
        if (dirs[0]) gc.strokeLine(cx, y + 6, cx, cy);
        if (dirs[1]) gc.strokeLine(cx, cy, x + TILE_SIZE - 6, cy);
        if (dirs[2]) gc.strokeLine(cx, cy, cx, y + TILE_SIZE - 6);
        if (dirs[3]) gc.strokeLine(x + 6, cy, cx, cy);
    }

    private void handleValidation() {
        if (game.validate()) {
            statusLabel.setText("SUCCES ! CIRCUIT RETABLI ! Refroidissement actif !");
            statusLabel.setStyle("-fx-text-fill: #64ffda; -fx-font-size: 15px; -fx-font-weight: bold;");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                stage.close();
                if (onSuccess != null) onSuccess.run();
            });
            pause.play();
        } else {
            statusLabel.setText("ERREUR : Le circuit n'est pas connecte. Penalite -30s O2");
            statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 13px;");
            if (onFailure != null) onFailure.run();
        }
    }
}
