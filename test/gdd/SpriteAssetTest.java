package gdd;

import static gdd.Global.*;
import gdd.powerup.Heal;
import gdd.powerup.MegaShot;
import gdd.powerup.MultiShot;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.SplitShot;
import gdd.sprite.obstacle.Coral;
import gdd.sprite.enemy.EnemyProjectile;

public class SpriteAssetTest {

    public static void main(String[] args) {
        loadsOctopusRockTexture();
        playsCoralDeathAnimation();
        loadsPowerUpTextures();
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
