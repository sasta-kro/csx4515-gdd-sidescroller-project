package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

public class Coral extends Sprite {

    private int health = 1;
    public int contactDamage = CORAL_CONTACT_DAMAGE;

    public Coral(int x, int y) {
        super(x, y, CORAL_WIDTH, CORAL_HEIGHT, new Color(235, 95, 115));
    }

    @Override
    public void act() {
        x -= WORLD_SCROLL_SPEED;
        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    public void damage() {
        if (--health <= 0) {
            die();
        }
    }
}
