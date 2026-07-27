package gdd.sprite;

import java.awt.Color;

public class Bubble extends Sprite {

    private static final int POP_FRAME_TICKS = 3;

    private final int damage;
    private final int speed;
    private final boolean playerOwned;

    public Bubble(int x, int y, int width, int height, int damage,
            int speed, Color color, boolean playerOwned) {
        super(x, y, width, height, color);
        this.damage = damage;
        this.speed = speed;
        this.playerOwned = playerOwned;
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

        x += playerOwned ? speed : -speed;
        if (isOutsideViewport()) {
            die();
        }
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

    public int getDamage() {
        return damage;
    }

    public boolean isPlayerOwned() {
        return playerOwned;
    }
}
