package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.util.Random;

public class Octopus extends Enemy {

    private final Random random = new Random();
    private final double centerY;
    private double wave;
    private int rockCooldown;
    private boolean rockReady;

    public Octopus(Player player, int x, int y) {
        super(player, x, y, 46, 46, 2,
                ENEMY_CONTACT_DAMAGE, 200, new Color(190, 95, 190));
        centerY = y;
        rockCooldown = nextRockCooldown();
    }

    @Override
    public void act() {
        moveWithWorld();
        wave += 0.045;
        y = centerY + Math.sin(wave) * 44;

        if (--rockCooldown <= 0) {
            rockReady = true;
            rockCooldown = nextRockCooldown();
        }

        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    public EnemyRock takeRock() {
        if (!rockReady || !isVisible()) {
            return null;
        }
        rockReady = false;
        return new EnemyRock(getX() - 14, getY() + getRenderHeight() / 2);
    }

    private int nextRockCooldown() {
        return secondsToTicks(2 + random.nextDouble() * 2);
    }
}
