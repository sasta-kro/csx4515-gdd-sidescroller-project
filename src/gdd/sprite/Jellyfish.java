package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

public class Jellyfish extends Enemy {

    private final double centerY;
    private double wave;

    public Jellyfish(Player player, int x, int y) {
        super(player, x, y, 34, 42, 1,
                ENEMY_CONTACT_DAMAGE, 100, new Color(130, 210, 235));
        centerY = y;
        wave = Math.random() * Math.PI * 2;
    }

    @Override
    public void act() {
        moveWithWorld();
        wave += 0.055;
        y = centerY + Math.sin(wave) * 34;
        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }
}
