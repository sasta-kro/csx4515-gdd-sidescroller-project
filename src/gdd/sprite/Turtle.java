package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

public class Turtle extends Enemy {

    private static final double TRACK_SPEED = 1.25;

    public Turtle(Player player, int x, int y) {
        super(player, x, y, 52, 34, 2,
                ENEMY_CONTACT_DAMAGE, 250, new Color(80, 175, 105));
    }

    @Override
    public void act() {
        double targetX = player.getX() - x;
        double targetY = player.getY() - y;
        double length = Math.max(1.0, Math.hypot(targetX, targetY));

        x += targetX / length * TRACK_SPEED;
        y += targetY / length * TRACK_SPEED;
        moveWithWorld();

        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }
}
