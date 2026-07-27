package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class SpeedUp extends PowerUp {

    private static final ImageIcon sprite =
            new ImageIcon("src/images/powerups/speed.png");

    public SpeedUp(int x, int y) {
        super(x, y, COLOR_SPEED, "Speed Up");
        setImage(sprite.getImage());
    }

    @Override
    public void upgrade(Player player) {
        player.applySpeedUp();
        die();
    }
}
