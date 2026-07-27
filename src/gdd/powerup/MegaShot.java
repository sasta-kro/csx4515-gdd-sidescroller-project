package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class MegaShot extends PowerUp {

    private static final ImageIcon sprite =
            new ImageIcon("src/images/powerups/mega-shot.png");

    public MegaShot(int x, int y) {
        super(x, y, COLOR_MEGA, "Mega-shot");
        setImage(sprite.getImage());
    }

    @Override
    public void upgrade(Player player) {
        player.applyMegaShot();
        die();
    }
}
