package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class Heal extends PowerUp {

    private static final ImageIcon sprite =
            new ImageIcon("src/images/powerups/heal.png");

    public Heal(int x, int y) {
        super(x, y, COLOR_HEAL, "Heal");
        setImage(sprite.getImage());
    }

    @Override
    public void upgrade(Player player) {
        player.heal();
        die();
    }
}
