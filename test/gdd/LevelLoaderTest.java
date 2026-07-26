package gdd;

import static gdd.Global.*;
import gdd.level.LevelLoader;
import gdd.level.TileMap;
import gdd.spawn.SpawnDetails;
import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LevelLoaderTest {

    public static void main(String[] args) throws Exception {
        loadsMultiCellTerrain();
        rejectsCoveredCellWithoutAnchor();
        loadsMultipleEventsOnOneTick();
        collidesWithScrollingTerrain();
    }

    private static void loadsMultiCellTerrain() throws Exception {
        Path file = Files.createTempFile("ocean-terrain", ".csv");
        Files.writeString(file, terrainCsv());

        int[][] terrain = LevelLoader.loadTerrain(file.toString());

        assertEquals(14, terrain.length, "terrain rows");
        assertEquals(20, terrain[0].length, "terrain columns");
        assertEquals(10, terrain[2][15], "large tile anchor");
        assertEquals(-1, terrain[3][16], "large tile covered cell");
    }

    private static void rejectsCoveredCellWithoutAnchor() throws Exception {
        Path file = Files.createTempFile("ocean-invalid-terrain", ".csv");
        Files.writeString(file, terrainCsv().replaceFirst("0,", "-1,"));

        try {
            LevelLoader.loadTerrain(file.toString());
            throw new AssertionError("unclaimed covered cell was accepted");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("has no tile anchor"),
                    "unclaimed covered cell error");
        }
    }

    private static void loadsMultipleEventsOnOneTick() throws Exception {
        Path file = Files.createTempFile("ocean-events", ".csv");
        Files.writeString(file, """
                # tick,type,x,y
                120,Jellyfish,756,180
                120,Turtle,756,430
                240,SnakeTop,420,0
                """);

        Map<Integer, List<SpawnDetails>> events
                = LevelLoader.loadEvents(file.toString());

        assertEquals(2, events.get(120).size(), "same-tick event count");
        assertEquals("SnakeTop", events.get(240).get(0).type,
                "snake event type");
    }

    private static void collidesWithScrollingTerrain() throws Exception {
        Path file = Files.createTempFile("ocean-terrain", ".csv");
        Files.writeString(file, terrainCsv());
        TileMap map = new TileMap(LevelLoader.loadTerrain(file.toString()));

        Rectangle initialPosition = new Rectangle(15 * TILE_SIZE,
                2 * TILE_SIZE, 20, 20);
        assertTrue(map.intersects(initialPosition, 0),
                "terrain collision at stage start");

        int stageTick = 100;
        Rectangle scrolledPosition = new Rectangle(
                15 * TILE_SIZE - stageTick * WORLD_SCROLL_SPEED,
                2 * TILE_SIZE, 20, 20);
        assertTrue(map.intersects(scrolledPosition, stageTick),
                "terrain collision after scrolling");
    }

    private static String terrainCsv() {
        List<String> rows = new ArrayList<>();
        for (int row = 0; row < 14; row++) {
            int[] values = new int[20];
            if (row == 2) {
                values[15] = 10;
                values[16] = -1;
            } else if (row == 3) {
                values[15] = -1;
                values[16] = -1;
            }

            List<String> cells = new ArrayList<>();
            for (int value : values) {
                cells.add(Integer.toString(value));
            }
            rows.add(String.join(",", cells));
        }
        return String.join("\n", rows) + "\n";
    }

    private static void assertEquals(Object expected, Object actual,
            String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected
                    + ", got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
