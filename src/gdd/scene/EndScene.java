package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class EndScene extends JPanel implements GameScene {

    private final Game game;
    private final boolean victory;
    private final KeyAdapter input = new EndInput();
    private Timer timer;
    private int ticks;

    public EndScene(Game game, boolean victory) {
        this.game = game;
        this.victory = victory;
        setFocusable(true);
        setBackground(new Color(3, 24, 42));
    }

    @Override
    public void start() {
        ticks = 0;
        addKeyListener(input);
        timer = new Timer(TIMER_DELAY_MS, new GameCycle());
        timer.start();
        requestFocusInWindow();
    }

    @Override
    public void stop() {
        removeKeyListener(input);
        if (timer != null) {
            timer.stop();
        }
    }

    private void update() {
        ticks++;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(victory ? new Color(80, 220, 150) : new Color(245, 100, 100));
        g.setFont(new Font("SansSerif", Font.BOLD, 44));
        drawCentered(g, victory ? "OCEAN SAVED" : "GAME OVER", 300);

        boolean inputReady = !victory || ticks >= VICTORY_INPUT_DELAY_TICKS;
        if (inputReady) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 20));
            drawCentered(g, "Press ENTER to return to the main menu", 380);
        }
    }

    private void drawCentered(Graphics g, String text, int y) {
        int x = (getWidth() - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            update();
        }
    }

    private class EndInput extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent event) {
            boolean inputReady = !victory || ticks >= VICTORY_INPUT_DELAY_TICKS;
            if (event.getKeyCode() == KeyEvent.VK_ENTER && inputReady) {
                game.loadTitle();
            }
        }
    }
}
