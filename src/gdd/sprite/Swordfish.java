package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;

public class Swordfish extends Enemy {

    private static final String WALK_SHEET_PATH = "src/images/enemies/swordfish/Walk.png";
    private static final String HURT_SHEET_PATH = "src/images/enemies/swordfish/Hurt.png";
    private static final String ATTACK_SHEET_PATH = "src/images/enemies/swordfish/Attack.png";
    private static final String DEATH_SHEET_PATH = "src/images/enemies/swordfish/Death.png";

    private static final ImageIcon walkSheet = new ImageIcon(WALK_SHEET_PATH);
    private static final ImageIcon hurtSheet = new ImageIcon(HURT_SHEET_PATH);
    private static final ImageIcon attackSheet = new ImageIcon(ATTACK_SHEET_PATH);
    private static final ImageIcon deathSheet = new ImageIcon(DEATH_SHEET_PATH);

    private static final List<Rectangle> walkAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48)
    );
    private static final List<Rectangle> hurtAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48)
    );
    private static final List<Rectangle> attackAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48),
            new Rectangle(48*4, 0, 48, 48),
            new Rectangle(48*5, 0, 48, 48)
    );
    private static final List<Rectangle> deathAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48),
            new Rectangle(48*4, 0, 48, 48),
            new Rectangle(48*5, 0, 48, 48)
    );

    private enum State {
        CHARGING,
        DASHING
    }

    private State state = State.CHARGING;
    private int chargeTicks = secondsToTicks(0.75);
    private double dashX;
    private double dashY;
    private boolean hurt;

    public Swordfish(Player player, int x, int y) {
        super(player, x, y, 48*3, 48*3, 2,
                ENEMY_CONTACT_DAMAGE, 200, new Color(80, 165, 220));
        setHitboxScale(0.85, 0.3);
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

        if (state == State.CHARGING) {
            moveWithWorld();

            if (--chargeTicks <= 0) {
                lockDashDirection();
                state = State.DASHING;
                if (!hurt) {
                    updateAnimationFrames();
                }
            }
        } else {
            x += dashX;
            y += dashY;
        }

        if (state == State.DASHING
                && (getX() + getRenderWidth() < 0
                || getY() + getRenderHeight() < 0
                || getY() > BOARD_HEIGHT)) {
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

        if (state == State.DASHING) {
            setImage(attackSheet.getImage());
            setAnimationFrames(attackAnimationClips);
            return;
        }

        setImage(walkSheet.getImage());
        setAnimationFrames(walkAnimationClips);
    }

    private void lockDashDirection() {
        double targetX = player.getX() - x;
        double targetY = player.getY() - y;
        double length = Math.max(1.0, Math.hypot(targetX, targetY));
        double speed = 7.0;
        dashX = targetX / length * speed;
        dashY = targetY / length * speed;
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
