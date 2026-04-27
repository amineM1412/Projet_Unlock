package sound_player;

import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

public class IHM extends JFrame {

    private final String SOUND_DIR = "sounds";
    private final char   SEP      = java.io.File.separatorChar;
     
    public IHM() {
    
        String fontName = "Arial";
	int fontType    = Font.BOLD;
	int size        = 30;

	var currentFont = new Font(fontName,fontType,size);

        var panel = new JPanel();
        
        var label = new JLabel("Clic play :-)");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(currentFont);
        
        JButton play=new JButton("PLAY");
        play.setFont(currentFont);
        play.addActionListener(ev -> new Thread(new MusicPlayer(SOUND_DIR+SEP+"sound.mp3")).start());
        
        panel.setLayout(new GridLayout(2,1));
        panel.add(label);
        panel.add(play);
        
        getContentPane().add(panel);
        
        setTitle("Sound Player");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        
	setLocationRelativeTo(null);
        
    }
}
