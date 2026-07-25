package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public abstract class Sprite {

    protected boolean visible = true;
    protected boolean dying;
    protected Image image;
    protected double x;
    protected double y;
    protected double dx;
    protected double dy;

    private int width;
    private int height;
    private double renderScale = RENDER_SCALE;
    private double hitboxScale = HITBOX_SCALE;
    private Color placeholderColor;
    private final List<Image> animationFrames = new ArrayList<>();
    private int currentAnimationFrame;
    private int ticksSinceLastAnimationFrameChange;
    /// number of ticks each image remains visible. equivalent to animating on 2s, 3s, 4s, etc.
    private int animationInterval = 1;

    protected Sprite(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        placeholderColor = color;
    }

    public abstract void act();

    public void draw(Graphics g) {
        int renderWidth = getRenderWidth();
        int renderHeight = getRenderHeight();

        Image activeImage = getActiveImage();
        if (activeImage != null) {
            g.drawImage(activeImage, getX(), getY(), renderWidth, renderHeight, null);
            return;
        }

        g.setColor(placeholderColor);
        g.fillRect(getX(), getY(), renderWidth, renderHeight);
        g.setColor(new Color(255, 255, 255, 130));
        g.drawRect(getX(), getY(), renderWidth, renderHeight);

        if (renderWidth >= 24 && renderHeight >= 20) {
            String label = getClass().getSimpleName().substring(0, 1);
            int textX = getX() + (renderWidth - g.getFontMetrics()
                    .stringWidth(label)) / 2;
            int textY = getY() + (renderHeight + g.getFontMetrics()
                    .getAscent()) / 2 - 2;
            g.drawString(label, textX, textY);
        }
    }

    public Rectangle getBounds() {
        int renderWidth = getRenderWidth();
        int renderHeight = getRenderHeight();
        int hitboxWidth = Math.max(1, (int) Math.round(renderWidth * hitboxScale));
        int hitboxHeight = Math.max(1, (int) Math.round(renderHeight * hitboxScale));
        int offsetX = (renderWidth - hitboxWidth) / 2;
        int offsetY = (renderHeight - hitboxHeight) / 2;
        return new Rectangle(getX() + offsetX, getY() + offsetY,
                hitboxWidth, hitboxHeight);
    }

    public boolean collidesWith(Sprite other) {
        return other != null
                && isVisible()
                && other.isVisible()
                && getBounds().intersects(other.getBounds());
    }

    public boolean isOutsideViewport() {
        return getX() + getRenderWidth() < 0
                || getX() > BOARD_WIDTH
                || getY() + getRenderHeight() < 0
                || getY() > BOARD_HEIGHT;
    }

    public void die() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isDying() {
        return dying;
    }

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    public int getX() {
        return (int) Math.round(x);
    }

    public int getY() {
        return (int) Math.round(y);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRenderWidth() {
        return Math.max(1, (int) Math.round(width * renderScale));
    }

    public int getRenderHeight() {
        return Math.max(1, (int) Math.round(height * renderScale));
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setRenderScale(double renderScale) {
        this.renderScale = Math.max(0.1, renderScale);
    }

    public void setHitboxScale(double hitboxScale) {
        this.hitboxScale = Math.max(0.1, hitboxScale);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setAnimationFrames(List<Image> frames, int intervalTicks) {
        animationFrames.clear();
        animationFrames.addAll(frames);
        currentAnimationFrame = 0;
        ticksSinceLastAnimationFrameChange = 0;
        animationInterval = Math.max(1, intervalTicks);
    }

    public void advanceAnimation() {
        // check for no animation (static image)
        if (animationFrames.size() < 2) {
            return;
        }

        ticksSinceLastAnimationFrameChange++;

        if (ticksSinceLastAnimationFrameChange >= animationInterval) {
            // reset frame timer back down to 0
            ticksSinceLastAnimationFrameChange = 0;

            // move on to next animation frame
            currentAnimationFrame++;

            // if out of bounds, circle back to frame 0
            if (currentAnimationFrame >= animationFrames.size()) {
                currentAnimationFrame = 0;
            }
        }
    }

    private Image getActiveImage() {
        if (!animationFrames.isEmpty()) {
            return animationFrames.get(currentAnimationFrame);
        }
        return image;
    }

    public Color getPlaceholderColor() {
        return placeholderColor;
    }

    public void setPlaceholderColor(Color color) {
        placeholderColor = color;
    }
}
