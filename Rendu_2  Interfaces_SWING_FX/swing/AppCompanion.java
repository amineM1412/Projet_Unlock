
import javax.swing.*;

public class AppCompanion {

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
        setTitle("Unlock! - App Companion");
        setSize(350, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- HAUT : CHRONOMETRE ---
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.DARK_GRAY);
        timerLabel = new JLabel("60:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        timerLabel.setForeground(Color.RED);
        topPanel.add(timerLabel);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTRE : PAVE NUMERIQUE ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Grille de boutons 3x4
        JPanel padPanel = new JPanel(new GridLayout(4, 3, 5, 5));
        for (int i = 1; i <= 9; i++) {
            padPanel.add(createNumButton(String.valueOf(i)));
        }
        padPanel.add(createNumButton("C")); // Clear
        padPanel.add(createNumButton("0"));

        centerPanel.add(padPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createNumButton(String text) {
        JButton btn = new JButton(text);

        btn.addActionListener(e -> {
            if (text.equals("C")) {
                codeField.setText("");
            } else {
                codeField.setText(codeField.getText() + text);
            }

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
}
