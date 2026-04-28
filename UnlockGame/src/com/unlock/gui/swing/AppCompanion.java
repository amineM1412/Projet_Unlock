package com.unlock.gui.swing;

import com.unlock.core.GameEngine;
import com.unlock.model.Card;

import javax.swing.*;
import java.awt.*;

/**
 * Application Compagnon (Swing) pour Unlock! -- "Signal Fantome"
 * Affiche le chronometre, la jauge d'oxygene, le pave numerique,
 * et permet d'entrer les codes sur les cartes CODE (jaunes).
 *
 * Fonctionnement en 2 etapes :
 *  1. L'utilisateur entre le numero de la carte CODE decouverte
 *  2. Puis il entre le code a 3 ou 4 chiffres pour cette carte
 */
public class AppCompanion extends JFrame {

    private GameEngine engine;

    // UI -- Timer & Oxygene
    private JLabel timerLabel;
    private JLabel oxygenLabel;
    private JProgressBar oxygenBar;
    private JLabel actLabel;

    // UI -- Pave numerique
    private JTextField inputField;
    private JLabel statusLabel;
    private JLabel stepLabel;

    // Etat du processus en 2 etapes
    private int selectedMachineId = -1;  // -1 = pas de carte selectionnee (etape 1)

    // Timer Swing
    private Timer clockTimer;

    public AppCompanion(GameEngine engine) {
        this.engine = engine;
        initUI();
        startTimer();
    }

    private void initUI() {
        setTitle("Unlock! -- App Compagnon");
        setSize(380, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(26, 26, 46));

        // =============== HAUT : CHRONO + O2 ===============
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(13, 27, 42));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Acte
        actLabel = new JLabel("ACTE 1", SwingConstants.CENTER);
        actLabel.setFont(new Font("Arial", Font.BOLD, 12));
        actLabel.setForeground(new Color(100, 255, 218));
        actLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(actLabel);
        topPanel.add(Box.createVerticalStrut(5));

