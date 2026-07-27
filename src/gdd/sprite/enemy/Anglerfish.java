package gdd.sprite.enemy;

import gdd.sprite.Player;

import static gdd.Global.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

public class Anglerfish extends Enemy {

    private static final int HITBOX_X_OFFSET = 6 * BOSS_SCALE;
    private static final int HITBOX_Y_OFFSET = BOSS_SCALE;

    private static final int PHASE_ONE_IDLE_TICKS = secondsToTicks(2);
    private static final int PHASE_TWO_IDLE_TICKS = secondsToTicks(0.5);
    private static final double PHASE_ONE_LASER_CHASE_SPEED = 3.0;
    private static final double PHASE_TWO_LASER_CHASE_SPEED = 6.0;

    private static final double BITE_DASH_SPEED = 11.0;
    private static final double BITE_RETURN_SPEED = 8.0;

    private static final int PHASE_ONE_SUMMON_COUNT = 3;
    private static final int PHASE_TWO_SUMMON_COUNT = 5;
    private static final int SUMMON_VERTICAL_SPACING = 55;

    private static final Color BUBBLE_COLOR = new Color(154, 133, 113);
    private static final Color PHASE_TWO_TINT = new Color(235, 75, 115);
    private static final float PHASE_TWO_TINT_STRENGTH = 0.28f;

    private enum AttackState {
        IDLE,
        LASER_CHARGE,
        LASER,
        BITE_WARNING,
        BITE_OUT,
        BITE_RETURN,
        SUMMON,
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

    private static final Image phaseTwoIdleSheet =
            createPhaseTwoTint(idleSheet.getImage());
    private static final Image phaseTwoHurtSheet =
            createPhaseTwoTint(hurtSheet.getImage());
    private static final Image phaseTwoAttackSheet =
            createPhaseTwoTint(attackSheet.getImage());
    private static final Image phaseTwoWalkSheet =
            createPhaseTwoTint(walkSheet.getImage());
    private static final Image phaseTwoDeathSheet =
            createPhaseTwoTint(deathSheet.getImage());

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
    private static final List<Rectangle> attackOpeningAnimationClips = List.of(
            new Rectangle(0, 0, 48, 48),
            new Rectangle(48, 0, 48, 48),
            new Rectangle(48*2, 0, 48, 48)
    );
    private static final List<Rectangle> attackLoopAnimationClips = List.of(
            new Rectangle(48*2, 0, 48, 48),
            new Rectangle(48*3, 0, 48, 48)
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
    private AttackState previousAttack;
    private int consecutiveAttackCount;
    private boolean laserChargeStarted;
    private boolean laserShotStarted;
    private int stateTicks;
    private int attackCooldown = PHASE_ONE_IDLE_TICKS;
    private int laserInterval;
    private double biteXSpeed;
    private double biteYSpeed;
    private double biteStartY;
    private boolean hurt;
    private boolean deathFinished;
    private double idleWave;
    private int hitboxTopBound;
    private int hitboxBottomBound = BOARD_HEIGHT;

    public Anglerfish(Player player) {
        super(player, BOARD_WIDTH - 45*BOSS_SCALE, 115, 48*BOSS_SCALE, 48*BOSS_SCALE,
                BOSS_MAX_HEALTH, BOSS_CONTACT_DAMAGE, 5000,
                new Color(54, 115, 82));
        homeX = getX();
        setHitboxScale(0.6, 0.45);
        setFlippedHorizontally(true);
        updateAnimationFrames();
    }

    public void setVerticalHitboxBounds(int top, int bottom) {
        hitboxTopBound = top;
        hitboxBottomBound = bottom;
        keepHitboxInsideVerticalBounds();
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

        if (hurt && isAnimationFinished()) {
            hurt = false;
            updateAnimationFrames();
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
                    updateAnimationFrames();
                }
                break;
            case LASER:
                updateLaser();
                break;
            case BITE_WARNING:
                if (--stateTicks <= 0) {
                    startBite();
                }
                break;
            case BITE_OUT:
                updateBiteOut();
                break;
            case BITE_RETURN:
                updateBiteReturn();
                break;
            case SUMMON:
                createSummons();
                returnToIdle();
                break;
            default:
                break;
        }
    }

    private void updateIdle() {
        idleWave += 0.045;
        y += Math.sin(idleWave) * 0.15;
        keepHitboxInsideVerticalBounds();
        if (--attackCooldown <= 0) {
            chooseAttack();
        }
    }

