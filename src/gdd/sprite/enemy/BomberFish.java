package gdd.sprite.enemy;

import gdd.sprite.Player;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

public class BomberFish extends Enemy {

    private enum State {
        SWIMMING,
        HURT,
        DETONATING
    }

    private static final String WALK_SHEET_PATH =
            "src/images/boss/bomber-fish/Walk.png";
    private static final String HURT_SHEET_PATH =
            "src/images/boss/bomber-fish/Hurt.png";
    private static final String ATTACK_SHEET_PATH =
            "src/images/boss/bomber-fish/Attack.png";

    private static final ImageIcon walkSheet = new ImageIcon(WALK_SHEET_PATH);
    private static final ImageIcon hurtSheet = new ImageIcon(HURT_SHEET_PATH);
    private static final ImageIcon attackSheet = new ImageIcon(ATTACK_SHEET_PATH);

    private static final List<Rectangle> walkAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48 * 2, 0, 48, 48),
            new Rectangle(48 * 3, 0, 48, 48)
    );
    private static final List<Rectangle> hurtAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48)
    );
    private static final List<Rectangle> attackAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48 * 2, 0, 48, 48),
            new Rectangle(48 * 3, 0, 48, 48),
            new Rectangle(48 * 4, 0, 48, 48),
            new Rectangle(48 * 5, 0, 48, 48)
    );

    private int explosionTicks;
    private State state = State.SWIMMING;

    public BomberFish(Player player, int x, int y, Random random) {
        super(player, x, y, 48 * 2, 48 * 2, 1,
                ENEMY_CONTACT_DAMAGE, 0, new Color(235, 105, 75));
        explosionTicks = secondsToTicks(2 + random.nextDouble() * 2);
        setHitboxScale(0.35, 0.25);
        setFlippedHorizontally(true);
        updateAnimationFrames();
    }

    @Override
    public void act() {
        if (state == State.DETONATING) {
            return;
        }

        if (state == State.HURT && isAnimationFinished()) {
            state = State.SWIMMING;
            updateAnimationFrames();
        }

        double targetX = player.getX() - x;
        double targetY = player.getY() - y;
        double length = Math.max(1.0, Math.hypot(targetX, targetY));
        double speed = 2.6;
        x += targetX / length * speed;
        y += targetY / length * speed;

        if (--explosionTicks <= 0 || collidesWith(player)) {
            state = State.DETONATING;
            updateAnimationFrames();
        }
    }

    public boolean shouldExplode() {
        return state == State.DETONATING && isAnimationFinished();
    }

    private void updateAnimationFrames() {
        switch (state) {
            case HURT -> {
                setImage(hurtSheet.getImage());
                setAnimationFrames(hurtAnimationClips);
                setAnimationLooping(false);
            }
            case DETONATING -> {
                setImage(attackSheet.getImage());
                setAnimationFrames(attackAnimationClips);
                setAnimationLooping(false);
            }
            case SWIMMING -> {
                setImage(walkSheet.getImage());
                setAnimationFrames(walkAnimationClips);
            }
        }
    }

    @Override
    public boolean damage(int amount) {
        if (!isVisible() || state == State.DETONATING || amount <= 0) {
            return false;
        }

        health -= amount;
        if (health <= 0) {
            health = 0;
            die();
            return true;
        }

        state = State.HURT;
        updateAnimationFrames();
        return false;
    }
}
