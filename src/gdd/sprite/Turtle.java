package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;

public class Turtle extends Enemy {

    private static final double TRACK_SPEED = 1.25;
    private static final String WALK_SHEET_PATH = "src/images/enemies/turtle/Walk.png";
    private static final String HURT_SHEET_PATH = "src/images/enemies/turtle/Hurt.png";
    private static final String DEATH_SHEET_PATH = "src/images/enemies/turtle/Death.png";

    private static final ImageIcon walkSheet = new ImageIcon(WALK_SHEET_PATH);
    private static final ImageIcon hurtSheet = new ImageIcon(HURT_SHEET_PATH);
    private static final ImageIcon deathSheet = new ImageIcon(DEATH_SHEET_PATH);

    private static final List<Rectangle> walkAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48),
            new Rectangle(48*4, 0, 48, 48),
            new Rectangle(48*5, 0, 48, 48)
    );
    private static final List<Rectangle> hurtAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48)
    );
    private static final List<Rectangle> deathAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48),
            new Rectangle(48*4, 0, 48, 48),
            new Rectangle(48*5, 0, 48, 48)
    );

    private boolean hurt;

    public Turtle(Player player, int x, int y) {
        super(player, x, y, 48*3, 48*3, 2, ENEMY_CONTACT_DAMAGE, 250, new Color(80, 175, 105));
        setHitboxScale(0.6, 0.3);
        setFlippedHorizontally(true);
        updateAnimationFrames();
    }

    @Override
    public void act() {
        if (isDying()) {
            if (isAnimationFinished()) {
                die();
            }
            return;
        }

        if (hurt && isAnimationFinished()) {
            hurt = false;
            updateAnimationFrames();
        }

        double targetX = player.getX() - x;
        double targetY = player.getY() - y;
        double length = Math.max(1.0, Math.hypot(targetX, targetY));
        setFlippedHorizontally(targetX < (double) this.getRenderWidth() /2);

        x += targetX / length * TRACK_SPEED;
        y += targetY / length * TRACK_SPEED;
        moveWithWorld();

        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    private void updateAnimationFrames() {
        if (isDying()) {
            setImage(deathSheet.getImage());
            setAnimationFrames(deathAnimationClips);
            setAnimationLooping(false);
            return;
        }

        if (hurt) {
            setImage(hurtSheet.getImage());
            setAnimationFrames(hurtAnimationClips);
            setAnimationLooping(false);
            return;
        }

        setImage(walkSheet.getImage());
        setAnimationFrames(walkAnimationClips);
    }

    @Override
    public boolean damage(int amount) {
        if (!isVisible() || isDying() || amount <= 0) {
            return false;
        }

        health -= amount;
        if (health <= 0) {
            health = 0;
            setDying(true);
            updateAnimationFrames();
            return true;
        }

        hurt = true;
        updateAnimationFrames();
        return false;
    }
}
