package gdd;

import static gdd.Global.*;
import gdd.level.LevelLoader;
import gdd.level.TileMap;
import gdd.spawn.SpawnDetails;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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
        ignoresTransparentPartsOfTerrainTiles();
        ignoresTransparentGapsInHangingIsland();
        resolvesScrollingTerrainOverlap();
        keepsTerrainCorrectionInsidePlayerMovementArea();
        drawsTerrainHitboxOutline();
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

        Rectangle initialPosition = new Rectangle(15 * TILE_SIZE + 10,
                2 * TILE_SIZE + 70, 20, 20);
        assertTrue(map.intersects(initialPosition, 0),
                "terrain collision at stage start");

        int stageTick = 100;
        Rectangle scrolledPosition = new Rectangle(
                initialPosition.x - stageTick * WORLD_SCROLL_SPEED,
                initialPosition.y, 20, 20);
        assertTrue(map.intersects(scrolledPosition, stageTick),
                "terrain collision after scrolling");
    }

    private static void ignoresTransparentPartsOfTerrainTiles() {
        int[][] terrain = new int[14][20];
        terrain[2][15] = 10;
        TileMap map = new TileMap(terrain);

        Rectangle transparentSlopeCorner = new Rectangle(
                15 * TILE_SIZE, 2 * TILE_SIZE, 10, 10);
        Rectangle visibleSlopeRock = new Rectangle(
                15 * TILE_SIZE + 10, 2 * TILE_SIZE + 70, 10, 10);

        assertTrue(!map.intersects(transparentSlopeCorner, 0),
                "transparent slope corner has no collision");
        assertTrue(map.intersects(visibleSlopeRock, 0),
                "visible slope section has collision");
    }

    private static void ignoresTransparentGapsInHangingIsland() {
        int[][] terrain = new int[14][20];
        terrain[2][5] = 20;
        TileMap map = new TileMap(terrain);
        int tileX = 5 * TILE_SIZE;
        int tileY = 2 * TILE_SIZE;

        Rectangle transparentLowerSide = new Rectangle(
                tileX + 10, tileY + 150, 20, 20);
        Rectangle visibleChain = new Rectangle(
                tileX + 90, tileY + 150, 20, 20);

        assertTrue(!map.intersects(transparentLowerSide, 0),
                "hanging-island transparent gap has no collision");
        assertTrue(map.intersects(visibleChain, 0),
                "hanging-island chain section has collision");
    }

    private static void resolvesScrollingTerrainOverlap() {
        int[][] terrain = new int[14][20];
        terrain[2][15] = 10;
        TileMap map = new TileMap(terrain);

        int stageTick = 100;
        Rectangle playerBounds = new Rectangle(545, 170, 20, 20);
        Rectangle allowedBounds = new Rectangle(
                0, 0, BOARD_WIDTH, BOARD_HEIGHT - 32);
        Point correction = map.getCollisionCorrection(
                playerBounds, allowedBounds, stageTick);

        assertEquals(new Point(-15, 0), correction,
                "scrolling terrain correction");
        playerBounds.translate(correction.x, correction.y);
        assertTrue(!map.intersects(playerBounds, stageTick),
                "scrolling terrain overlap resolved");
    }

    private static void keepsTerrainCorrectionInsidePlayerMovementArea() {
        int[][] terrain = new int[14][20];
        terrain[0][5] = 10;
        TileMap map = new TileMap(terrain);

        Rectangle playerBounds = new Rectangle(260, 70, 20, 20);
        Rectangle allowedBounds = new Rectangle(
                0, 5, BOARD_WIDTH, BOARD_HEIGHT - 37);
        Point correction = map.getCollisionCorrection(
                playerBounds, allowedBounds, 0);

        playerBounds.translate(correction.x, correction.y);
        assertTrue(allowedBounds.contains(playerBounds),
                "terrain correction remains in player movement area");
        assertTrue(!map.intersects(playerBounds, 0),
                "terrain overlap near viewport edge resolved");
    }

    private static void drawsTerrainHitboxOutline() {
        if (!DEV_SHOW_ENTITY_HITBOXES) {
            return;
        }

        int[][] terrain = new int[14][20];
        terrain[2][1] = 10;
        terrain[2][2] = -1;
        TileMap map = new TileMap(terrain);
        BufferedImage image = new BufferedImage(
                BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();

        map.draw(graphics, 0);
        graphics.dispose();

        assertEquals(Color.RED.getRGB(), image.getRGB(
                TILE_SIZE, 2 * TILE_SIZE + 70),
                "terrain hitbox outline color");
        assertTrue(image.getRGB(TILE_SIZE, 2 * TILE_SIZE)
                != Color.RED.getRGB(),
                "transparent terrain has no hitbox outline");
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
