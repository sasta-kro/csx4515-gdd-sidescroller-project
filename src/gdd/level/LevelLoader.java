package gdd.level;

import static gdd.Global.*;
import gdd.spawn.SpawnDetails;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LevelLoader {

    public static final int TERRAIN_ROWS = BOARD_HEIGHT / TILE_SIZE;

    private LevelLoader() {
    }

    public static int[][] loadTerrain(String filePath) {
        List<String> lines = dataLines(filePath);
        if (lines.size() != TERRAIN_ROWS) {
            throw new IllegalArgumentException(
                    "Terrain must contain " + TERRAIN_ROWS + " rows: " + filePath);
        }

        int columns = lines.get(0).split(",", -1).length;
        int[][] terrain = new int[TERRAIN_ROWS][columns];

        for (int row = 0; row < TERRAIN_ROWS; row++) {
            String[] values = lines.get(row).split(",", -1);
            if (values.length != columns) {
                throw new IllegalArgumentException(
                        "Terrain row " + row + " has a different width");
            }

            for (int column = 0; column < columns; column++) {
                int tile = Integer.parseInt(values[column]);
                if (tile < -1 || tile > 0 && !TileRegistry.contains(tile)) {
                    throw new IllegalArgumentException(
                            "Unknown terrain value " + tile
                            + " at row " + row + ", column " + column);
                }
                terrain[row][column] = tile;
            }
        }

        validateFootprints(terrain);
        return terrain;
    }

    public static Map<Integer, List<SpawnDetails>> loadEvents(String filePath) {
        Map<Integer, List<SpawnDetails>> events = new HashMap<>();

        for (String line : dataLines(filePath)) {
            String[] values = line.split(",", -1);
            if (values.length != 4) {
                throw new IllegalArgumentException("Invalid event row: " + line);
            }

            int tick = Integer.parseInt(values[0]);
            String type = values[1];
            int x = Integer.parseInt(values[2]);
            int y = Integer.parseInt(values[3]);
            events.computeIfAbsent(tick, ignored -> new ArrayList<>())
                    .add(new SpawnDetails(type, x, y));
        }

        return events;
    }

    private static List<String> dataLines(String filePath) {
        try {
            List<String> result = new ArrayList<>();
            for (String rawLine : Files.readAllLines(Path.of(filePath))) {
                String line = rawLine.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    result.add(line);
                }
            }
            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void validateFootprints(int[][] terrain) {
        int rows = terrain.length;
        int columns = terrain[0].length;
        boolean[][] claimedCells = new boolean[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int tile = terrain[row][column];
                if (tile <= 0) {
                    continue;
                }

                TileDefinition definition = TileRegistry.get(tile);
                if (row + definition.cellsHigh > rows
                        || column + definition.cellsWide > columns) {
                    throw new IllegalArgumentException(
                            "Tile " + tile + " exceeds terrain bounds at row "
                            + row + ", column " + column);
                }

                for (int coveredRow = row;
                        coveredRow < row + definition.cellsHigh; coveredRow++) {
                    for (int coveredColumn = column;
                            coveredColumn < column + definition.cellsWide;
                            coveredColumn++) {
                        if (claimedCells[coveredRow][coveredColumn]) {
                            throw new IllegalArgumentException(
                                    "Tile " + tile + " overlaps another tile at row "
                                    + row + ", column " + column);
                        }
                        if (coveredRow == row && coveredColumn == column) {
                            claimedCells[coveredRow][coveredColumn] = true;
                            continue;
                        }
                        if (terrain[coveredRow][coveredColumn] != -1) {
                            throw new IllegalArgumentException(
                                    "Tile " + tile + " has an incomplete footprint at row "
                                    + row + ", column " + column);
                        }
                        claimedCells[coveredRow][coveredColumn] = true;
                    }
                }
            }
        }

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (terrain[row][column] == -1
                        && !claimedCells[row][column]) {
                    throw new IllegalArgumentException(
                            "Covered cell has no tile anchor at row "
                            + row + ", column " + column);
                }
            }
        }
    }
}
