package find_the_animal;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.Timer;
import javax.swing.JOptionPane;

public class IHM extends JFrame {

    private final String SOUND_DIR = "sounds";
    private final char   SEP      = java.io.File.separatorChar;
    private final int    NB_TURNS = 5;
    
    private final JLabel instructions, temps, timeDesc, score, scoreDesc;
    private final JButton play;
    private final ArrayList<JButton> buttons;
    private final ArrayList<String> paths;
    private final RuleManager manager;
    
    private int   turn;
    private Timer playTimer;
    private Timer timeTimer;
       
    public IHM() {
    
        turn=0;
        
        buttons = new ArrayList<>();
        
        buttons.add(new JButton("Cat"));
        buttons.add(new JButton("Dog"));
        buttons.add(new JButton("Eagle"));
        buttons.add(new JButton("Pork"));
        buttons.add(new JButton("Ant"));
        
        buttons.get(0).addActionListener(event->check(0));
        buttons.get(1).addActionListener(event->check(1));
        buttons.get(2).addActionListener(event->check(2));
        buttons.get(3).addActionListener(event->check(3));
        buttons.get(4).addActionListener(event->check(4));
        
        for (javax.swing.JButton bouton : buttons) {
            bouton.setEnabled(false);
        }
        
        paths = new ArrayList<>();
        paths.add("01_Cat.mp3");
        paths.add("02_Dog.mp3");
        paths.add("03_Eagle.mp3");
        paths.add("04_Pork.mp3");
        paths.add("05_Ant.mp3");
        
        manager=new RuleManager(paths.size());
        
        Container container=getContentPane();
        
        instructions=new JLabel("Click on 'PLAY' and find the correct animal five times (5s each)");
             
        container.add(instructions,BorderLayout.NORTH);
        
        play=new JButton("PLAY");
        play.addActionListener(event -> playSong());
        JPanel central_panel=new JPanel();
        central_panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        central_panel.add(play);
        container.add(central_panel);
        
        JPanel lower_panel = new JPanel();
        lower_panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        for(var bouton : buttons) lower_panel.add(bouton);
        
        container.add(lower_panel,BorderLayout.SOUTH);
        
        JPanel left_panel=new JPanel();
        left_panel.setLayout(new GridLayout(2,1));
        timeDesc=new JLabel("Time");
        left_panel.add(timeDesc);
        temps=new JLabel("4");
        left_panel.add(temps);
        container.add(left_panel,BorderLayout.WEST);
        
        JPanel right_panel=new JPanel();
        right_panel.setLayout(new GridLayout(2,1));
        scoreDesc=new JLabel("Score");
        right_panel.add(scoreDesc);
        score=new JLabel("0");
        right_panel.add(score);
        container.add(right_panel,BorderLayout.EAST);
        
        updateShape();
        
        setSize(1200,256);
        setTitle("Which animal cries?");
	setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private void check(int choice) {
        if(manager.getCurrent()==choice) {
            score.setText(Integer.toString(Integer.parseInt(score.getText())+1));
        }
        for (Iterator<JButton> it = buttons.iterator(); it.hasNext();) {
            javax.swing.JButton bouton = it.next();
            bouton.setEnabled(false);
        }
    }
    
    private void play(int choice) {
        new Thread(new MusicPlayer(SOUND_DIR+SEP+paths.get(choice))).start();
        for (javax.swing.JButton button : buttons) {
            button.setEnabled(true);
        }
    }
    
    private void playSong() {
        
        score.setText("0");
        play.setEnabled(false);
        manager.setCurrent();
        play(manager.getCurrent());
        turn++;
        
        int play_delay = 5000;
        
        playTimer = new Timer( play_delay, event ->
                { 
                    if(turn==NB_TURNS){
                        int last_score=Integer.parseInt(score.getText());
                        timeTimer.stop();
                        playTimer.stop();
                        for (javax.swing.JButton bouton : buttons) {
                            bouton.setEnabled(false);
                        }
                        play.setEnabled(true);
                        
                        JOptionPane.showMessageDialog(null, "Your score is "+last_score, "Results", JOptionPane.INFORMATION_MESSAGE);
                        turn=0;
                        temps.setText("4");
                    } else {
                    manager.setCurrent();
                    play(manager.getCurrent());
                    turn++;
                    }
                 } );
        playTimer.setRepeats(true);
        
        int time_delay = 1000;
        timeTimer = new Timer( time_delay, event ->
                { 
                    int value = Integer.parseInt(temps.getText())-1;
                    if(value == -1) {
                        value=4;
                    }
                    temps.setText(Integer.toString(value));
                    
                 } );
        timeTimer.setRepeats(true);
        
        playTimer.start();
        timeTimer.start();
        
    }
    
    private void updateShape() {

        String fontName = "Arial";
	int fontType    = Font.BOLD;
	int size        = 30;

	var currentFont = new Font(fontName,fontType,size);

	instructions.setHorizontalAlignment(JLabel.CENTER);
	instructions.setFont(currentFont);
        
        size=40;
        currentFont = new Font(fontName,fontType,size);
        
        timeDesc.setHorizontalAlignment(JLabel.CENTER);
        timeDesc.setFont(currentFont);
        temps.setHorizontalAlignment(JLabel.CENTER);
        temps.setFont(currentFont);
        scoreDesc.setHorizontalAlignment(JLabel.CENTER);
        scoreDesc.setFont(currentFont);
        score.setHorizontalAlignment(JLabel.CENTER);
        score.setFont(currentFont);
         
        size=50;
        currentFont = new Font(fontName,fontType,size);
        
        play.setFont(currentFont);
        
        for (JButton bouton : buttons) {
            bouton.setFont(currentFont);
        }
    }
}
