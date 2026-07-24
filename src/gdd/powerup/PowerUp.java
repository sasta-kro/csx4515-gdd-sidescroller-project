package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import gdd.sprite.Sprite;
import java.awt.Color;

public abstract class PowerUp extends Sprite {

    private final double centerY;
    private double wave;
    private final String displayName;

    protected PowerUp(int x, int y, Color color, String displayName) {
        super(x, y, POWERUP_WIDTH, POWERUP_HEIGHT, color);
        centerY = y;
        wave = Math.random() * Math.PI * 2;
        this.displayName = displayName;
    }

    @Override
    public void act() {
        x -= WORLD_SCROLL_SPEED;
        wave += POWERUP_WAVE_SPEED;
        y = centerY + Math.sin(wave) * POWERUP_WAVE_AMPLITUDE;
        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    public abstract void upgrade(Player player);

    public String getDisplayName() {
        return displayName;
    }
}
