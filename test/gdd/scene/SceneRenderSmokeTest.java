package gdd.scene;

import static gdd.Global.*;
import gdd.RunState;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class SceneRenderSmokeTest {

    public static void main(String[] args) throws Exception {
        renderPanel(new TitleScene(null), "title");

        Scene1 scene1 = new Scene1(null, new RunState());
        scene1.resetStage();
        advance(scene1, 180);
        renderPanel(scene1, "stage1");

        Scene2 scene2 = new Scene2(null, new RunState());
        scene2.resetStage();
        advance(scene2, 180);
        renderPanel(scene2, "stage2");

        BossScene bossScene = new BossScene(null, new RunState());
        bossScene.resetStage();
        advance(bossScene, 180);
        renderPanel(bossScene, "boss");
    }

    private static void advance(Scene1 scene, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            scene.updateGame();
        }
    }

    private static void renderPanel(JPanel panel, String name) throws Exception {
        panel.setSize(BOARD_WIDTH, BOARD_HEIGHT);
        BufferedImage image = new BufferedImage(
                BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        panel.paint(image.getGraphics());

        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < BOARD_HEIGHT; y += 10) {
            for (int x = 0; x < BOARD_WIDTH; x += 10) {
                colors.add(image.getRGB(x, y));
            }
        }

        if (colors.size() < 8) {
            throw new AssertionError(name + " rendered too few colors: "
                    + colors.size());
        }

        ImageIO.write(image, "png",
                new File("/tmp/ocean-invaders-" + name + ".png"));
    }
}
