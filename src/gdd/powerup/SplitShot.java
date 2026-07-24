package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;

public class SplitShot extends PowerUp {

    public SplitShot(int x, int y) {
        super(x, y, COLOR_SPLIT, "Split-shot");
    }

    @Override
    public void upgrade(Player player) {
        player.applySplitShot();
        die();
    }
}