        // Label titre
        JLabel titleLabel = new JLabel("CHRONOMETRE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(new Color(150, 150, 150));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel);

        // Timer
        timerLabel = new JLabel("60:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 52));
        timerLabel.setForeground(new Color(79, 195, 247));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(timerLabel);

        // Start / Pause
        JPanel timerBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        timerBtnPanel.setOpaque(false);

        JButton startBtn = createStyledButton("Start", new Color(67, 160, 71));
        JButton stopBtn = createStyledButton("Pause", new Color(255, 152, 0));

        startBtn.addActionListener(e -> {
            if (clockTimer != null && !clockTimer.isRunning()) {
                clockTimer.start();
                engine.setPaused(false);  // Deblocage des interactions
            }
        });
        stopBtn.addActionListener(e -> {
            if (clockTimer != null && clockTimer.isRunning()) {
                clockTimer.stop();
                engine.setPaused(true);   // Blocage des interactions (anti-triche)
            }
        });

        timerBtnPanel.add(startBtn);
        timerBtnPanel.add(stopBtn);
        topPanel.add(timerBtnPanel);

        // Jauge d'oxygene
        topPanel.add(Box.createVerticalStrut(8));
        JLabel o2Title = new JLabel("OXYGENE", SwingConstants.CENTER);
        o2Title.setFont(new Font("Arial", Font.BOLD, 11));
        o2Title.setForeground(new Color(150, 150, 150));
        o2Title.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(o2Title);

        oxygenBar = new JProgressBar(0, 100);
        oxygenBar.setValue(100);
        oxygenBar.setStringPainted(true);
        oxygenBar.setString("100%");
        oxygenBar.setFont(new Font("Arial", Font.BOLD, 12));
        oxygenBar.setForeground(new Color(0, 230, 118));
        oxygenBar.setBackground(new Color(30, 30, 50));
        oxygenBar.setMaximumSize(new Dimension(340, 22));
        oxygenBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(oxygenBar);

        oxygenLabel = new JLabel("40:00 restantes", SwingConstants.CENTER);
        oxygenLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        oxygenLabel.setForeground(new Color(150, 150, 150));
        oxygenLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(oxygenLabel);

        add(topPanel, BorderLayout.NORTH);

        // =============== CENTRE : PAVE NUMERIQUE (2 etapes) ===============
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        centerPanel.setBackground(new Color(26, 26, 46));

        // Etape courante
        stepLabel = new JLabel("Etape 1 : Entrez le numero de la carte CODE", SwingConstants.CENTER);
        stepLabel.setForeground(new Color(100, 255, 218));
        stepLabel.setFont(new Font("Arial", Font.BOLD, 12));
        centerPanel.add(stepLabel, BorderLayout.NORTH);

        // Champ de saisie
        inputField = new JTextField(10);
        inputField.setFont(new Font("Monospaced", Font.BOLD, 28));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setEditable(false);
        inputField.setBackground(new Color(22, 36, 71));
        inputField.setForeground(new Color(0, 255, 136));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 255, 218), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.setOpaque(false);
        codePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        codePanel.add(inputField, BorderLayout.CENTER);

        // Grille de boutons 3x4
        JPanel padPanel = new JPanel(new GridLayout(4, 3, 5, 5));
        padPanel.setOpaque(false);
        for (int i = 1; i <= 9; i++) {
            padPanel.add(createNumButton(String.valueOf(i)));
        }
        padPanel.add(createNumButton("C"));
        padPanel.add(createNumButton("0"));

        JButton validateBtn = createStyledButton("OK", new Color(0, 137, 123));
        validateBtn.setFont(new Font("Arial", Font.BOLD, 20));
        validateBtn.addActionListener(e -> handleOkButton());
        padPanel.add(validateBtn);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false);
        inputPanel.add(codePanel, BorderLayout.NORTH);
        inputPanel.add(padPanel, BorderLayout.CENTER);
        centerPanel.add(inputPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // =============== BAS : STATUT ===============
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(new Color(13, 27, 42));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        statusLabel = new JLabel("Entrez le numero de la carte a code decouverte.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(180, 180, 180));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(statusLabel);

        // Bouton Annuler pour revenir a l'etape 1
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        cancelPanel.setOpaque(false);
        JButton cancelBtn = createStyledButton("Annuler", new Color(120, 60, 60));
        cancelBtn.addActionListener(e -> resetToStep1());
        cancelPanel.add(cancelBtn);
        bottomPanel.add(cancelPanel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ================================================================
    //  GESTION 2 ETAPES
    // ================================================================

    /**
     * Bouton OK : si etape 1, valide le numero de carte. Si etape 2, valide le code.
     */
    private void handleOkButton() {
        String input = inputField.getText();
        if (input.isEmpty()) return;

        // Anti-triche : impossible d'entrer un code si le timer est en pause
        if (engine.isPaused()) {
            statusLabel.setText("[PAUSE] Relancez le timer avant d'entrer un code !");
            statusLabel.setForeground(new Color(255, 152, 0));
            inputField.setText("");
            return;
        }

        if (selectedMachineId == -1) {
            validateCardNumber(input);
        } else {
            validateCode(input);
        }
    }

    /**
     * Etape 1 : Verifie que le numero de carte est une carte CODE visible
     */
    private void validateCardNumber(String input) {
        int cardId;
        try {
            cardId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            statusLabel.setText("[ERREUR] Entrez un numero valide.");
            statusLabel.setForeground(Color.RED);
            inputField.setText("");
            return;
        }

        Card card = engine.getDeck().get(cardId);
        if (card == null) {
            statusLabel.setText("[ERREUR] Cette carte n'existe pas.");
            statusLabel.setForeground(Color.RED);
            inputField.setText("");
            return;
        }

        if (!card.isVisible()) {
            statusLabel.setText("[ERREUR] Vous n'avez pas encore decouvert cette carte.");
            statusLabel.setForeground(new Color(255, 152, 0));
            inputField.setText("");
            return;
        }

        if (!card.requiresCode()) {
            statusLabel.setText("[ERREUR] La carte " + cardId + " n'est pas une carte a code.");
            statusLabel.setForeground(new Color(255, 152, 0));
            inputField.setText("");
            return;
        }

        // Carte CODE valide et visible -> passer a l'etape 2
        selectedMachineId = cardId;
        inputField.setText("");
        stepLabel.setText("Etape 2 : Entrez le code pour la carte " + cardId);
        stepLabel.setForeground(new Color(255, 193, 7));
        statusLabel.setText("Carte " + cardId + " selectionnee. Entrez le code.");
        statusLabel.setForeground(new Color(100, 255, 218));

        // Changer la couleur de la bordure du champ
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    /**
     * Etape 2 : Envoie le code entre au moteur de jeu
     */
    private void validateCode(String input) {
        Card result = engine.tryCode(selectedMachineId, input);
        if (result != null) {
            statusLabel.setText("[OK] Code accepte ! Carte " + result.getId() + " debloquee !");
            statusLabel.setForeground(new Color(0, 230, 118));

            if (engine.isGameWon()) {
                clockTimer.stop();
                timerLabel.setForeground(new Color(0, 230, 118));
            }
        } else {
            statusLabel.setText("[ERREUR] Code incorrect pour carte " + selectedMachineId + " ! (-60s O2)");
            statusLabel.setForeground(Color.RED);
        }

        inputField.setText("");
        // Revenir a l'etape 1
        resetToStep1();
    }

    /**
     * Remet l'interface a l'etape 1
     */
    private void resetToStep1() {
        selectedMachineId = -1;
        inputField.setText("");
        stepLabel.setText("Etape 1 : Entrez le numero de la carte CODE");
        stepLabel.setForeground(new Color(100, 255, 218));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 255, 218), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    // ================================================================
    //  BOUTONS STYLISES
    // ================================================================

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createNumButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setBackground(new Color(31, 48, 68));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (text.equals("C")) {
                inputField.setText("");
            } else {
                if (inputField.getText().length() < 4) {
                    inputField.setText(inputField.getText() + text);
                }
            }
        });
        return btn;
    }

    // ================================================================
    //  TIMER
    // ================================================================

    private void startTimer() {
        clockTimer = new Timer(1000, e -> {
            int time = engine.getTimeRemaining();
            int o2 = engine.getOxygenRemaining();

            if (!engine.isGameOver() && time > 0 && o2 > 0) {
                engine.tick();
                time = engine.getTimeRemaining();
                o2 = engine.getOxygenRemaining();

                // Timer
                int mins = time / 60;
                int secs = time % 60;
                timerLabel.setText(String.format("%02d:%02d", mins, secs));

                // Couleur du timer
                if (time > 1800) {
                    timerLabel.setForeground(new Color(79, 195, 247));
                } else if (time > 600) {
                    timerLabel.setForeground(new Color(255, 152, 0));
                } else {
                    timerLabel.setForeground(Color.RED);
                }

                // Oxygene
                int o2Pct = engine.getOxygenPercentage();
                oxygenBar.setValue(o2Pct);
                oxygenBar.setString(o2Pct + "%");
                int o2Min = o2 / 60;
                int o2Sec = o2 % 60;
                oxygenLabel.setText(String.format("%02d:%02d restantes", o2Min, o2Sec));

                if (o2Pct > 50) {
                    oxygenBar.setForeground(new Color(0, 230, 118));
                } else if (o2Pct > 25) {
                    oxygenBar.setForeground(new Color(255, 152, 0));
                } else {
                    oxygenBar.setForeground(Color.RED);
                }

                // Acte
                actLabel.setText("ACTE " + engine.getCurrentAct());

            } else {
                clockTimer.stop();
                if (engine.isGameWon()) {
                    timerLabel.setForeground(new Color(0, 230, 118));
                    statusLabel.setText("VICTOIRE ! SOS envoye !");
                    statusLabel.setForeground(new Color(0, 230, 118));
                } else {
                    timerLabel.setText("00:00");
                    timerLabel.setForeground(Color.RED);
                    statusLabel.setText("Echec de la mission - Oxygene epuise !");
                    statusLabel.setForeground(Color.RED);
                }
            }
        });
        clockTimer.start();
    }
}
