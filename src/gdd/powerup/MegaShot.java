package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;

public class MegaShot extends PowerUp {

    public MegaShot(int x, int y) {
        super(x, y, COLOR_MEGA, "Mega-shot");
    }

    @Override
    public void upgrade(Player player) {
        player.applyMegaShot();
        die();
    }
}
