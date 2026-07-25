package gdd;

import gdd.powerup.WeaponType;

/// stores current game state like player score, hp, position.
/// also handles player hp and score validation
public class RunState {

    private int score;
    private int health;
    private int playerX;
    private int playerY;
    private int speedLevel;
    private int speedPowerupTicks;
    private WeaponType weaponType;
    private int multiShotLevel;
    private int weaponPowerupTicks;

    public RunState() {
        reset();
    }

    public void reset() {
        score = 0;
        health = Global.PLAYER_MAX_HEALTH;
        playerX = Global.PLAYER_START_X;
        playerY = Global.PLAYER_START_Y;
        speedLevel = 0;
        speedPowerupTicks = 0;
        weaponType = WeaponType.BASE;
        multiShotLevel = 0;
        weaponPowerupTicks = 0;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        score += Math.max(0, amount);   // TODO: why max?? why not just += amount  (cuz amount is always positive right?)
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        if (health < 0) {
            this.health = 0;
        } else if (health > Global.PLAYER_MAX_HEALTH) {
            this.health = Global.PLAYER_MAX_HEALTH;
        } else {
            this.health = health;
        }
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public void setPlayerPosition(int x, int y) {
        playerX = x;
        playerY = y;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public int getSpeedPowerupTicks() {
        return speedPowerupTicks;
    }

    public void setSpeedPowerup(int level, int remainingTicks) {
        speedLevel = level;
        speedPowerupTicks = remainingTicks;
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

    public void setWeaponPowerup(WeaponType type, int multiShotLevel,
            int remainingTicks) {
        weaponType = type;
        this.multiShotLevel = multiShotLevel;
        weaponPowerupTicks = remainingTicks;
    }
}
