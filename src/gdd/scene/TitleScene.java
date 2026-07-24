package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

public class TitleScene extends JPanel implements GameScene {

    private final Game game;
    private final KeyAdapter input = new TitleInput();

    public TitleScene(Game game) {
        this.game = game;
        setFocusable(true);
        setBackground(new Color(4, 32, 56));
    }

    @Override
    public void start() {
        addKeyListener(input);
        requestFocusInWindow();
    }

    @Override
    public void stop() {
        removeKeyListener(input);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawPlaceholderOcean(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 46));
        drawCentered(g, "OCEAN INVADERS", 190);

        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        drawCentered(g, "[1] Start Game", 340);
        drawCentered(g, "[2] Start From Stage 2", 382);
        drawCentered(g, "[Q] Quit", 424);

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        drawCentered(g, "Team Members: [Name]  [Name]  [Name]", 610);
        g.dispose();
    }

    private void drawPlaceholderOcean(Graphics2D g) {
        g.setColor(new Color(6, 52, 86));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(15, 92, 126));
        for (int y = 60; y < getHeight(); y += 80) {
            g.fillRect(0, y, getWidth(), 3);
        }

        g.setColor(new Color(98, 198, 220));
        for (int x = 40; x < getWidth(); x += 95) {
            g.drawOval(x, 70 + (x % 130), 18, 18);
            g.drawOval(x + 12, 105 + (x % 90), 8, 8);
        }

        g.setColor(COLOR_PLAYER);
        g.fillRect(80, 230, 90, 48);
        g.setColor(Color.WHITE);
        g.fillOval(145, 240, 9, 9);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        int x = (getWidth() - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private class TitleInput extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_1:
                    game.startNewGame();
                    break;
                case KeyEvent.VK_2:
                    game.startFromScene2();
                    break;
                case KeyEvent.VK_Q:
                    game.quit();
                    break;
                default:
                    break;
            }
        }
    }
}
