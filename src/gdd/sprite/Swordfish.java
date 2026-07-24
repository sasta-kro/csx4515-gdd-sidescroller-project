package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

public class Swordfish extends Enemy {

    private enum State {
        CHARGING,
        DASHING
    }

    private State state = State.CHARGING;
    private int chargeTicks = secondsToTicks(0.75);
    private double dashX;
    private double dashY;

    public Swordfish(Player player, int x, int y) {
        super(player, x, y, 58, 24, 2,
                ENEMY_CONTACT_DAMAGE, 200, new Color(80, 165, 220));
    }

    @Override
    public void act() {
        if (state == State.CHARGING) {
            moveWithWorld();
            setPlaceholderColor((chargeTicks / 6) % 2 == 0
                    ? new Color(240, 220, 90)
                    : new Color(80, 165, 220));

            if (--chargeTicks <= 0) {
                lockDashDirection();
                state = State.DASHING;
            }
        } else {
            x += dashX;
            y += dashY;
        }

        if (state == State.DASHING
                && (getX() + getRenderWidth() < 0
                || getY() + getRenderHeight() < 0
                || getY() > BOARD_HEIGHT)) {
            die();
        }
    }

    private void lockDashDirection() {
        double targetX = player.getX() - x;
        double targetY = player.getY() - y;
        double length = Math.max(1.0, Math.hypot(targetX, targetY));
        double speed = 7.0;
        dashX = targetX / length * speed;
        dashY = targetY / length * speed;
    }
}