    private void chooseAttack() {
        AttackState selectedAttack;
        do {
            selectedAttack = switch (random.nextInt(3)) {
                case 0 -> AttackState.LASER_CHARGE;
                case 1 -> AttackState.BITE_WARNING;
                default -> AttackState.SUMMON;
            };
        } while (consecutiveAttackCount >= 3
                && selectedAttack == previousAttack);

        if (selectedAttack == previousAttack) {
            consecutiveAttackCount++;
        } else {
            previousAttack = selectedAttack;
            consecutiveAttackCount = 1;
        }

        state = selectedAttack;
        switch (state) {
            case LASER_CHARGE:
                stateTicks = BOSS_LASER_CHARGE_TICKS;
                laserChargeStarted = true;
                break;
            case BITE_WARNING:
                stateTicks = BOSS_BITE_WARNING_TICKS;
                break;
            default:
                break;
        }
        updateAnimationFrames();
    }

    public boolean consumeLaserChargeStarted() {
        boolean result = laserChargeStarted;
        laserChargeStarted = false;
        return result;
    }

    public boolean consumeLaserShotStarted() {
        boolean result = laserShotStarted;
        laserShotStarted = false;
        return result;
    }

    private void updateLaser() {
        double playerCenter = player.getY() + player.getRenderHeight() / 2.0;
        Rectangle bossHitbox = getBounds();
        double bossCenter = bossHitbox.getCenterY();
        double distanceToPlayer = playerCenter - bossCenter;
        double chaseSpeed = isPhaseTwo()
                ? PHASE_TWO_LASER_CHASE_SPEED
                : PHASE_ONE_LASER_CHASE_SPEED;
        y += Math.max(-chaseSpeed,
                Math.min(chaseSpeed, distanceToPlayer));
        keepHitboxInsideVerticalBounds();

        if (laserInterval-- <= 0) {
            pendingProjectiles.add(createBubbleAtHitboxEdge());
            laserShotStarted = true;
            laserInterval = Math.max(1, BOSS_LASER_INTERVAL_TICKS);
        }

        if (--stateTicks <= 0) {
            returnToIdle();
        }
    }

    private void startBite() {
        state = AttackState.BITE_OUT;
        biteStarted = true;
        biteStartY = y;

        if (isPhaseTwo()) {
            Rectangle bossHitbox = getBounds();
            Rectangle playerHitbox = player.getBounds();
            double targetX = playerHitbox.getCenterX()
                    - bossHitbox.getCenterX();
            double targetY = playerHitbox.getCenterY()
                    - bossHitbox.getCenterY();
            double distance = Math.max(1.0, Math.hypot(targetX, targetY));
            biteXSpeed = targetX / distance * BITE_DASH_SPEED;
            biteYSpeed = targetY / distance * BITE_DASH_SPEED;
        } else {
            biteXSpeed = -BITE_DASH_SPEED;
            biteYSpeed = 0;
        }

        updateAnimationFrames();
    }

    public boolean consumeBiteStarted() {
        boolean result = biteStarted;
        biteStarted = false;
        return result;
    }

    private void updateBiteOut() {
        x += biteXSpeed;
        y += biteYSpeed;
        keepHitboxInsideVerticalBounds();

        Rectangle hitbox = getBounds();
        boolean reachedHorizontalEdge = biteXSpeed < 0
                ? hitbox.x <= 0
                : hitbox.x + hitbox.width >= BOARD_WIDTH;
        if (reachedHorizontalEdge) {
            state = AttackState.BITE_RETURN;
            updateAnimationFrames();
        }
    }

    private void updateBiteReturn() {
        double distanceX = homeX - x;
        double distanceY = biteStartY - y;
        double distance = Math.hypot(distanceX, distanceY);

        if (distance <= BITE_RETURN_SPEED) {
            x = homeX;
            y = biteStartY;
            returnToIdle();
            return;
        }

        x += distanceX / distance * BITE_RETURN_SPEED;
        y += distanceY / distance * BITE_RETURN_SPEED;
        keepHitboxInsideVerticalBounds();
    }

    private void keepHitboxInsideVerticalBounds() {
        Rectangle hitbox = getBounds();
        if (hitbox.y < hitboxTopBound) {
            y += hitboxTopBound - hitbox.y;
        }

        if (hitbox.y + hitbox.height > hitboxBottomBound) {
            y -= hitbox.y + hitbox.height - hitboxBottomBound;
        }
    }

