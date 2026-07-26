package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

public class Octopus extends Enemy {

    private static final String WALK_SHEET_PATH = "src/images/enemies/octopus/Walk.png";
    private static final String HURT_SHEET_PATH = "src/images/enemies/octopus/Hurt.png";
    private static final String ATTACK_SHEET_PATH = "src/images/enemies/octopus/Attack.png";
    private static final String DEATH_SHEET_PATH = "src/images/enemies/octopus/Death.png";

    private static final ImageIcon walkSheet = new ImageIcon(WALK_SHEET_PATH);
    private static final ImageIcon hurtSheet = new ImageIcon(HURT_SHEET_PATH);
    private static final ImageIcon attackSheet = new ImageIcon(ATTACK_SHEET_PATH);
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
    private boolean attacking;
    private boolean hurt;

    public Octopus(Player player, int x, int y) {
        super(player, x, y, 48*2, 48*2, 2,
                ENEMY_CONTACT_DAMAGE, 200, new Color(190, 95, 190));
        centerY = y;
        rockCooldown = nextRockCooldown();
        setHitboxScale(0.55, 0.65);
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
        } else if (attacking && isAnimationFinished()) {
            attacking = false;
            updateAnimationFrames();
        }

        moveWithWorld();
        wave += 0.045;
        y = centerY + Math.sin(wave) * 44;

        if (--rockCooldown <= 0) {
            rockReady = true;
            rockCooldown = nextRockCooldown();
        }

        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    public EnemyProjectile shootRockIfReady() {
        if (!rockReady || !isVisible() || isDying() || hurt) {
            return null;
        }

        rockReady = false;
        attacking = true;
        updateAnimationFrames();
        return new EnemyProjectile(getX() - 14, getY() + getRenderHeight() / 2);
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

        if (attacking) {
            setImage(attackSheet.getImage());
            setAnimationFrames(attackAnimationClips);
            setAnimationLooping(false);
            return;
        }

        setImage(walkSheet.getImage());
        setAnimationFrames(walkAnimationClips);
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
            setDying(true);
            updateAnimationFrames();
            return true;
        }

        attacking = false;
        hurt = true;
        updateAnimationFrames();
        return false;
    }
}
