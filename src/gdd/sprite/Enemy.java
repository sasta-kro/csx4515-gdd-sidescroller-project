package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

public abstract class Enemy extends Sprite {

    protected final Player player;
    protected int health;
    private final int contactDamage;
    private final int scoreValue;

    protected Enemy(Player player, int x, int y, int width, int height,
            int health, int contactDamage, int scoreValue, Color color) {
        super(x, y, width, height, color);
        this.player = player;
        this.health = health;
        this.contactDamage = contactDamage;
        this.scoreValue = scoreValue;
        setHitboxScale(0.9, 0.9);
    }

    protected void moveWithWorld() {
        x -= WORLD_SCROLL_SPEED;
    }

    public boolean damage(int amount) {
        if (!isVisible() || amount <= 0) {
            return false;
        }

        health -= amount;
        if (health <= 0) {
            die();
            return true;
        }
        return false;
    }

    public int getHealth() {
        return health;
    }

    public int getContactDamage() {
        return contactDamage;
    }

    public int getScoreValue() {
        return scoreValue;
    }
}
