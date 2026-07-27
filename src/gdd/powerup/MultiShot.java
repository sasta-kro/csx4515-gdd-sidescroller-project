package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class MultiShot extends PowerUp {

    private static final ImageIcon sprite =
            new ImageIcon("src/images/powerups/multi-shot.png");

    public MultiShot(int x, int y) {
        super(x, y, COLOR_MULTI, "Multi-shot");
        setImage(sprite.getImage());
    }

    @Override
    public void upgrade(Player player) {
        player.applyMultiShot();
        die();
    }
}
