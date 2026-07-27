package gdd;

import gdd.sprite.obstacle.Explosion;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ExplosionAnimationTest {

    public static void main(String[] args) {
        Explosion explosion = new Explosion(100, 100, 40);

        assertEquals(660, explosion.getImage().getWidth(null), "explosion sheet width");
        assertEquals(82, explosion.getImage().getHeight(null), "explosion sheet height");

        int firstFrameHash = renderHash(explosion);
        for (int tick = 0; tick < 4; tick++) {
            explosion.advanceAnimation();
        }
        assertEquals(firstFrameHash, renderHash(explosion), "explosion holds each frame for five ticks");

        explosion.advanceAnimation();
        assertNotEquals(firstFrameHash, renderHash(explosion), "explosion advances to the next frame");

        for (int tick = 5; tick < 55; tick++) {
            explosion.act();
            explosion.advanceAnimation();
        }

        assertTrue(explosion.isVisible(), "explosion keeps its final frame visible");
        explosion.act();
        assertTrue(!explosion.isVisible(), "explosion disappears after its final frame");
    }

    private static int renderHash(Explosion explosion) {
        BufferedImage rendered = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = rendered.createGraphics();
        explosion.draw(graphics);
        graphics.dispose();

        int hash = 1;
        for (int y = 0; y < rendered.getHeight(); y++) {
            for (int x = 0; x < rendered.getWidth(); x++) {
                hash = 31 * hash + rendered.getRGB(x, y);
            }
        }
        return hash;
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertNotEquals(int unexpected, int actual, String label) {
        if (unexpected == actual) {
            throw new AssertionError(label + ": both values were " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
