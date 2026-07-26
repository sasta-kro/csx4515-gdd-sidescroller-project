package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

public class Octopus extends Enemy {

    private enum State {
        IDLE,
        ATTACKING,
        HURT,
        DYING
    }

    private static final String IDLE_SHEET_PATH = "src/images/enemies/octopus/Idle.png";
    private static final String HURT_SHEET_PATH = "src/images/enemies/octopus/Hurt.png";
    private static final String ATTACK_SHEET_PATH = "src/images/enemies/octopus/Attack.png";
    private static final String DEATH_SHEET_PATH = "src/images/enemies/octopus/Death.png";

    private static final ImageIcon idleSheet = new ImageIcon(IDLE_SHEET_PATH);
    private static final ImageIcon hurtSheet = new ImageIcon(HURT_SHEET_PATH);
    private static final ImageIcon attackSheet = new ImageIcon(ATTACK_SHEET_PATH);
    private static final ImageIcon deathSheet = new ImageIcon(DEATH_SHEET_PATH);

    private static final List<Rectangle> idleAnimationClips = List.of(
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

    private final Random random = new Random();
    private final double centerY;
    private double wave;
    private int rockCooldown;
    private boolean rockReady;
    private boolean rockThrown;
    private State state = State.IDLE;

    public Octopus(Player player, int x, int y) {
        super(player, x, y, 48*2, 48*2, 2,
                ENEMY_CONTACT_DAMAGE, 200, new Color(190, 95, 190));
        centerY = y;
        rockCooldown = nextRockCooldown();
        setHitboxScale(0.55, 0.65);
        setFlippedHorizontally(true);
        updateAnimationFrames();
    }

    @Override
    public void act() {
        if (state == State.DYING) {
            if (isAnimationFinished()) {
                die();
            }
            return;
        }

        if (state == State.HURT && isAnimationFinished()) {
            state = State.IDLE;
            updateAnimationFrames();
        }

        moveWithWorld();
        wave += 0.045;
        y = centerY + Math.sin(wave) * 44;

        if (!rockReady && state == State.IDLE && --rockCooldown <= 0) {
            rockReady = true;
        }

        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    public EnemyProjectile shootRockIfReady() {
        if (!isVisible() || state == State.HURT || state == State.DYING) {
            return null;
        }

        if (state == State.IDLE) {
            if (!rockReady) {
                return null;
            }

            rockReady = false;
            rockThrown = false;
            state = State.ATTACKING;
            updateAnimationFrames();
            return null;
        }

        if (!rockThrown
                && getCurrentAnimationFrame() >= attackAnimationClips.size() / 2) {
            rockThrown = true;
            rockCooldown = nextRockCooldown();
            return createRock();
        }

        if (isAnimationFinished()) {
            state = State.IDLE;
            updateAnimationFrames();
        }

        return null;
    }

    private EnemyProjectile createRock() {
        EnemyProjectile rock = new EnemyProjectile(getX() - 16, getY());
        rock.setY(getY()
                + (getRenderHeight() - rock.getRenderHeight()) / 2);
        return rock;
    }

    private void updateAnimationFrames() {
        switch (state) {
            case DYING -> {
                setImage(deathSheet.getImage());
                setAnimationInterval(7);
                setAnimationFrames(deathAnimationClips);
                setAnimationLooping(false);
            }
            case HURT -> {
                setImage(hurtSheet.getImage());
                setAnimationInterval(7);
                setAnimationFrames(hurtAnimationClips);
                setAnimationLooping(false);
            }
            case ATTACKING -> {
                setImage(attackSheet.getImage());
                setAnimationInterval(7);
                setAnimationFrames(attackAnimationClips);
                setAnimationLooping(false);
            }
            case IDLE -> {
                setImage(idleSheet.getImage());
                setAnimationInterval(14);
                setAnimationFrames(idleAnimationClips);
            }
        }
    }

    private int nextRockCooldown() {
        return secondsToTicks(2 + random.nextDouble() * 2);
    }

    @Override
    public boolean damage(int amount) {
        if (!isVisible() || isDying() || amount <= 0) {
            return false;
        }

        health -= amount;
        if (health <= 0) {
            health = 0;
            state = State.DYING;
            setDying(true);
            updateAnimationFrames();
            return true;
        }

        if (state == State.ATTACKING && !rockThrown) {
            rockReady = true;
        }
        state = State.HURT;
        updateAnimationFrames();
        return false;
    }
}
