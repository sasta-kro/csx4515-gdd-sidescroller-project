package gdd.sprite;

import gdd.RunState;
import static gdd.Global.*;
import gdd.powerup.WeaponType;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Player extends Sprite {

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
        super(state.getPlayerX(), state.getPlayerY(),
                PLAYER_WIDTH, PLAYER_HEIGHT, COLOR_PLAYER);
        health = state.getHealth();
        setHitboxScale(0.88);
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
        if (invincibilityTicks > 0) {
            invincibilityTicks--;
        }
        if (healFlashTicks > 0) {
            healFlashTicks--;
        }
        if (shotCooldownTicks > 0) {
            shotCooldownTicks--;
        }
        if (burstIntervalTicks > 0) {
            burstIntervalTicks--;
        }

        if (speedPowerupTicks > 0 && --speedPowerupTicks == 0) {
            speedLevel = 0;
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
        return true;
    }

    public void heal() {
        health = Math.min(PLAYER_MAX_HEALTH, health + 1);
        healFlashTicks = secondsToTicks(0.25);
    }

    public void applySpeedUp() {
        speedLevel = Math.min(2, speedLevel + 1);
        speedPowerupTicks = SPEED_POWERUP_TICKS;
    }

    public void applyMultiShot() {
        if (weaponType != WeaponType.MULTI_SHOT) {
            multiShotLevel = 0;
        }
        weaponType = WeaponType.MULTI_SHOT;
        multiShotLevel = Math.min(4, multiShotLevel + 1);
        weaponPowerupTicks = MULTI_SHOT_TICKS;
        burstRemaining = 0;
    }

    public void applyMegaShot() {
        weaponType = WeaponType.MEGA_SHOT;
        multiShotLevel = 0;
        weaponPowerupTicks = WEAPON_POWERUP_TICKS;
        burstRemaining = 0;
    }

    public void applySplitShot() {
        weaponType = WeaponType.SPLIT_SHOT;
        multiShotLevel = 0;
        weaponPowerupTicks = WEAPON_POWERUP_TICKS;
        burstRemaining = 0;
    }

    public void clearTemporaryPowerups() {
        speedLevel = 0;
        speedPowerupTicks = 0;
        clearWeapon();
    }

    private void clearWeapon() {
        weaponType = WeaponType.BASE;
        multiShotLevel = 0;
        weaponPowerupTicks = 0;
        burstRemaining = 0;
        burstIntervalTicks = 0;
    }

    public void syncTo(RunState state) {
        state.setHealth(health);
        state.setPlayerPosition(getX(), getY());
    }

    public void restorePosition(int oldX, int oldY) {
        x = oldX;
        y = oldY;
    }

    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                movingUp = true;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                movingDown = true;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                movingLeft = true;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                movingRight = true;
                break;
            case KeyEvent.VK_SPACE:
                firing = true;
                break;
            default:
                break;
        }
    }

    public void keyReleased(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                movingUp = false;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                movingDown = false;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                movingLeft = false;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                movingRight = false;
                break;
            case KeyEvent.VK_SPACE:
                firing = false;
                break;
            default:
                break;
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
