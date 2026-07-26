package gdd.sprite.obstacle;

import gdd.sprite.Sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;

public class Coral extends Sprite {

    private static final String IDLE_IMAGE_PATH = "src/images/obstacles/coral.png";
    private static final String DEATH_SHEET_PATH = "src/images/obstacles/coral_death.png";
    private static final ImageIcon idleImage = new ImageIcon(IDLE_IMAGE_PATH);
    private static final ImageIcon deathSheet = new ImageIcon(DEATH_SHEET_PATH);
    private static final List<Rectangle> deathAnimationClips = List.of(
            new Rectangle(0, 0, 52, 53),
            new Rectangle(52, 0, 52, 53),
            new Rectangle(52 * 2, 0, 52, 53),
            new Rectangle(52 * 3, 0, 52, 53),
            new Rectangle(52 * 4, 0, 52, 53),
            new Rectangle(52 * 5, 0, 52, 53)
    );

    private int health = 1;
    public int contactDamage = CORAL_CONTACT_DAMAGE;

    public Coral(int x, int y) {
        super(x, y, CORAL_WIDTH, CORAL_HEIGHT, new Color(235, 95, 115));
        setImage(idleImage.getImage());
    }

    @Override
    public void act() {
        x -= WORLD_SCROLL_SPEED;

        if (isDying() && isAnimationFinished()) {
            die();
            return;
        }

        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    public void damage() {
        if (isDying()) {
            return;
        }

        health--;
        setDying(true);
        setImage(deathSheet.getImage());
        setAnimationInterval(7);
        setAnimationFrames(deathAnimationClips);
        setAnimationLooping(false);
    }
}
