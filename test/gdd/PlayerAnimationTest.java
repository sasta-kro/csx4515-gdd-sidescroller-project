package gdd;

import static gdd.Global.*;
import gdd.sprite.Player;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class PlayerAnimationTest {

    public static void main(String[] args) {
        animatesSwimming();
        animatesDeathInActiveColor();
    }

    private static void animatesSwimming() {
        Player player = new Player(new RunState());
        player.setX(0);
        player.setY(0);

        assertEquals(1024, player.getImage().getWidth(null),
                "player asset-sheet width");
        assertEquals(1536, player.getImage().getHeight(null),
                "player asset-sheet height");

        int firstFrameHash = renderHash(player);
        for (int tick = 0; tick < secondsToTicks(0.12); tick++) {
            player.advanceAnimation();
        }
        int secondFrameHash = renderHash(player);

        if (firstFrameHash == secondFrameHash) {
            throw new AssertionError(
                    "player animation did not advance to a different clip");
        }
    }

    private static void animatesDeathInActiveColor() {
        Player base = new Player(new RunState());
        Player speed = new Player(new RunState());
        speed.applySpeedUp();
        Player multi = new Player(new RunState());
        multi.applyMultiShot();
        Player mega = new Player(new RunState());
        mega.applyMegaShot();
        Player split = new Player(new RunState());
        split.applySplitShot();

        Set<Integer> firstFrameHashes = new HashSet<>();
        for (Player player : new Player[]{base, speed, multi, mega, split}) {
            player.setX(0);
            player.setY(0);
            player.startDeathAnimation();
            firstFrameHashes.add(renderHash(player));

            int firstFrameHash = renderHash(player);
            for (int tick = 0; tick < TARGET_FPS
                    && !player.isDeathAnimationFinished(); tick++) {
                player.advanceAnimation();
            }

            if (!player.isDeathAnimationFinished()) {
                throw new AssertionError(
                        "player death animation did not finish");
            }
            if (firstFrameHash == renderHash(player)) {
                throw new AssertionError(
                        "player death animation did not change frames");
            }
        }

        assertEquals(5, firstFrameHashes.size(),
                "death animation active colors");
    }

    private static int renderHash(Player player) {
        BufferedImage rendered = new BufferedImage(
                PLAYER_WIDTH, PLAYER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = rendered.createGraphics();
        player.draw(graphics);
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
            throw new AssertionError(label + ": expected " + expected
                    + ", got " + actual);
        }
    }
}
