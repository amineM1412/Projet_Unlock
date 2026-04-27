package com.unlock.core;

import com.unlock.gui.javafx.JavaFXMain;
import com.unlock.gui.swing.AppCompanion;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("Lancement de Unlock! (Version Hybride Swing/JavaFX)");

        // 1. Création du moteur de jeu (partagé par les deux interfaces)
        GameEngine engine = new GameEngine();

        // 2. Lancement de l'interface JavaFX (Table de jeu) dans son propre Thread
        new Thread(() -> {
            JavaFXMain.setGameEngine(engine);
            javafx.application.Application.launch(JavaFXMain.class, args);
        }).start();

        // 3. Lancement de l'interface Swing (App Smartphone) dans le Thread AWT
        SwingUtilities.invokeLater(() -> {
            AppCompanion app = new AppCompanion(engine);
            // On la positionne à droite de l'écran pour ne pas cacher la table
            app.setLocation(1150, 100); 
            app.setVisible(true);
        });
    }
}
