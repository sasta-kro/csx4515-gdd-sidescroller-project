package gdd.ui;

import static gdd.Global.scaledFontSize;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class PauseMenu {

    private static final Color SHADOW = new Color(1, 13, 24);
    private static final Color TITLE = new Color(126, 245, 235);
    private static final Color PRIMARY = new Color(255, 236, 137);
    private static final Color TEXT = new Color(235, 245, 241);
    private static final Color DANGER = new Color(255, 177, 147);
    private static final Color SHORTCUT = new Color(145, 190, 194);

    private PauseMenu() {
    }

    public static void draw(Graphics graphics, int width, int height) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(new Color(0, 5, 12, 170));
        g.fillRect(0, 0, width, height);

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(48)));
        drawCenteredShadowed(g, "PAUSED", width, 225, TITLE);

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(12)));
        drawCenteredShadowed(g, "OCEAN INVADERS", width, 258,
                new Color(159, 205, 205));

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(28)));
        drawMenuOption(g, width, "ESC", "RESUME", 325,
                PRIMARY, new Color(177, 211, 209));

        int resumeWidth = g.getFontMetrics().stringWidth("RESUME");
        g.setColor(new Color(91, 222, 210));
        g.fillRect((width - resumeWidth) / 2, 337, resumeWidth, 2);

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(19)));
        drawMenuOption(g, width, "R", "RESTART GAME", 390,
                TEXT, SHORTCUT);
        drawMenuOption(g, width, "M", "MAIN MENU", 433,
                TEXT, SHORTCUT);
        drawMenuOption(g, width, "Q", "QUIT GAME", 476,
                DANGER, SHORTCUT);

        g.dispose();
    }

    private static void drawMenuOption(Graphics2D g, int width,
            String shortcut, String label, int y, Color labelColor,
            Color shortcutColor) {
        Font labelFont = g.getFont();
        int labelWidth = g.getFontMetrics(labelFont).stringWidth(label);
        int labelX = (width - labelWidth) / 2;

        g.setColor(SHADOW);
        g.drawString(label, labelX + 3, y + 3);
        g.setColor(labelColor);
        g.drawString(label, labelX, y);

        Font shortcutFont = labelFont.deriveFont(
                Math.max(10f, labelFont.getSize2D() * 0.62f));
        g.setFont(shortcutFont);
        int shortcutWidth = g.getFontMetrics().stringWidth(shortcut);
        int shortcutX = labelX - shortcutWidth - scaledFontSize(14);

        g.setColor(SHADOW);
        g.drawString(shortcut, shortcutX + 2, y + 2);
        g.setColor(shortcutColor);
        g.drawString(shortcut, shortcutX, y);
        g.setFont(labelFont);
    }

    private static void drawCenteredShadowed(Graphics2D g, String text,
            int width, int y, Color color) {
        int x = (width - g.getFontMetrics().stringWidth(text)) / 2;
        g.setColor(SHADOW);
        g.drawString(text, x + 3, y + 3);
        g.setColor(color);
        g.drawString(text, x, y);
    }
}
