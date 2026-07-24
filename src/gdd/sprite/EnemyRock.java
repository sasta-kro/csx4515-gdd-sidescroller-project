package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

public class EnemyRock extends Sprite {

    private final int damage;
    private final int speed;

    public EnemyRock(int x, int y) {
        this(x, y, 16, 16, 5, ENEMY_PROJECTILE_DAMAGE,
                new Color(115, 100, 90));
    }

    protected EnemyRock(int x, int y, int width, int height,
            int speed, int damage, Color color) {
        super(x, y, width, height, color);
        this.speed = speed;
        this.damage = damage;
    }

    @Override
    public void act() {
        x -= speed;
        if (isOutsideViewport()) {
            die();
        }
    }

    public int getDamage() {
        return damage;
    }
}
