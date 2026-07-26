package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

public class Anglerfish extends Enemy {

    private static final int HITBOX_X_OFFSET = 6 * BOSS_SCALE;
    private static final int HITBOX_Y_OFFSET = BOSS_SCALE;

    private enum AttackState {
        IDLE,
        LASER_CHARGE,
        LASER,
        BITE_WARNING,
        BITE_OUT,
        BITE_RETURN,
        SUMMON,
        HURT,
        DYING
    }

    private static final String IDLE_SHEET_PATH = "src/images/boss/anglerfish-boss/Idle.png";
    private static final String HURT_SHEET_PATH = "src/images/boss/anglerfish-boss/Hurt.png";
    private static final String ATTACK_SHEET_PATH = "src/images/boss/anglerfish-boss/Attack.png";
    private static final String WALK_SHEET_PATH = "src/images/boss/anglerfish-boss/Walk.png";
    private static final String DEATH_SHEET_PATH = "src/images/boss/anglerfish-boss/Death.png";

    private static final ImageIcon idleSheet = new ImageIcon(IDLE_SHEET_PATH);
    private static final ImageIcon hurtSheet = new ImageIcon(HURT_SHEET_PATH);
    private static final ImageIcon attackSheet = new ImageIcon(ATTACK_SHEET_PATH);
    private static final ImageIcon walkSheet = new ImageIcon(WALK_SHEET_PATH);
    private static final ImageIcon deathSheet = new ImageIcon(DEATH_SHEET_PATH);

