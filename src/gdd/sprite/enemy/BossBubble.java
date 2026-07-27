package gdd.sprite.enemy;

import java.awt.Color;

public class BossBubble extends EnemyProjectile {

    static final int WIDTH = 18;
    static final int HEIGHT = 14;

    public BossBubble(int x, int y, int damage) {
        super(x, y, WIDTH, HEIGHT, 6, damage,
                new Color(105, 205, 245));
    }
}
