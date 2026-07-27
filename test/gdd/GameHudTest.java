package gdd;

import gdd.ui.GameHud;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class GameHudTest {

    private static final int WIDTH = 716;
    private static final int BAR_X = (WIDTH - 500) / 2;
    private static final int BAR_Y = 103;

    public static void main(String[] args) {
        usesGreenBossHealthBeforePhaseTwo();
        usesDeepRedBossHealthDuringPhaseTwo();
        hidesBossDebugTextNormally();
    }

    private static void usesGreenBossHealthBeforePhaseTwo() {
        BufferedImage image = drawBossHealth(50);

        assertEquals(new Color(48, 176, 91).getRGB(),
                image.getRGB(BAR_X + 20, BAR_Y + 6),
                "phase-one boss health color");
    }

    private static void usesDeepRedBossHealthDuringPhaseTwo() {
        BufferedImage image = drawBossHealth(25);

        assertEquals(new Color(139, 24, 42).getRGB(),
                image.getRGB(BAR_X + 20, BAR_Y + 6),
                "phase-two boss health color");
    }

    private static BufferedImage drawBossHealth(int health) {
        return drawBossHealth(health, "IDLE");
    }

    private static BufferedImage drawBossHealth(
            int health, String attackName) {
        BufferedImage image = new BufferedImage(
                WIDTH, 180, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        GameHud.drawBossHealth(graphics, WIDTH, health, 50, attackName);
        graphics.dispose();
        return image;
    }

    private static void hidesBossDebugTextNormally() {
        int[] idlePixels = ((DataBufferInt) drawBossHealth(
                50, "IDLE").getRaster().getDataBuffer()).getData();
        int[] bitePixels = ((DataBufferInt) drawBossHealth(
                50, "BITE OUT").getRaster().getDataBuffer()).getData();

        if (!Arrays.equals(idlePixels, bitePixels)) {
            throw new AssertionError(
                    "boss attack state should be hidden in normal mode");
        }
    }

    private static void assertEquals(int expected, int actual,
            String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected
                    + ", got " + actual);
        }
    }
}
