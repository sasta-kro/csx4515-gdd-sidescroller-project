package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;

public class Jellyfish extends Enemy {

    private enum State {
        IDLE,
        ATTACKING,
        DYING
    }

    private static final String IDLE_SHEET_PATH = "src/images/enemies/jellyfish/Idle.png";
    private static final String ATTACK_SHEET_PATH = "src/images/enemies/jellyfish/Attack.png";
    private static final String DEATH_SHEET_PATH = "src/images/enemies/jellyfish/Death.png";
    private static final int ATTACK_COOLDOWN_TICKS = secondsToTicks(2);
    private static final double[] HITBOX_SCALE = {0.3, 0.5};

    private static final ImageIcon idleSheet = new ImageIcon(IDLE_SHEET_PATH);
    private static final ImageIcon attackSheet = new ImageIcon(ATTACK_SHEET_PATH);
    private static final ImageIcon deathSheet = new ImageIcon(DEATH_SHEET_PATH);

    private static final List<Rectangle> idleAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48)
    );

    private static final List<Rectangle> attackAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48)
    );

    private static final List<Rectangle> deathAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48),
            new Rectangle(48*4, 0, 48, 48),
            new Rectangle(48*5, 0, 48, 48)
    );

    private final double centerY;
    private double wave;
    private State state = State.IDLE;
    private int attackCooldownTicks = ATTACK_COOLDOWN_TICKS;

    public Jellyfish(Player player, int x, int y) {
        super(player, x, y, 48*2, 48*2, 1, ENEMY_CONTACT_DAMAGE, 100, new Color(130, 210, 235));

        centerY = y;
        wave = Math.random() * Math.PI * 2;
        setHitboxScale(HITBOX_SCALE[0], HITBOX_SCALE[1]);
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

        if (state == State.ATTACKING && isAnimationFinished()) {
            state = State.IDLE;
            attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
            updateAnimationFrames();
        } else if (state == State.IDLE && --attackCooldownTicks <= 0) {
            state = State.ATTACKING;
            updateAnimationFrames();
        }

        moveWithWorld();
        wave += 0.055;
        y = centerY + Math.sin(wave) * 34;
        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    private void updateAnimationFrames() {
        if (state == State.DYING) {
            setImage(deathSheet.getImage());
            setAnimationFrames(deathAnimationClips);
            setAnimationLooping(false);
            return;
        }

        if (state == State.ATTACKING) {
            setImage(attackSheet.getImage());
            setAnimationFrames(attackAnimationClips);
            setHitboxScale(0.7, 0.8);
            setAnimationLooping(false);
            return;
        }

        setImage(idleSheet.getImage());
        setAnimationFrames(idleAnimationClips);
        setHitboxScale(HITBOX_SCALE[0], HITBOX_SCALE[1]);
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
        return false;
    }
}
