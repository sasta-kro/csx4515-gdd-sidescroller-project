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
            new Rectangle(0, 22, 60, 60),
            new Rectangle(60, 22, 60, 60),
            new Rectangle(60*2, 22, 60, 60),
            new Rectangle(60*3, 22, 60, 60),
            new Rectangle(60*4, 22, 60, 60),
            new Rectangle(60*5, 22, 60, 60)
//            new Rectangle(60*6, 22, 60, 60),
//            new Rectangle(60*7, 22, 60, 60),
//            new Rectangle(60*8, 22, 60, 60),
//            new Rectangle(60*9, 22, 60, 60),
//            new Rectangle(60*10, 22, 60, 60)
    );

    private final int radius;

    public Explosion(int centerX, int centerY) {
        this(centerX, centerY, 30);
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

    public boolean reaches(Sprite sprite) {
        double centerX = getX() + radius;
        double centerY = getY() + radius;
        double spriteCenterX = sprite.getX() + sprite.getRenderWidth() / 2.0;
        double spriteCenterY = sprite.getY() + sprite.getRenderHeight() / 2.0;
        return Math.hypot(spriteCenterX - centerX,
                spriteCenterY - centerY) <= radius;
    }
}
