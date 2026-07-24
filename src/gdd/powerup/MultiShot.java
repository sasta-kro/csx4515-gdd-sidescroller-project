package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;

public class MultiShot extends PowerUp {

    public MultiShot(int x, int y) {
        super(x, y, COLOR_MULTI, "Multi-shot");
    }

    @Override
    public void upgrade(Player player) {
        player.applyMultiShot();
        die();
    }
}
