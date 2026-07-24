package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Graphics2D;

public class Explosion extends Sprite {

    private int remainingTicks;
    private final int radius;

    public Explosion(int centerX, int centerY) {
        this(centerX, centerY, 28);
    }

    public Explosion(int centerX, int centerY, int radius) {
        super(centerX - radius, centerY - radius,
                radius * 2, radius * 2, new Color(255, 150, 50, 170));
        this.radius = radius;
        remainingTicks = secondsToTicks(0.35);
    }

    @Override
    public void act() {
        remainingTicks--;
        if (remainingTicks <= 0) {
            die();
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 170, 55, 150));
        g.fillOval(getX(), getY(), getRenderWidth(), getRenderHeight());
        g.setColor(Color.WHITE);
        g.drawOval(getX(), getY(), getRenderWidth(), getRenderHeight());
    }

    public int getRadius() {
        return radius;
    }
}
