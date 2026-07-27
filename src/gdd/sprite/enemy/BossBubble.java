package gdd.sprite.enemy;

import gdd.sprite.BubbleSprite;
import java.awt.Color;

public class BossBubble extends EnemyProjectile {

    static final int WIDTH = 36;
    static final int HEIGHT = 36;
    private static final int POP_FRAME_TICKS = 3;

    public BossBubble(int x, int y, int damage, Color color) {
        super(x, y, WIDTH, HEIGHT, 6, damage,
                color);
        setHitboxScale(0.85, 0.85);
        setImage(BubbleSprite.tintedSheet(color));
        setAnimationFrames(BubbleSprite.flightClips());
    }

    @Override
    public void act() {
        if (isDying()) {
            if (isAnimationFinished()) {
                super.die();
            }
            return;
        }

        super.act();
    }

    @Override
    public void die() {
        if (isDying()) {
            return;
        }

        setDying(true);
        setAnimationFrames(BubbleSprite.popClips());
        setAnimationInterval(POP_FRAME_TICKS);
        setAnimationLooping(false);
    }
}
