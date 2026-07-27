package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class EndScene extends JPanel implements GameScene {

    private static final String GAME_OVER_ART_PATH =
            "src/images/background/game-over.png";
    private static final String VICTORY_ART_PATH =
            "src/images/background/victory-v4.png";
    private static final ImageIcon gameOverArt =
            new ImageIcon(GAME_OVER_ART_PATH);
    private static final ImageIcon victoryArt =
            new ImageIcon(VICTORY_ART_PATH);
    private static final int PROMPT_FLASH_TICKS = 28;

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

        if (victory) {
            drawVictory(g);
        } else {
            drawGameOver(g);
        }
    }

    private void drawGameOver(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.drawImage(gameOverArt.getImage(), 0, 0,
                getWidth(), getHeight(), null);
        g.setColor(new Color(0, 5, 14, 48));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(56)));
        drawCenteredShadowed(g, "GAME OVER", 255,
                new Color(244, 111, 106));

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(14)));
        drawCenteredShadowed(g, "LOST TO THE DEPTHS", 292,
                new Color(176, 211, 211));

        if ((ticks / PROMPT_FLASH_TICKS) % 2 == 0) {
            g.setFont(new Font("Monospaced", Font.BOLD,
                    scaledFontSize(16)));
            drawCenteredShadowed(g, "PRESS ENTER TO RETURN TO MAIN MENU",
                    382, new Color(255, 229, 151));
        }

        g.dispose();
    }

    private void drawVictory(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.drawImage(victoryArt.getImage(), 0, 0,
                getWidth(), getHeight(), null);
        g.setColor(new Color(0, 10, 20, 28));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(52)));
        drawCenteredShadowed(g, "OCEAN SAVED", 250,
                new Color(255, 236, 137));

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(14)));
        drawCenteredShadowed(g, "LIFE RETURNS TO THE DEPTHS", 288,
                new Color(171, 241, 210));

        boolean inputReady = ticks >= VICTORY_INPUT_DELAY_TICKS;
        if (inputReady
                && (ticks / PROMPT_FLASH_TICKS) % 2 == 0) {
            g.setFont(new Font("Monospaced", Font.BOLD,
                    scaledFontSize(16)));
            drawCenteredShadowed(g, "PRESS ENTER TO RETURN TO MAIN MENU",
                    330, new Color(244, 250, 232));
        }

        g.dispose();
    }

    private void drawCenteredShadowed(Graphics2D g, String text, int y,
            Color color) {
        int x = (getWidth() - g.getFontMetrics().stringWidth(text)) / 2;
        g.setColor(new Color(0, 8, 18, 230));
        g.drawString(text, x + 4, y + 4);
        g.setColor(color);
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
