package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

public class Mine extends Sprite {

    private boolean triggered;

    public Mine(int x, int y) {
        super(x, y, MINE_SIZE, MINE_SIZE, new Color(90, 95, 105));
    }

    @Override
    public void act() {
        x -= WORLD_SCROLL_SPEED;
        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    public boolean trigger() {
        if (triggered || !isVisible()) {
            return false;
        }
        triggered = true;
        die();
        return true;
    }

    public int getCenterX() {
        return getX() + getRenderWidth() / 2;
    }

    public int getCenterY() {
        return getY() + getRenderHeight() / 2;
    }
}
