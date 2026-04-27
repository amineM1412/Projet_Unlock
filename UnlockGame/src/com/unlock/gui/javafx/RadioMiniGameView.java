package com.unlock.gui.javafx;

import com.unlock.core.RadioMiniGame;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Vue JavaFX interactive pour le mini-jeu radio.
 * 3 molettes (sliders) + oscilloscope animé.
 */
public class RadioMiniGameView {

    private final RadioMiniGame game;
    private final Runnable onSuccess;
    private final Runnable onFailure;
    private Stage stage;

    private Label freqLabel;
    private Label signalLabel;
    private Label statusLabel;
    private Canvas oscilloscope;
    private AnimationTimer waveAnimator;
    private double wavePhase = 0;

    public RadioMiniGameView(RadioMiniGame game, Runnable onSuccess, Runnable onFailure) {
        this.game = game;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    public void show() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Mini-jeu : Transmission Radio");
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a1a;");
        root.setPadding(new Insets(15));

        // ===== TITRE =====
        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);
        Label title = new Label("CONSOLE DE COMMUNICATION");
        title.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label instr = new Label("Reglez les molettes pour capter la frequence de secours");
        instr.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        topBox.getChildren().addAll(title, instr);
        root.setTop(topBox);
        BorderPane.setMargin(topBox, new Insets(0, 0, 10, 0));

        // ===== OSCILLOSCOPE (Canvas) =====
        oscilloscope = new Canvas(500, 150);
        oscilloscope.setStyle("-fx-effect: dropshadow(three-pass-box, #00e5ff, 10, 0, 0, 0);");
        VBox canvasBox = new VBox(oscilloscope);
        canvasBox.setAlignment(Pos.CENTER);
        canvasBox.setStyle("-fx-background-color: #001a1a; -fx-border-color: #004d40; " +
                           "-fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        canvasBox.setPadding(new Insets(5));

        // ===== FRÉQUENCE AFFICHÉE =====
        freqLabel = new Label("1000 Hz");
        freqLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Courier New';");
        freqLabel.setAlignment(Pos.CENTER);

        signalLabel = new Label("Signal : 0%");
        signalLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 14px;");

        VBox displayBox = new VBox(10, canvasBox, freqLabel, signalLabel);
        displayBox.setAlignment(Pos.CENTER);
        root.setCenter(displayBox);

        // ===== MOLETTES (Sliders) =====
        VBox slidersBox = new VBox(15);
        slidersBox.setPadding(new Insets(15, 0, 0, 0));

        HBox dial1Box = createDialSlider("Milliers", 0, 9, game.getDialThousands(),
                val -> { game.setDialThousands(val); updateDisplay(); });
        HBox dial2Box = createDialSlider("Centaines", 0, 9, game.getDialHundreds(),
                val -> { game.setDialHundreds(val); updateDisplay(); });
        HBox dial3Box = createDialSlider("Dizaines/Unites", 0, 99, game.getDialTensUnits(),
                val -> { game.setDialTensUnits(val); updateDisplay(); });

        slidersBox.getChildren().addAll(dial1Box, dial2Box, dial3Box);

        // ===== BOUTONS =====
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

        Button validateBtn = new Button("Emettre");
        validateBtn.setStyle("-fx-background-color: #00897B; -fx-text-fill: white; -fx-font-size: 14px; " +
                             "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        validateBtn.setOnAction(e -> handleValidation());

        bottomBox.getChildren().addAll(statusLabel, validateBtn);

        VBox bottomVBox = new VBox(10, slidersBox, bottomBox);
        root.setBottom(bottomVBox);

        // ===== ANIMATION DE L'OSCILLOSCOPE =====
        startWaveAnimation();

        Scene scene = new Scene(root, 550, 500);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> stopWaveAnimation());
        stage.show();

        updateDisplay();
    }

    private HBox createDialSlider(String label, int min, int max, int initial,
                                   java.util.function.IntConsumer onValueChange) {
        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-pref-width: 110;");

        Slider slider = new Slider(min, max, initial);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(max <= 9 ? 1 : 10);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(max <= 9);
        slider.setPrefWidth(300);
        slider.setStyle("-fx-control-inner-background: #1a1a2e;");

        Label valLabel = new Label(String.valueOf(initial));
        valLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 40;");

        slider.valueProperty().addListener((obs, old, val) -> {
            int intVal = val.intValue();
            valLabel.setText(String.valueOf(intVal));
            onValueChange.accept(intVal);
        });

        HBox box = new HBox(10, lbl, slider, valLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void updateDisplay() {
        int freq = game.getCurrentFrequency();
        int signal = game.getSignalStrength();
        freqLabel.setText(freq + " Hz");
        signalLabel.setText("Signal : " + signal + "%");

        if (signal > 80) {
            signalLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else if (signal > 50) {
            signalLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-size: 14px;");
        } else {
            signalLabel.setStyle("-fx-text-fill: #ff5722; -fx-font-size: 14px;");
        }
    }

    private void startWaveAnimation() {
        waveAnimator = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawWave();
                wavePhase += 0.05;
            }
        };
        waveAnimator.start();
    }

    private void stopWaveAnimation() {
        if (waveAnimator != null) waveAnimator.stop();
    }

    private void drawWave() {
        GraphicsContext gc = oscilloscope.getGraphicsContext2D();
        double w = oscilloscope.getWidth();
        double h = oscilloscope.getHeight();

        // Fond sombre avec grille
        gc.setFill(Color.web("#001a1a"));
        gc.fillRect(0, 0, w, h);

        // Grille
        gc.setStroke(Color.web("#003333"));
        gc.setLineWidth(0.5);
        for (int x = 0; x < w; x += 25) gc.strokeLine(x, 0, x, h);
        for (int y = 0; y < h; y += 25) gc.strokeLine(0, y, w, y);

        // Ligne centrale
        gc.setStroke(Color.web("#004d40"));
        gc.setLineWidth(1);
        gc.strokeLine(0, h / 2, w, h / 2);

        // Onde sinusoïdale — stabilité dépend du signal
        int signal = game.getSignalStrength();
        double amplitude = 30 + (100 - signal) * 0.3;
        double frequency = 0.02 + (signal / 100.0) * 0.06;
        double noise = (100 - signal) * 0.5;

        // Couleur selon signal
        if (signal > 80) gc.setStroke(Color.web("#00ff88"));
        else if (signal > 50) gc.setStroke(Color.web("#ffeb3b"));
        else gc.setStroke(Color.web("#ff5722"));

        gc.setLineWidth(2);
        gc.beginPath();
        for (int x = 0; x < w; x++) {
            double y = h / 2 + amplitude * Math.sin(frequency * x + wavePhase)
                       + noise * Math.sin(0.1 * x + wavePhase * 3) * Math.random();
            if (x == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
    }

    private void handleValidation() {
        if (game.validate()) {
            statusLabel.setText("SUCCES ! FREQUENCE CAPTEE ! Signal de secours recu !");
            statusLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 14px; -fx-font-weight: bold;");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                stopWaveAnimation();
                stage.close();
                if (onSuccess != null) onSuccess.run();
            });
            pause.play();
        } else {
            int dist = game.getDistanceToTarget();
            String hint;
            if (dist < 50) hint = "Tres proche !";
            else if (dist < 200) hint = "Presque...";
            else if (dist < 1000) hint = "Pas encore...";
            else hint = "Tres loin de la cible.";

            statusLabel.setText("ERREUR : Frequence incorrecte. " + hint + " Penalite -30s O2");
            statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 13px;");
            if (onFailure != null) onFailure.run();
        }
    }
}
