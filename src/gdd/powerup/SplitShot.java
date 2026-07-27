package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class SplitShot extends PowerUp {

    private static final ImageIcon sprite =
            new ImageIcon("src/images/powerups/split-shot.png");

    public SplitShot(int x, int y) {
        super(x, y, COLOR_SPLIT, "Split-shot");
        setImage(sprite.getImage());
    }

    @Override
    public void upgrade(Player player) {
        player.applySplitShot();
        die();
    }
}
