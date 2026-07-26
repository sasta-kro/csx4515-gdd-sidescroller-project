package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import javax.swing.ImageIcon;

public class EnemyProjectile extends Sprite {

    private static final String ROCK_IMAGE_PATH
            = "src/images/enemies/octopus/Rock.png";
    private static final ImageIcon rockImage
            = new ImageIcon(ROCK_IMAGE_PATH);

    private final int damage;
    private final int speed;

    public EnemyProjectile(int x, int y) {
        this(x, y, 16, 16, 5, ENEMY_PROJECTILE_DAMAGE,
                new Color(115, 100, 90));
        setImage(rockImage.getImage());
    }

    protected EnemyProjectile(int x, int y, int width, int height,
                              int speed, int damage, Color color) {
        super(x, y, width, height, color);
        this.speed = speed;
        this.damage = damage;
    }

    @Override
    public void act() {
        x -= speed;
        if (isOutsideViewport()) {
            die();
        }
    }

    public int getDamage() {
        return damage;
    }
}
