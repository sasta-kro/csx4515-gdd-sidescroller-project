package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;

public class Heal extends PowerUp {

    public Heal(int x, int y) {
        super(x, y, COLOR_HEAL, "Heal");
    }

    @Override
    public void upgrade(Player player) {
        player.heal();
        die();
    }
}
