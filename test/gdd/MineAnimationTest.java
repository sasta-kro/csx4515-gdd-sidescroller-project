package gdd;

import gdd.sprite.obstacle.Mine;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class MineAnimationTest {

    public static void main(String[] args) {
        Mine mine = new Mine(500, 300);
        int originalWidth = mine.getRenderWidth();
        int originalHeight = mine.getRenderHeight();
        BufferedImage originalImage = (BufferedImage) mine.getImage();
        Set<BufferedImage> seenFrames = new HashSet<>();
        seenFrames.add(originalImage);

        for (int tick = 0; tick < 6; tick++) {
            mine.act();
            seenFrames.add((BufferedImage) mine.getImage());
        }

        assertTrue(mine.getImage() == originalImage,
                "mine holds its tint frame for seven ticks");

        mine.act();
        seenFrames.add((BufferedImage) mine.getImage());
        assertTrue(mine.getImage() != originalImage,
                "mine advances its tint frame on the seventh tick");

        for (int tick = 7; tick < 28; tick++) {
            mine.act();
            seenFrames.add((BufferedImage) mine.getImage());
        }

        BufferedImage tintedImage = (BufferedImage) mine.getImage();
        assertEquals(originalWidth, mine.getRenderWidth(),
                "mine pulse keeps render width");
        assertEquals(originalHeight, mine.getRenderHeight(),
                "mine pulse keeps render height");
        assertTrue(tintedImage != originalImage,
                "mine pulse changes tint frame");
        assertTrue(distanceFromWhite(tintedImage)
                        < distanceFromWhite(originalImage),
                "mine pulse shifts visible pixels toward white");

        for (int tick = 28; tick < 56; tick++) {
            mine.act();
            seenFrames.add((BufferedImage) mine.getImage());
        }

        assertTrue(mine.getImage() == originalImage,
                "mine pulse returns to its original colours");
        assertEquals(5, seenFrames.size(),
                "mine pulse uses exactly five tint frames");
    }

    private static long distanceFromWhite(BufferedImage image) {
        long distance = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                if ((pixel >>> 24) == 0) {
                    continue;
                }
                distance += 255 - ((pixel >>> 16) & 0xff);
                distance += 255 - ((pixel >>> 8) & 0xff);
                distance += 255 - (pixel & 0xff);
            }
        }
        return distance;
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected
                    + ", got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
