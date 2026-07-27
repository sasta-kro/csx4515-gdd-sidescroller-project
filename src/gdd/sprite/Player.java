package gdd.sprite;

import gdd.RunState;
import static gdd.Global.*;
import gdd.powerup.WeaponType;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final String ASSET_SHEET_PATH = "src/images/player/player_fish_spritesheet.png";

    // orange fish
    /// Rectangle values are: top-left x, top-left y, width, height in source-image pixels.
    private static final List<Rectangle> originalAnimationClips = List.of(
            new Rectangle(64*8, 64*9, 64, 64),
            new Rectangle(64*9, 64*9, 64, 64),
            new Rectangle(64*10, 64*9, 64, 64),
            new Rectangle(64*11, 64*9, 64, 64)
    );

    // pink fish
    /// Rectangle values are: top-left x, top-left y, width, height in source-image pixels.
    private static final List<Rectangle> speedAnimationClips = List.of(
            new Rectangle(64*4, 64*17, 64, 64),
            new Rectangle(64*5, 64*17, 64, 64),
            new Rectangle(64*6, 64*17, 64, 64),
            new Rectangle(64*7, 64*17, 64, 64)
    );

    // cyan fish
    /// Rectangle values are: top-left x, top-left y, width, height in source-image pixels.
    private static final List<Rectangle> multiShotAnimationClips = List.of(
            new Rectangle(64*4, 64*9, 64, 64),
            new Rectangle(64*5, 64*9, 64, 64),
            new Rectangle(64*6, 64*9, 64, 64),
            new Rectangle(64*7, 64*9, 64, 64)
    );

    // yellow fish
    /// Rectangle values are: top-left x, top-left y, width, height in source-image pixels.
    private static final List<Rectangle> megaShotAnimationClips = List.of(
            new Rectangle(64*12, 64*9, 64, 64),
            new Rectangle(64*13, 64*9, 64, 64),
            new Rectangle(64*14, 64*9, 64, 64),
            new Rectangle(64*15, 64*9, 64, 64)
    );

    // purple fish
    /// Rectangle values are: top-left x, top-left y, width, height in source-image pixels.
    private static final List<Rectangle> splitShotAnimationClips = List.of(
            new Rectangle(0, 64*9, 64, 64),
            new Rectangle(64, 64*9, 64, 64),
            new Rectangle(64*2, 64*9, 64, 64),
            new Rectangle(64*3, 64*9, 64, 64)
    );

    // green fish (few frames only)
    /// Rectangle values are: top-left x, top-left y, width, height in source-image pixels.
    private static final List<Rectangle> healAnimationClips = List.of(
            new Rectangle(0, 64*17, 64, 64),
            new Rectangle(64, 64*17, 64, 64),
            new Rectangle(64*2, 64*17, 64, 64),
            new Rectangle(64*3, 64*17, 64, 64)
    );

    // red fish (few frames only)
    /// Rectangle values are: top-left x, top-left y, width, height in source-image pixels.
    private static final List<Rectangle> damageAnimationClips = List.of(
            new Rectangle(64*12, 64*17, 64, 64),
            new Rectangle(64*13, 64*17, 64, 64),
            new Rectangle(64*14, 64*17, 64, 64),
            new Rectangle(64*15, 64*17, 64, 64)
    );

    private boolean movingUp;
    private boolean movingDown;
    private boolean movingLeft;
    private boolean movingRight;
    private boolean firing;

    private int health;
    private int invincibilityTicks;
    private int healFlashTicks;
    private int speedLevel;
    private int speedPowerupTicks;
    private WeaponType weaponType = WeaponType.BASE;
    private int multiShotLevel;
    private int weaponPowerupTicks;
    private int shotCooldownTicks;
    private int burstRemaining;
    private int burstIntervalTicks;

    public Player(RunState state) {
        super(state.getPlayerX(), state.getPlayerY(), PLAYER_WIDTH, PLAYER_HEIGHT, COLOR_PLAYER);
        health = state.getHealth();
        speedLevel = state.getSpeedLevel();
        speedPowerupTicks = state.getSpeedPowerupTicks();
        weaponType = state.getWeaponType();
        multiShotLevel = state.getMultiShotLevel();
        weaponPowerupTicks = state.getWeaponPowerupTicks();
        setHitboxScale(0.88, 0.80);
        setFlippedHorizontally(true);
        loadPlayerAnimation();
    }

    private void loadPlayerAnimation() {
        var assetSheet = new ImageIcon(ASSET_SHEET_PATH);
        setImage(assetSheet.getImage());
        updateAnimationFrames();
    }

    private void updateAnimationFrames() {
        if (invincibilityTicks > 0) {
            setAnimationFrames(damageAnimationClips);
            return;
        }

        if (healFlashTicks > 0) {
            setAnimationFrames(healAnimationClips);
            return;
        }

        switch (weaponType) {
            case MULTI_SHOT -> setAnimationFrames(multiShotAnimationClips);
            case MEGA_SHOT -> setAnimationFrames(megaShotAnimationClips);
            case SPLIT_SHOT -> setAnimationFrames(splitShotAnimationClips);

            default -> {
                if (speedLevel > 0) {
                    setAnimationFrames(speedAnimationClips);
                } else {
                    setAnimationFrames(originalAnimationClips);
                }
            }
        }
    }

    @Override
    public void act() {
        int speed = getCurrentSpeed();
        dx = 0;
        dy = 0;

        if (movingLeft) {
            dx -= speed;
        }
        if (movingRight) {
            dx += speed;
        }
        if (movingUp) {
            dy -= speed;
        }
        if (movingDown) {
            dy += speed;
        }

        x += dx;
        y += dy;
        clampToViewport();
        updateCounters();
        setPlaceholderColor(getActiveColor());
    }

    private void clampToViewport() {
        x = Math.max(0, Math.min(BOARD_WIDTH - getRenderWidth(), x));
        y = Math.max(0, Math.min(BOARD_HEIGHT - getRenderHeight() - 32, y));
    }

    private void updateCounters() {
        if (invincibilityTicks > 0 && --invincibilityTicks == 0) {
            updateAnimationFrames();
        }
        if (healFlashTicks > 0 && --healFlashTicks == 0) {
            updateAnimationFrames();
        }
        if (shotCooldownTicks > 0) {
            shotCooldownTicks--;
        }
        if (burstIntervalTicks > 0) {
            burstIntervalTicks--;
        }

        if (speedPowerupTicks > 0 && --speedPowerupTicks == 0) {
            speedLevel = 0;
            updateAnimationFrames();
        }

        if (weaponPowerupTicks > 0 && --weaponPowerupTicks == 0) {
            clearWeapon();
        }
    }

    public List<Bubble> createBubbles() {
        List<Bubble> bubbles = new ArrayList<>();

        if (burstRemaining > 0 && burstIntervalTicks == 0) {
            bubbles.add(createStandardBubble(0));
            burstRemaining--;
            if (burstRemaining > 0) {
                burstIntervalTicks = BURST_INTERVAL_TICKS;
            } else {
                shotCooldownTicks = BASE_SHOT_COOLDOWN_TICKS;
            }
            return bubbles;
        }

        if (!firing || shotCooldownTicks > 0 || burstRemaining > 0) {
            return bubbles;
        }

        switch (weaponType) {
            case MULTI_SHOT:
                burstRemaining = getMultiShotBubbleCount();
                bubbles.add(createStandardBubble(0));
                burstRemaining--;
                burstIntervalTicks = BURST_INTERVAL_TICKS;
                break;
            case MEGA_SHOT:
                bubbles.add(new Bubble(getShotX(), getShotY() - 4,
                        24, 18, 2, BUBBLE_SPEED, getActiveColor(), true));
                shotCooldownTicks = (int) Math.round(BASE_SHOT_COOLDOWN_TICKS * 1.125);
                break;
            case SPLIT_SHOT:
                bubbles.add(createStandardBubble(-14));
                bubbles.add(createStandardBubble(0));
                bubbles.add(createStandardBubble(14));
                shotCooldownTicks = BASE_SHOT_COOLDOWN_TICKS;
                break;
            default:
                bubbles.add(createStandardBubble(0));
                shotCooldownTicks = BASE_SHOT_COOLDOWN_TICKS;
                break;
        }

        return bubbles;
    }

    private Bubble createStandardBubble(int yOffset) {
        return new Bubble(getShotX(), getShotY() + yOffset,
                BUBBLE_WIDTH, BUBBLE_HEIGHT, BUBBLE_DAMAGE,
                BUBBLE_SPEED, getActiveColor(), true);
    }

    private int getShotX() {
        return getX() + getRenderWidth();
    }

    private int getShotY() {
        return getY() + (getRenderHeight() - BUBBLE_HEIGHT) / 2;
    }

    private int getMultiShotBubbleCount() {
        return multiShotLevel == 0 ? 1 : multiShotLevel + 2;
    }

    public boolean damage(int amount) {
        if (amount <= 0 || invincibilityTicks > 0 || health <= 0) {
            return false;
        }

        health = Math.max(0, health - amount);
        invincibilityTicks = PLAYER_INVINCIBILITY_TICKS;
        updateAnimationFrames();
        return true;
    }

    public void heal() {
        health = Math.min(PLAYER_MAX_HEALTH, health + 1);
        healFlashTicks = secondsToTicks(0.25);
        updateAnimationFrames();
    }

    public void applySpeedUp() {
        speedLevel = Math.min(2, speedLevel + 1);
        speedPowerupTicks = SPEED_POWERUP_TICKS;
        updateAnimationFrames();
    }

    public void applyMultiShot() {
        if (weaponType != WeaponType.MULTI_SHOT) {
            multiShotLevel = 0;
        }
        weaponType = WeaponType.MULTI_SHOT;
        multiShotLevel = Math.min(4, multiShotLevel + 1);
        weaponPowerupTicks = MULTI_SHOT_TICKS;
        burstRemaining = 0;
        updateAnimationFrames();
    }

    public void applyMegaShot() {
        weaponType = WeaponType.MEGA_SHOT;
        multiShotLevel = 0;
        weaponPowerupTicks = WEAPON_POWERUP_TICKS;
        burstRemaining = 0;
        updateAnimationFrames();
    }

    public void applySplitShot() {
        weaponType = WeaponType.SPLIT_SHOT;
        multiShotLevel = 0;
        weaponPowerupTicks = WEAPON_POWERUP_TICKS;
        burstRemaining = 0;
        updateAnimationFrames();
    }

    private void clearWeapon() {
        weaponType = WeaponType.BASE;
        multiShotLevel = 0;
        weaponPowerupTicks = 0;
        burstRemaining = 0;
        burstIntervalTicks = 0;
        updateAnimationFrames();
    }

    public void syncTo(RunState state) {
        state.setHealth(health);
        state.setPlayerPosition(getX(), getY());
        state.setSpeedPowerup(speedLevel, speedPowerupTicks);
        state.setWeaponPowerup(weaponType, multiShotLevel, weaponPowerupTicks);
    }

    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> movingUp = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> movingDown = true;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> movingLeft = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> movingRight = true;
            case KeyEvent.VK_SPACE -> firing = true;
            default -> {}
        }
    }

    public void keyReleased(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> movingUp = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> movingDown = false;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> movingLeft = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> movingRight = false;
            case KeyEvent.VK_SPACE -> firing = false;
            default -> {}
        }
    }

    @Override
    public void draw(Graphics g) {
        if (isDying()) {
            Color original = getPlaceholderColor();
            setPlaceholderColor((invincibilityTicks / 3) % 2 == 0
                    ? new Color(245, 70, 70)
                    : Color.WHITE);
            super.draw(g);
            setPlaceholderColor(original);
            return;
        }

        if (invincibilityTicks > 0 && (invincibilityTicks / 4) % 2 == 0) {
            Color original = getPlaceholderColor();
            setPlaceholderColor(new Color(245, 70, 70));
            super.draw(g);
            setPlaceholderColor(original);
            return;
        }
        super.draw(g);
    }

    private Color getActiveColor() {
        if (healFlashTicks > 0) {
            return COLOR_HEAL;
        }

        switch (weaponType) {
            case MULTI_SHOT:
                return COLOR_MULTI;
            case MEGA_SHOT:
                return COLOR_MEGA;
            case SPLIT_SHOT:
                return COLOR_SPLIT;
            default:
                return speedLevel > 0 ? COLOR_SPEED : COLOR_PLAYER;
        }
    }

    public int getHealth() {
        return health;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public int getCurrentSpeed() {
        if (speedLevel == 1) {
            return PLAYER_SPEED_LEVEL_1;
        }
        if (speedLevel >= 2) {
            return PLAYER_SPEED_LEVEL_2;
        }
        return PLAYER_BASE_SPEED;
    }

    public int getSpeedPowerupTicks() {
        return speedPowerupTicks;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public int getMultiShotLevel() {
        return multiShotLevel;
    }

    public int getWeaponPowerupTicks() {
        return weaponPowerupTicks;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
