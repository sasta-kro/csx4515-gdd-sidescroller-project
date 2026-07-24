package gdd.sprite;

import java.awt.Color;

public class Bubble extends Sprite {

    private final int damage;
    private final int speed;
    private final boolean playerOwned;

    public Bubble(int x, int y, int width, int height, int damage,
            int speed, Color color, boolean playerOwned) {
        super(x, y, width, height, color);
        this.damage = damage;
        this.speed = speed;
        this.playerOwned = playerOwned;
        setHitboxScale(0.85);
    }

    @Override
    public void act() {
        x += playerOwned ? speed : -speed;
        if (isOutsideViewport()) {
            die();
        }
    }

    public int getDamage() {
        return damage;
    }

    public boolean isPlayerOwned() {
        return playerOwned;
    }
}
