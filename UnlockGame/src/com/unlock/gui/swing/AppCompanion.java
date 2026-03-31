package com.unlock.gui.swing;

import com.unlock.core.GameEngine;
import com.unlock.model.Card;

import javax.swing.*;
import java.awt.*;

public class AppCompanion extends JFrame {

    private GameEngine engine;
    private JLabel timerLabel;
    private JTextField codeField;
    private JLabel statusLabel;
    
    // Timer Swing pour mettre à jour l'horloge chaque seconde
    private Timer clockTimer;

    public AppCompanion(GameEngine engine) {
        this.engine = engine;
        initUI();
        startTimer();
    }

    private void initUI() {
        setTitle("Unlock! - App Compagnon");
        setSize(350, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- HAUT : CHRONOMETRE ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.DARK_GRAY);
        
        timerLabel = new JLabel("60:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        timerLabel.setForeground(Color.RED);
        topPanel.add(timerLabel, BorderLayout.CENTER);

        // Ajout des boutons Start et Stop
        JPanel timerBtnPanel = new JPanel(new FlowLayout());
        timerBtnPanel.setOpaque(false);
        
        JButton startBtn = new JButton("▶ Start");
        JButton stopBtn = new JButton("⏸ Stop");
        
        startBtn.addActionListener(e -> {
            if (clockTimer != null && !clockTimer.isRunning()) {
                clockTimer.start();
            }
        });
        
        stopBtn.addActionListener(e -> {
            if (clockTimer != null && clockTimer.isRunning()) {
                clockTimer.stop();
            }
        });
        
        timerBtnPanel.add(startBtn);
        timerBtnPanel.add(stopBtn);
        topPanel.add(timerBtnPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTRE : PAVE NUMERIQUE ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Champ affichant le code tapé
        codeField = new JTextField(10);
        codeField.setFont(new Font("Arial", Font.BOLD, 24));
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.setEditable(false);
        centerPanel.add(codeField, BorderLayout.NORTH);

        // Grille de boutons 3x4
        JPanel padPanel = new JPanel(new GridLayout(4, 3, 5, 5));
        for (int i = 1; i <= 9; i++) {
            padPanel.add(createNumButton(String.valueOf(i)));
        }
        padPanel.add(createNumButton("C")); // Clear
        padPanel.add(createNumButton("0"));
        
        JButton validateBtn = new JButton("OK");
        validateBtn.setBackground(new Color(76, 175, 80)); // Vert
        validateBtn.setForeground(Color.WHITE);
        validateBtn.setFont(new Font("Arial", Font.BOLD, 18));
        validateBtn.addActionListener(e -> validateCode());
        padPanel.add(validateBtn);
        
        centerPanel.add(padPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- BAS : STATUT ---
        statusLabel = new JLabel("Entrez le numéro de machine puis le code.", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JButton createNumButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.addActionListener(e -> {
            if (text.equals("C")) {
                codeField.setText("");
            } else {
                codeField.setText(codeField.getText() + text);
            }
        });
        return btn;
    }

    private void startTimer() {
        clockTimer = new Timer(1000, e -> {
            int time = engine.getTimeRemaining();
            if (time > 0) {
                // Le moteur perd du temps automatiquement OU on gère la baisse ici.
                // Mieux vaut baisser le temps dans l'app et maj l'engine.
                engine.applyPenalty(1); // Décrémente de 1 seconde (normal)
                time = engine.getTimeRemaining(); 
                
                int mins = time / 60;
                int secs = time % 60;
                timerLabel.setText(String.format("%02d:%02d", mins, secs));
            } else {
                timerLabel.setText("00:00");
                statusLabel.setText("Temps écoulé ! Échec de la mission.");
                clockTimer.stop();
            }
        });
        clockTimer.start();
    }

    private void validateCode() {
        String input = codeField.getText();
        if (input.isEmpty()) return;

        // Logique de scénario hardcodée pour simplifier: Machine 30, Code 352
        // On suppose que l'utilisateur tape 30352 (Machine + Code) ou on l'affine
        // Pour Unlock, on choisit souvent la machine, on y va et on tape le code.
        // Simplifions: S'il tape 352, c'est pour la machine 30.
        
        if (input.equals("352")) {
            Card result = engine.tryCodeOnMachine(30, 352);
            if (result != null) {
                statusLabel.setText("Succès ! Vous avez débloqué la carte : " + result.getId());
                statusLabel.setForeground(new Color(0, 150, 0));
                clockTimer.stop(); // Victoire
            }
        } else {
            engine.tryCodeOnMachine(30, Integer.parseInt(input)); // Pénalité !
            statusLabel.setText("Erreur ! Pénalité de temps !");
            statusLabel.setForeground(Color.RED);
        }
        
        codeField.setText("");
    }
}
