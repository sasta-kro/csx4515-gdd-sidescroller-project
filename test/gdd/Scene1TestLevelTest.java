package gdd;

import static gdd.Global.*;
import gdd.powerup.PowerUp;
import gdd.sprite.Enemy;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class Scene1TestLevelTest {

    private static final int[] PREVIEW_TICKS = {
        0, 720, 1440, 2160, 2880, 3599
    };

    public static void main(String[] args) throws Exception {
        int[][] terrain = LevelLoader.loadTerrain(
                DEV_SCENE1_TERRAIN_PATH);
        Map<Integer, List<SpawnDetails>> schedule = LevelLoader.loadEvents(
                DEV_SCENE1_EVENTS_PATH);

        assertEquals(14, terrain.length, "terrain rows");
        assertEquals(159, terrain[0].length, "terrain columns");
        assertEquals(35, schedule.values().stream()
                .mapToInt(List::size).sum(), "scheduled events");
        assertEquals(2, schedule.get(480).size(),
                "same-tick pair");
        assertEquals(3, schedule.get(2040).size(),
                "same-tick formation");

        verifiesSpawnManagerUsesTheSchedule();
        renderTerrainPreview(terrain);
    }

    private static void verifiesSpawnManagerUsesTheSchedule() {
        Player player = new Player(new RunState());
        SpawnManager manager = new SpawnManager(player, 1,
                DEV_SCENE1_EVENTS_PATH);
        manager.setMode(SpawnMode.SCRIPTED);

        List<Enemy> enemies = new ArrayList<>();
        List<PowerUp> powerUps = new ArrayList<>();
        int mines = 0;

        for (int tick = 1; tick <= secondsToTicks(60); tick++) {
            manager.update(tick, enemies, powerUps);
            mines += manager.takeWorldEvents().size();
        }

        assertEquals(23, enemies.size(), "spawned enemies");
        assertEquals(5, powerUps.size(), "spawned powerups");
        assertEquals(7, mines, "spawned mines");
    }

    private static void renderTerrainPreview(int[][] terrain)
            throws Exception {
        int columns = 3;
        int rows = 2;
        BufferedImage contactSheet = new BufferedImage(
                BOARD_WIDTH * columns, BOARD_HEIGHT * rows,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetGraphics = contactSheet.createGraphics();
        TileMap tileMap = new TileMap(terrain);

        for (int index = 0; index < PREVIEW_TICKS.length; index++) {
            int x = index % columns * BOARD_WIDTH;
            int y = index / columns * BOARD_HEIGHT;
            Graphics2D viewport = (Graphics2D) sheetGraphics.create(
                    x, y, BOARD_WIDTH, BOARD_HEIGHT);
            viewport.setColor(new Color(4, 42, 70));
            viewport.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
            tileMap.draw(viewport, PREVIEW_TICKS[index]);
            viewport.setColor(new Color(0, 0, 0, 180));
            viewport.fillRect(8, 8, 190, 30);
            viewport.setColor(Color.WHITE);
            viewport.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            viewport.drawString("Tick " + PREVIEW_TICKS[index]
                    + " | " + PREVIEW_TICKS[index] / 60 + " sec",
                    16, 29);
            viewport.dispose();
        }

        sheetGraphics.dispose();
        ImageIO.write(contactSheet, "png", new File(
                "/tmp/ocean-invaders-scene1-test-contact-sheet.png"));
    }

    private static void assertEquals(int expected, int actual,
            String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected
                    + ", got " + actual);
        }
    }
}
