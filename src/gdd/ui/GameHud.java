package gdd.ui;

import static gdd.Global.*;
import gdd.RunState;
import gdd.powerup.WeaponType;
import gdd.sprite.BubbleSprite;
import gdd.sprite.Player;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public final class GameHud {

    private static final int HUD_HEIGHT = 78;
    private static final int ICON_SIZE = 44;

    private static final Color PRIMARY_TEXT = new Color(255, 250, 232);
    private static final Color SECONDARY_TEXT = new Color(255, 211, 132);
    private static final Color TEXT_OUTLINE = new Color(0, 12, 20, 235);
    private static final Color EMPTY_PIP = new Color(41, 77, 88);
    private static final Color HEALTH_BUBBLE_COLOR =
            new Color(130, 0, 12);
    private static final Color SCORE_BUBBLE_COLOR =
            new Color(232, 157, 12);
    private static final Color TRACK_COLOR = new Color(24, 62, 73);
    private static final Color BOSS_PHASE_ONE_HEALTH =
            new Color(48, 176, 91);
    private static final Color BOSS_PHASE_ONE_HIGHLIGHT =
            new Color(151, 245, 176, 190);
    private static final Color BOSS_PHASE_TWO_HEALTH =
            new Color(139, 24, 42);
    private static final Color BOSS_PHASE_TWO_HIGHLIGHT =
            new Color(235, 92, 105, 190);

    private static final Image speedIcon =
            new ImageIcon("src/images/powerups/speed.png").getImage();
    private static final Image multiShotIcon =
            new ImageIcon("src/images/powerups/multi-shot.png").getImage();
    private static final Image megaShotIcon =
            new ImageIcon("src/images/powerups/mega-shot.png").getImage();
    private static final Image splitShotIcon =
            new ImageIcon("src/images/powerups/split-shot.png").getImage();
    private static final Image baseBubbleSheet =
            BubbleSprite.tintedSheet(COLOR_PLAYER);
    private static final Image healthBubbleSheet =
            overlayTint(BubbleSprite.tintedSheet(HEALTH_BUBBLE_COLOR),
                    HEALTH_BUBBLE_COLOR, 0.62f);
    private static final Image scoreBubbleSheet =
            overlayTint(BubbleSprite.tintedSheet(SCORE_BUBBLE_COLOR),
                    SCORE_BUBBLE_COLOR, 0.42f);

    private GameHud() {
    }

    public static void draw(Graphics graphics, int width, Player player,
            RunState runState, String stageName, int remainingTicks) {
        Graphics2D g = (Graphics2D) graphics.create();
        configureGraphics(g);

        drawHealth(g, player.getHealth());
        drawScore(g, runState.getScore());
        drawStageAndTime(g, stageName, remainingTicks);
        drawSpeed(g, width - 286, player);
        drawWeapon(g, width - 152, player);

        g.dispose();
    }

    public static void drawBossHealth(Graphics graphics, int width,
            int health, int maxHealth, String attackName) {
        Graphics2D g = (Graphics2D) graphics.create();
        configureGraphics(g);

        int barWidth = Math.min(500, width - 140);
        int barX = (width - barWidth) / 2;
        int labelY = HUD_HEIGHT + 19;
        int barY = HUD_HEIGHT + 25;
        int healthWidth = (int) Math.round(
                barWidth * health / (double) maxHealth);
        boolean phaseTwo = health <= maxHealth / 2;
        Color healthColor = phaseTwo
                ? BOSS_PHASE_TWO_HEALTH
                : BOSS_PHASE_ONE_HEALTH;
        Color highlightColor = phaseTwo
                ? BOSS_PHASE_TWO_HIGHLIGHT
                : BOSS_PHASE_ONE_HIGHLIGHT;

        g.setColor(new Color(1, 18, 29, 205));
        g.fillRoundRect(barX - 9, labelY - 16,
                barWidth + 18, 39, 8, 8);

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(PRIMARY_TEXT);
        g.drawString("ANGLERFISH", barX, labelY);

        String status = attackName + "   " + health + "/" + maxHealth;
        FontMetrics metrics = g.getFontMetrics();
        g.setColor(SECONDARY_TEXT);
        g.drawString(status, barX + barWidth - metrics.stringWidth(status),
                labelY);

        g.setColor(new Color(47, 42, 59));
        g.fillRoundRect(barX, barY, barWidth, 10, 6, 6);
        g.setColor(healthColor);
        g.fillRoundRect(barX, barY, healthWidth, 10, 6, 6);
        if (healthWidth > 6) {
            g.setColor(highlightColor);
            g.drawLine(barX + 3, barY + 2,
                    barX + healthWidth - 3, barY + 2);
        }

        g.dispose();
    }

    private static void drawHealth(Graphics2D g, int health) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        drawText(g, "HP", 14, 18, SECONDARY_TEXT);

        for (int index = 0; index < PLAYER_MAX_HEALTH; index++) {
            int x = 15 + index * 27;
            boolean filled = index < health;

            drawBubbleFrame(g, healthBubbleSheet,
                    x, 29, 24, filled ? 1f : 0.20f);
        }
    }

    private static void drawScore(Graphics2D g, int score) {
        int iconX = 176;
        int iconY = 29;

        drawBubbleFrame(g, scoreBubbleSheet,
                iconX - 2, iconY - 2, 28, 1f);

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        drawText(g, "SCORE", 208, 19, SECONDARY_TEXT);

        g.setFont(new Font("Monospaced", Font.BOLD, 23));
        drawText(g, String.format("%06d", score),
                207, 51, PRIMARY_TEXT);
    }

    private static void drawStageAndTime(Graphics2D g, String stageName,
            int remainingTicks) {
        int centerX = 370;
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        drawCentered(g, stageName, centerX, 20, SECONDARY_TEXT);

        String time = remainingTicks < 0
                ? "BOSS"
                : formatTime(remainingTicks);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        drawCentered(g, time, centerX, 50, PRIMARY_TEXT);
    }

    private static void drawSpeed(Graphics2D g, int x, Player player) {
        int level = player.getSpeedLevel();
        int ticks = player.getSpeedPowerupTicks();

        drawPowerIcon(g, speedIcon, x, 18);
        drawPowerLabel(g, "SPEED", x + 48, level > 0);
        drawStackPips(g, x + 49, 34, 2, level, COLOR_SPEED);
        drawDurationBar(g, x + 49, 55, 72,
                ticks, SPEED_POWERUP_TICKS, COLOR_SPEED);
        drawSeconds(g, ticks, x + 121, 47);
    }

    private static void drawWeapon(Graphics2D g, int x, Player player) {
        WeaponType type = player.getWeaponType();
        int ticks = player.getWeaponPowerupTicks();
        int stackLevel = type == WeaponType.MULTI_SHOT
                ? player.getMultiShotLevel()
                : type == WeaponType.BASE ? 0 : 1;
        int maxStacks = type == WeaponType.MULTI_SHOT ? 4 : 1;
        int maxTicks = type == WeaponType.MULTI_SHOT
                ? MULTI_SHOT_TICKS
                : WEAPON_POWERUP_TICKS;
        Color color = weaponColor(type);

        if (type == WeaponType.BASE) {
            drawBaseBubbleIcon(g, x, 18);
        } else {
            drawPowerIcon(g, weaponIcon(type), x, 18);
        }

        drawPowerLabel(g, weaponLabel(type), x + 48,
                type != WeaponType.BASE);
        drawStackPips(g, x + 49, 34, maxStacks, stackLevel, color);
        drawDurationBar(g, x + 49, 55, 91, ticks, maxTicks, color);
        drawSeconds(g, ticks, x + 140, 47);
    }

    private static void drawPowerIcon(Graphics2D g, Image image,
            int x, int y) {
        g.drawImage(image, x, y, ICON_SIZE, ICON_SIZE, null);
    }

    private static void drawBaseBubbleIcon(Graphics2D g, int x, int y) {
        drawBubbleFrame(g, baseBubbleSheet,
                x + 3, y + 3, ICON_SIZE - 6, 1f);
    }

    private static void drawBubbleFrame(Graphics2D g, Image sheet,
            int x, int y, int size, float opacity) {
        Rectangle clip = BubbleSprite.flightClips().get(0);
        Graphics2D bubbleGraphics = (Graphics2D) g.create();
        bubbleGraphics.setComposite(
                AlphaComposite.SrcOver.derive(opacity));
        bubbleGraphics.drawImage(sheet,
                x, y, x + size, y + size,
                clip.x, clip.y,
                clip.x + clip.width, clip.y + clip.height, null);
        bubbleGraphics.dispose();
    }

    private static BufferedImage overlayTint(Image source,
            Color color, float strength) {
        BufferedImage tinted = new BufferedImage(
                source.getWidth(null), source.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = tinted.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.setComposite(AlphaComposite.SrcAtop.derive(strength));
        graphics.setColor(color);
        graphics.fillRect(0, 0, tinted.getWidth(), tinted.getHeight());
        graphics.dispose();
        return tinted;
    }

    private static void drawPowerLabel(Graphics2D g, String label,
            int x, boolean active) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        drawText(g, label, x, 25,
                active ? PRIMARY_TEXT : SECONDARY_TEXT);
    }

    private static void drawStackPips(Graphics2D g, int x, int y,
            int maxStacks, int activeStacks, Color color) {
        for (int index = 0; index < maxStacks; index++) {
            g.setColor(index < activeStacks ? color : EMPTY_PIP);
            g.fillOval(x + index * 11, y, 7, 7);
        }
    }

    private static void drawDurationBar(Graphics2D g, int x, int y,
            int width, int ticks, int maxTicks, Color color) {
        double progress = maxTicks == 0
                ? 0
                : Math.max(0, Math.min(1, ticks / (double) maxTicks));
        int progressWidth = (int) Math.round(width * progress);

        g.setColor(TRACK_COLOR);
        g.fillRoundRect(x, y, width, 6, 6, 6);
        if (progressWidth > 0) {
            g.setColor(color);
            g.fillRoundRect(x, y, progressWidth, 6, 6, 6);
        }
    }

    private static void drawSeconds(Graphics2D g, int ticks,
            int rightX, int baselineY) {
        String text = ticks > 0
                ? String.format("%.1f", ticks / (double) TARGET_FPS)
                : "--";
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        int width = g.getFontMetrics().stringWidth(text);
        drawText(g, text, rightX - width, baselineY, SECONDARY_TEXT);
    }

    private static Image weaponIcon(WeaponType type) {
        return switch (type) {
            case MULTI_SHOT -> multiShotIcon;
            case MEGA_SHOT -> megaShotIcon;
            case SPLIT_SHOT -> splitShotIcon;
            default -> baseBubbleSheet;
        };
    }

    private static String weaponLabel(WeaponType type) {
        return switch (type) {
            case MULTI_SHOT -> "MULTI";
            case MEGA_SHOT -> "MEGA";
            case SPLIT_SHOT -> "SPLIT";
            default -> "BUBBLE";
        };
    }

    private static Color weaponColor(WeaponType type) {
        return switch (type) {
            case MULTI_SHOT -> COLOR_MULTI;
            case MEGA_SHOT -> COLOR_MEGA;
            case SPLIT_SHOT -> COLOR_SPLIT;
            default -> new Color(122, 214, 238);
        };
    }

    private static String formatTime(int ticks) {
        int totalSeconds = ticks / TARGET_FPS;
        return String.format("%d:%02d",
                totalSeconds / 60, totalSeconds % 60);
    }

    private static void drawCentered(Graphics2D g, String text,
            int centerX, int baselineY, Color color) {
        int x = centerX - g.getFontMetrics().stringWidth(text) / 2;
        drawText(g, text, x, baselineY, color);
    }

    private static void drawText(Graphics2D g, String text,
            int x, int baselineY, Color color) {
        g.setColor(TEXT_OUTLINE);
        g.drawString(text, x - 1, baselineY);
        g.drawString(text, x + 1, baselineY);
        g.drawString(text, x, baselineY - 1);
        g.drawString(text, x, baselineY + 1);
        g.drawString(text, x + 2, baselineY + 2);
        g.setColor(color);
        g.drawString(text, x, baselineY);
    }

    private static void configureGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }
}