    private static final List<Rectangle> idleAnimationClips = List.of(
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
    private static final List<Rectangle> walkAnimationClips = List.of(
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

    private final Random random = new Random();
    private final List<EnemyProjectile> pendingProjectiles = new ArrayList<>();
    private final List<BomberFish> pendingSummons = new ArrayList<>();
    private final int homeX;
    private AttackState state = AttackState.IDLE;
    private int stateTicks;
    private int attackCooldown = BOSS_PHASE_ONE_COOLDOWN_TICKS;
    private int laserInterval;
    private boolean deathFinished;
    private double idleWave;

    public Anglerfish(Player player) {
        super(player, BOARD_WIDTH - 45*BOSS_SCALE, 115, 48*BOSS_SCALE, 48*BOSS_SCALE,
                BOSS_MAX_HEALTH, BOSS_CONTACT_DAMAGE, 5000,
                new Color(54, 115, 82));
        homeX = getX();
        setHitboxScale(0.6, 0.45);
        setFlippedHorizontally(true);
        updateAnimationFrames();
    }

    @Override
    public Rectangle getBounds() {
        Rectangle bounds = super.getBounds();
        bounds.translate(HITBOX_X_OFFSET, HITBOX_Y_OFFSET);
        return bounds;
    }

    @Override
    public void act() {
        if (state == AttackState.DYING) {
            updateDeath();
            return;
        }

        if (state == AttackState.HURT && isAnimationFinished()) {
            x = homeX;
            returnToIdle();
        }

        updatePhaseColor();

        switch (state) {
            case IDLE:
                updateIdle();
                break;
            case LASER_CHARGE:
                if (--stateTicks <= 0) {
                    state = AttackState.LASER;
                    stateTicks = BOSS_LASER_DURATION_TICKS;
                    laserInterval = 0;
                }
                break;
            case LASER:
                updateLaser();
                break;
            case BITE_WARNING:
                if (--stateTicks <= 0) {
                    state = AttackState.BITE_OUT;
                    updateAnimationFrames();
                }
                break;
            case BITE_OUT:
                x -= 11;
                if (getX() <= 30) {
                    state = AttackState.BITE_RETURN;
                }
                break;
            case BITE_RETURN:
                x += 8;
                if (getX() >= homeX) {
                    x = homeX;
                    returnToIdle();
                }
                break;
            case SUMMON:
                createSummons();
                returnToIdle();
                break;
            case HURT:
                break;
            default:
                break;
        }
    }

    private void updateIdle() {
        idleWave += 0.045;
        y += Math.sin(idleWave) * 0.15;
        if (--attackCooldown <= 0) {
            chooseAttack();
        }
    }

    private void chooseAttack() {
        switch (random.nextInt(3)) {
            case 0:
                state = AttackState.LASER_CHARGE;
                stateTicks = BOSS_LASER_CHARGE_TICKS;
                break;
            case 1:
                state = AttackState.BITE_WARNING;
                stateTicks = BOSS_BITE_WARNING_TICKS;
                break;
            default:
                state = AttackState.SUMMON;
                break;
        }
        updateAnimationFrames();
    }

    private void updateLaser() {
        double playerCenter = player.getY() + player.getRenderHeight() / 2.0;
        double bossCenter = y + getRenderHeight() / 2.0;
        y += Math.signum(playerCenter - bossCenter) * 2.0;
        y = Math.max(45, Math.min(BOARD_HEIGHT - getRenderHeight() - 20, y));

        if (laserInterval-- <= 0) {
            pendingProjectiles.add(new BossBubble(getX() - 18,
                    getY() + getRenderHeight() / 2, 1));
            laserInterval = Math.max(1, BOSS_LASER_INTERVAL_TICKS);
        }

        if (--stateTicks <= 0) {
            returnToIdle();
        }
    }

    private void createSummons() {
        for (int index = 0; index < 3; index++) {
            int yOffset = 80 + index * 115;
            pendingSummons.add(new BomberFish(player, getX() - 20,
                    getY() + yOffset, random));
        }
    }

    private void returnToIdle() {
        state = AttackState.IDLE;
        attackCooldown = health <= BOSS_PHASE_TWO_HEALTH
                ? BOSS_PHASE_TWO_COOLDOWN_TICKS
                : BOSS_PHASE_ONE_COOLDOWN_TICKS;
        updateAnimationFrames();
    }

    private void updateAnimationFrames() {
        if (state == AttackState.DYING) {
            setImage(deathSheet.getImage());
            setAnimationFrames(deathAnimationClips);
            setAnimationLooping(false);
            return;
        }

        if (state == AttackState.HURT) {
            setImage(hurtSheet.getImage());
            setAnimationFrames(hurtAnimationClips);
            setAnimationLooping(false);
            return;
        }

        if (state == AttackState.LASER_CHARGE || state == AttackState.LASER || state == AttackState.BITE_WARNING) {
            setImage(attackSheet.getImage());
            setAnimationFrames(attackAnimationClips);
            setAnimationLooping(false);
            return;
        }

        if (state == AttackState.BITE_OUT || state == AttackState.BITE_RETURN) {
            setImage(walkSheet.getImage());
            setAnimationFrames(walkAnimationClips);
            return;
        }

        setImage(idleSheet.getImage());
        setAnimationFrames(idleAnimationClips);
    }

    private void updatePhaseColor() {
        if (health <= BOSS_PHASE_TWO_HEALTH) {
            setPlaceholderColor(new Color(155, 78, 100));
        } else {
            setPlaceholderColor(new Color(54, 115, 82));
        }
    }

    @Override
    public boolean damage(int amount) {
        if (state == AttackState.DYING || amount <= 0) {
            return false;
        }

        health -= amount;
        if (health <= 0) {
            health = 0;
            state = AttackState.DYING;
            stateTicks = BOSS_DEATH_TICKS;
            setDying(true);
            updateAnimationFrames();
            return true;
        }

        state = AttackState.HURT;
        updateAnimationFrames();
        return false;
    }

    private void updateDeath() {
        setPlaceholderColor((stateTicks / 5) % 2 == 0
                ? Color.WHITE
                : new Color(220, 70, 90));
        if (--stateTicks <= 0) {
            deathFinished = true;
            die();
        }
    }

    public List<EnemyProjectile> takePendingProjectiles() {
        List<EnemyProjectile> result = new ArrayList<>(pendingProjectiles);
        pendingProjectiles.clear();
        return result;
    }

    public List<BomberFish> takePendingSummons() {
        List<BomberFish> result = new ArrayList<>(pendingSummons);
        pendingSummons.clear();
        return result;
    }

    public boolean isDeathFinished() {
        return deathFinished;
    }

    public String getAttackName() {
        return state.name().replace('_', ' ');
    }
}
