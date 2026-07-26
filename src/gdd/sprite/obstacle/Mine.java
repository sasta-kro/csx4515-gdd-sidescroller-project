package gdd.sprite.obstacle;

import gdd.sprite.Sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import javax.swing.ImageIcon;

public class Mine extends Sprite {

    private static final String MINE_IMAGE_PATH = "src/images/mines/mine.png";
    private static final int TINT_FRAME_COUNT = 5;
    private static final int TINT_FRAME_TICKS = 7;
    private static final double MAX_WHITE_TINT = 0.08;
    private static final BufferedImage[] MINE_TINT_FRAMES = createTintFrames();

    private boolean triggered;
    private int tintFrame;
    private int tintFrameTicks;
    private int tintFrameDirection = 1;

    public Mine(int x, int y) {
        super(x, y, MINE_SIZE, MINE_SIZE, new Color(90, 95, 105));
        setImage(MINE_TINT_FRAMES[0]);
        setHitboxScale(0.8, 0.8);
    }

    @Override
    public void act() {
        updatePulseTint();

        x -= WORLD_SCROLL_SPEED;

        if (getX() + getRenderWidth() < 0) {
            die();
        }
    }

    private void updatePulseTint() {
        tintFrameTicks++;

        if (tintFrameTicks < TINT_FRAME_TICKS) {
            return;
        }

        tintFrameTicks = 0;
        tintFrame += tintFrameDirection;

        if (tintFrame == MINE_TINT_FRAMES.length - 1) {
            tintFrameDirection = -1;

        } else if (tintFrame == 0) {
            tintFrameDirection = 1;
        }

        setImage(MINE_TINT_FRAMES[tintFrame]);
    }

    // for the pulsing tint
    private static BufferedImage[] createTintFrames() {
        ImageIcon mineImage = new ImageIcon(MINE_IMAGE_PATH);
        BufferedImage baseImage = new BufferedImage(mineImage.getIconWidth(), mineImage.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = baseImage.createGraphics();
        graphics.drawImage(mineImage.getImage(), 0, 0, null);
        graphics.dispose();

        BufferedImage[] frames = new BufferedImage[TINT_FRAME_COUNT];
        frames[0] = baseImage;

        for (int index = 1; index < frames.length; index++) {
            float tintAmount = (float) (MAX_WHITE_TINT * index / (frames.length - 1));
            float colorScale = 1.0f - tintAmount;

            RescaleOp whiteTint = new RescaleOp(
                    new float[] {colorScale, colorScale, colorScale, 1.0f},
                    new float[] {255.0f * tintAmount, 255.0f * tintAmount, 255.0f * tintAmount, 0.0f},
                    null
            );

            frames[index] = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            whiteTint.filter(baseImage, frames[index]);
        }

        return frames;
    }

    public boolean trigger() {
        if (triggered || !isVisible()) {
            return false;
        }
        triggered = true;
        die();
        return true;
    }

    public int getCenterX() {
        return getX() + getRenderWidth() / 2;
    }

    public int getCenterY() {
        return getY() + getRenderHeight() / 2;
    }
}
