package gdd;

import static gdd.Global.*;
import gdd.powerup.Heal;
import gdd.powerup.MegaShot;
import gdd.powerup.MultiShot;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.SplitShot;
import gdd.sprite.Bubble;
import gdd.sprite.obstacle.Coral;
import gdd.sprite.enemy.BossBubble;
import gdd.sprite.enemy.EnemyProjectile;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class SpriteAssetTest {

    public static void main(String[] args) {
        loadsOctopusRockTexture();
        playsCoralDeathAnimation();
        loadsPowerUpTextures();
        loadsAndPopsBubbleTexture();
        tintsAndScalesBubbleTexture();
    }

    private static void loadsOctopusRockTexture() {
        EnemyProjectile rock = new EnemyProjectile(400, 300);

        assertEquals(16, rock.getImage().getWidth(null),
                "rock texture width");
        assertEquals(16, rock.getImage().getHeight(null),
                "rock texture height");
    }

    private static void playsCoralDeathAnimation() {
        Coral coral = new Coral(400, 500);

        assertEquals(48, coral.getImage().getWidth(null),
                "coral idle texture width");
        assertEquals(56, coral.getImage().getHeight(null),
                "coral idle texture height");

        coral.damage();
        assertTrue(coral.isDying(),
                "coral enters dying state");
        assertTrue(coral.isVisible(),
                "coral remains visible during death animation");
        assertEquals(312, coral.getImage().getWidth(null),
                "coral death sheet width");

        int startX = coral.getX();
        coral.act();
        coral.advanceAnimation();
        assertEquals(startX - WORLD_SCROLL_SPEED, coral.getX(),
                "dying coral follows world scroll");

        for (int tick = 0; tick < 50; tick++) {
            coral.act();
            coral.advanceAnimation();
        }

        assertTrue(!coral.isVisible(),
                "coral disappears after death animation");
    }

    private static void loadsPowerUpTextures() {
        PowerUp[] powerUps = {
            new Heal(0, 0),
            new SpeedUp(0, 0),
            new MultiShot(0, 0),
            new MegaShot(0, 0),
            new SplitShot(0, 0)
        };

        for (PowerUp powerUp : powerUps) {
            assertEquals(48, powerUp.getImage().getWidth(null),
                    powerUp.getDisplayName() + " texture width");
            assertEquals(48, powerUp.getImage().getHeight(null),
                    powerUp.getDisplayName() + " texture height");
        }
    }

    private static void loadsAndPopsBubbleTexture() {
        Bubble bubble = new Bubble(100, 100,
                BUBBLE_WIDTH, BUBBLE_HEIGHT, BUBBLE_DAMAGE,
                BUBBLE_SPEED, Color.CYAN, true);

        assertEquals(48, bubble.getImage().getWidth(null),
                "bubble sheet width");
        assertEquals(16, bubble.getImage().getHeight(null),
                "bubble sheet height");

        bubble.die();
        assertTrue(bubble.isDying(),
                "bubble enters popping state");
        assertTrue(bubble.isVisible(),
                "bubble remains visible while popping");

        for (int tick = 0; tick < 10; tick++) {
            bubble.act();
            bubble.advanceAnimation();
        }

        assertTrue(!bubble.isVisible(),
                "bubble disappears after pop animation");
    }

    private static void tintsAndScalesBubbleTexture() {
        Bubble cyanBubble = new Bubble(0, 0,
                BUBBLE_WIDTH, BUBBLE_HEIGHT, BUBBLE_DAMAGE,
                BUBBLE_SPEED, Color.CYAN, true);
        Bubble brownBubble = new Bubble(0, 0,
                BUBBLE_WIDTH, BUBBLE_HEIGHT, BUBBLE_DAMAGE,
                BUBBLE_SPEED, new Color(154, 133, 113), true);
        BufferedImage cyanSheet = (BufferedImage) cyanBubble.getImage();
        BufferedImage brownSheet = (BufferedImage) brownBubble.getImage();

        assertTrue(cyanSheet.getRGB(8, 8) != brownSheet.getRGB(8, 8),
                "bubble sheet uses the supplied tint");

        BossBubble bossBubble = new BossBubble(
                0, 0, 1, new Color(235, 75, 115));
        assertEquals(36, bossBubble.getRenderWidth(),
                "boss bubble render width");
        assertEquals(36, bossBubble.getRenderHeight(),
                "boss bubble render height");
    }

    private static void assertEquals(int expected, int actual,
            String label) {
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
