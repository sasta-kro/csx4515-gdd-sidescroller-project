package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

public class Snake extends Enemy {

    private final int verticalDirection;

    public Snake(Player player, int x, boolean fromTop) {
        super(player, x, fromTop ? -70 : BOARD_HEIGHT,
                28, 70, 2, ENEMY_CONTACT_DAMAGE, 150,
                new Color(100, 205, 125));
        verticalDirection = fromTop ? 1 : -1;
    }

    @Override
    public void act() {
        moveWithWorld();
        y += verticalDirection * 3;
        if (isOutsideViewport()) {
            die();
        }
    }
}
