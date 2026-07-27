package gdd.scene;

import gdd.Game;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class TitleSceneTest {

    public static void main(String[] args) throws Exception {
        rendersParallaxOnConsecutiveTicks();
        rendersAnAnimatedTitleScene();
        exposesDirectBossStart();
    }

    private static void rendersParallaxOnConsecutiveTicks() {
        TitleScene scene = new TitleScene(null);
        scene.setSize(716, 700);
        BufferedImage firstFrame = render(scene);
        scene.updateTitle();
        BufferedImage secondFrame = render(scene);

        assertTrue(countChangedPixels(firstFrame, secondFrame) > 1_000,
                "fractional parallax should render every tick");
    }

    private static void rendersAnAnimatedTitleScene() {
        TitleScene scene = new TitleScene(null);
        scene.setSize(716, 700);
        BufferedImage firstFrame = render(scene);

        for (int tick = 0; tick < 90; tick++) {
            scene.updateTitle();
        }

        BufferedImage laterFrame = render(scene);
        int changedPixels = countChangedPixels(firstFrame, laterFrame);

        assertTrue(changedPixels > 10_000,
                "title scene should visibly animate");
    }

    private static int countChangedPixels(BufferedImage firstFrame,
            BufferedImage secondFrame) {
        int changedPixels = 0;
        for (int y = 0; y < firstFrame.getHeight(); y++) {
            for (int x = 0; x < firstFrame.getWidth(); x++) {
                if (firstFrame.getRGB(x, y) != secondFrame.getRGB(x, y)) {
                    changedPixels++;
                }
            }
        }
        return changedPixels;
    }

    private static void exposesDirectBossStart() throws Exception {
        assertTrue(Game.class.getMethod("startFromBoss") != null,
                "game exposes direct boss start");
    }

    private static BufferedImage render(TitleScene scene) {
        BufferedImage image = new BufferedImage(
                716, 700, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        scene.paint(graphics);
        graphics.dispose();
        return image;
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
