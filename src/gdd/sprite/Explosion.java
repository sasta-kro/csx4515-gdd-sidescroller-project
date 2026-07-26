package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;

public class Explosion extends Sprite {

    private static final String EXPLOSION_SHEET_PATH = "src/images/mines/explosion.png";
    private static final ImageIcon explosionSheet = new ImageIcon(EXPLOSION_SHEET_PATH);

    private static final List<Rectangle> explosionAnimationClips = List.of(
            new Rectangle(0, 0, 66, 82),
            new Rectangle(66, 0, 66, 82),
            new Rectangle(66*2, 0, 66, 82),
            new Rectangle(66*3, 0, 66, 82),
            new Rectangle(66*4, 0, 66, 82),
            new Rectangle(66*5, 0, 66, 82),
            new Rectangle(66*6, 0, 66, 82),
            new Rectangle(66*7, 0, 66, 82),
            new Rectangle(66*8, 0, 66, 82),
            new Rectangle(66*9, 0, 66, 82)
    );

    private final int radius;

    public Explosion(int centerX, int centerY) {
        this(centerX, centerY, 28);
    }

    public Explosion(int centerX, int centerY, int radius) {
        super(centerX - radius, centerY - radius, radius * 2, radius * 2, new Color(255, 150, 50, 170));
        this.radius = radius;
        setImage(explosionSheet.getImage());
        setAnimationFrames(explosionAnimationClips);
        setAnimationInterval(5);
        setAnimationLooping(false);
    }

    @Override
    public void act() {
        if (isAnimationFinished()) {
            die();
        }
    }

    public int getRadius() {
        return radius;
    }
}
