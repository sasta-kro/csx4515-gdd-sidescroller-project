package gdd;

public class RunState {

    private int score;
    private int health;
    private int playerX;
    private int playerY;

    public RunState() {
        reset();
    }

    public void reset() {
        score = 0;
        health = Global.PLAYER_MAX_HEALTH;
        playerX = Global.PLAYER_START_X;
        playerY = Global.PLAYER_START_Y;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        score += Math.max(0, amount);
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(Global.PLAYER_MAX_HEALTH, health));
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
}
