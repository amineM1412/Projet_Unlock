package com.unlock.gui.javafx;

import com.unlock.core.GameEngine;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXMain extends Application {

    private static GameEngine engine;

    // Permet au Launcher externe (ou à Swing) de passer le moteur
    public static void setGameEngine(GameEngine eng) {
        engine = eng;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TableJeu.fxml"));
        Parent root = loader.load();

        // Ajout du contrôleur
        TableJeuController controller = loader.getController();
        if (engine == null) {
            engine = new GameEngine(); // Fallback si lancé seul
        }
        controller.setGameEngine(engine);

        primaryStage.setTitle("Unlock! - Table de Jeu (JavaFX)");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
