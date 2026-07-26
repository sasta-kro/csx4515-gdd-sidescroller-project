package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;

public class Snake extends Enemy {

    private enum State {
        IDLE,
        ATTACKING,
        HURT,
        DYING
    }

    private static final String IDLE_SHEET_PATH = "src/images/enemies/snake/Walk.png";
    private static final String HURT_SHEET_PATH = "src/images/enemies/snake/Hurt.png";
    private static final String ATTACK_SHEET_PATH = "src/images/enemies/snake/Attack.png";
    private static final String DEATH_SHEET_PATH = "src/images/enemies/snake/Death.png";
    private static final int ATTACK_COOLDOWN_TICKS = secondsToTicks(2);

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

    private final int verticalDirection;
    private State state = State.IDLE;
    private int attackCooldownTicks = ATTACK_COOLDOWN_TICKS;

    public Snake(Player player, int x, boolean fromTop) {
        super(player, x, fromTop ? -70 : BOARD_HEIGHT,
                48*2, 48*2, 2, ENEMY_CONTACT_DAMAGE, 150,
                new Color(100, 205, 125));
        verticalDirection = fromTop ? 1 : -1;
        faceVerticalDirection();
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
        } else if (state == State.ATTACKING && isAnimationFinished()) {
            state = State.IDLE;
            attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
            updateAnimationFrames();
        } else if (state == State.IDLE && --attackCooldownTicks <= 0) {
            state = State.ATTACKING;
            updateAnimationFrames();
        }

        moveWithWorld();
        y += verticalDirection * 3;
        if (isOutsideViewport()) {
            die();
        }
    }

    private void faceVerticalDirection() {
        setRotationDegrees(verticalDirection > 0 ? -90 : 90);
    }

    private void updateAnimationFrames() {
        switch (state) {
            case DYING -> {
                setImage(deathSheet.getImage());
                setAnimationFrames(deathAnimationClips);
                setAnimationLooping(false);
                setHitboxScale(0.35, 0.85);
            }
            case HURT -> {
                setImage(hurtSheet.getImage());
                setAnimationFrames(hurtAnimationClips);
                setAnimationLooping(false);
                setHitboxScale(0.35, 0.85);
            }
            case ATTACKING -> {
                setImage(attackSheet.getImage());
                setAnimationFrames(attackAnimationClips);
                setAnimationLooping(false);
                setHitboxScale(0.7, 0.95);
            }
            case IDLE -> {
                setImage(idleSheet.getImage());
                setAnimationFrames(idleAnimationClips);
                setHitboxScale(0.35, 0.85);
            }
        }
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

        state = State.HURT;
        attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
        updateAnimationFrames();
        return false;
    }
}
