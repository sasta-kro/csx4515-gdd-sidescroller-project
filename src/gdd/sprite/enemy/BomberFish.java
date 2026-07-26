package gdd.sprite.enemy;

import gdd.sprite.Player;

import static gdd.Global.*;
import java.awt.Color;
import java.util.Random;

public class BomberFish extends Enemy {

    private int explosionTicks;

    public BomberFish(Player player, int x, int y, Random random) {
        super(player, x, y, 34, 24, 1,
                ENEMY_CONTACT_DAMAGE, 0, new Color(235, 105, 75));
        explosionTicks = secondsToTicks(2 + random.nextDouble() * 2);
    }

    @Override
    public void act() {
        double targetX = player.getX() - x;
        double targetY = player.getY() - y;
        double length = Math.max(1.0, Math.hypot(targetX, targetY));
        double speed = 2.6;
        x += targetX / length * speed;
        y += targetY / length * speed;
        explosionTicks--;
    }

    public boolean shouldExplode() {
        return explosionTicks <= 0 || collidesWith(player);
    }
}