    private BossBubble createBubbleAtHitboxEdge() {
        Rectangle hitbox = getBounds();
        int spawnX = hitbox.x - BossBubble.WIDTH / 2;
        int spawnY = hitbox.y + (hitbox.height - BossBubble.HEIGHT) / 2;
        Color bubbleColor = isPhaseTwo()
                ? PHASE_TWO_TINT
                : BUBBLE_COLOR;
        return new BossBubble(spawnX, spawnY, 1, bubbleColor);
    }

    private void createSummons() {
        int spawnX = spawnXAtLeftEdge(BomberFish.SIZE);
        int centerY = spawnYAtCenter(BomberFish.SIZE);
        int summonCount = isPhaseTwo()
                ? PHASE_TWO_SUMMON_COUNT
                : PHASE_ONE_SUMMON_COUNT;
        int middleIndex = summonCount / 2;

        for (int index = 0; index < summonCount; index++) {
            int spawnY = centerY
                    + (index - middleIndex) * SUMMON_VERTICAL_SPACING;
            pendingSummons.add(new BomberFish(
                    player, spawnX, spawnY, random));
        }
    }

    private int spawnXAtLeftEdge(int entityWidth) {
        return getX() - entityWidth / 2;
    }

    private int spawnYAtCenter(int entityHeight) {
        return getY() + (getRenderHeight() - entityHeight) / 2;
    }

    private void returnToIdle() {
        state = AttackState.IDLE;
        attackCooldown = isPhaseTwo()
                ? PHASE_TWO_IDLE_TICKS
                : PHASE_ONE_IDLE_TICKS;
        updateAnimationFrames();
    }

    private boolean isPhaseTwo() {
        return health <= BOSS_PHASE_TWO_HEALTH;
    }

    private void updateAnimationFrames() {
        if (state == AttackState.DYING) {
            setImage(phaseImage(deathSheet, phaseTwoDeathSheet));
            setAnimationFrames(deathAnimationClips);
            setAnimationLooping(false);
            return;
        }

        if (hurt) {
            setImage(phaseImage(hurtSheet, phaseTwoHurtSheet));
            setAnimationFrames(hurtAnimationClips);
            setAnimationLooping(false);
            return;
        }

        if (state == AttackState.LASER_CHARGE
                || state == AttackState.BITE_WARNING) {
            setImage(phaseImage(attackSheet, phaseTwoAttackSheet));
            setAnimationFrames(attackOpeningAnimationClips);
            setAnimationLooping(false);
            return;
        }

        if (state == AttackState.LASER
                || state == AttackState.BITE_OUT) {
            setImage(phaseImage(attackSheet, phaseTwoAttackSheet));
            setAnimationFrames(attackLoopAnimationClips);
            return;
        }

        if (state == AttackState.BITE_RETURN) {
            setImage(phaseImage(walkSheet, phaseTwoWalkSheet));
            setAnimationFrames(walkAnimationClips);
            return;
        }

        setImage(phaseImage(idleSheet, phaseTwoIdleSheet));
        setAnimationFrames(idleAnimationClips);
    }

    private Image phaseImage(ImageIcon normalSheet, Image phaseTwoSheet) {
        return health <= BOSS_PHASE_TWO_HEALTH
                ? phaseTwoSheet
                : normalSheet.getImage();
    }

    private static BufferedImage createPhaseTwoTint(Image source) {
        BufferedImage tintedImage = new BufferedImage(
                source.getWidth(null), source.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = tintedImage.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.setComposite(AlphaComposite.SrcAtop.derive(
                PHASE_TWO_TINT_STRENGTH));
        graphics.setColor(PHASE_TWO_TINT);
        graphics.fillRect(0, 0,
                tintedImage.getWidth(), tintedImage.getHeight());
        graphics.dispose();
        return tintedImage;
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

        boolean enteringPhaseTwo = !isPhaseTwo()
                && health - amount <= BOSS_PHASE_TWO_HEALTH;
        health -= amount;
        if (health <= 0) {
            health = 0;
            hurt = false;
            state = AttackState.DYING;
            stateTicks = BOSS_DEATH_TICKS;
            setDying(true);
            updateAnimationFrames();
            return true;
        }

        if (enteringPhaseTwo && state == AttackState.IDLE) {
            attackCooldown = Math.min(
                    attackCooldown, PHASE_TWO_IDLE_TICKS);
        }

        hurt = true;
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
