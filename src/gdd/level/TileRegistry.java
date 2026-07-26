package gdd.level;

import java.awt.Rectangle;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TileRegistry {

    private static final Map<Integer, TileDefinition> TILES = new LinkedHashMap<>();

    static {
        add(1, "Ceiling A", 16, 16, 80, 63, 2, 1);
        add(2, "Ceiling B", 112, 16, 80, 64, 2, 1);
        add(3, "Wall Left A", 209, 16, 47, 80, 1, 2);
        add(4, "Wall Right A", 272, 16, 47, 80, 1, 2);
        add(5, "Wall Left B", 323, 16, 61, 80, 1, 2);
        add(6, "Wall Right B", 400, 16, 61, 80, 1, 2);
        add(7, "Wide Ceiling", 16, 108, 176, 68, 4, 1);
        add(8, "Ceiling Corner Right", 320, 117, 80, 59, 2, 1);
        add(9, "Ceiling Corner Left", 208, 120, 80, 56, 2, 1);
        add(10, "Large Slope Left", 16, 188, 112, 116, 2, 2);
        add(11, "Large Slope Right", 144, 188, 112, 116, 2, 2);
        add(12, "Inner Corner Top Left", 272, 208, 64, 64, 1, 1);
        add(13, "Inner Corner Top Right", 352, 208, 64, 64, 1, 1);
        add(14, "Inner Corner Bottom Left", 272, 288, 64, 64, 1, 1);
        add(15, "Inner Corner Bottom Right", 352, 288, 64, 64, 1, 1);
        add(16, "Large Ceiling Left", 16, 320, 112, 116, 2, 2);
        add(17, "Large Ceiling Right", 144, 320, 112, 116, 2, 2);
        add(18, "Small Rock Top Left", 332, 392, 52, 56, 1, 1);
        add(19, "Small Rock Top Right", 400, 392, 52, 56, 1, 1);
        add(20, "Hanging Island", 13, 452, 181, 188, 4, 4);
        add(21, "Small Rock Bottom Left", 332, 464, 52, 56, 1, 1);
        add(22, "Small Rock Bottom Right", 400, 464, 52, 56, 1, 1);
    }

    private TileRegistry() {
    }

    private static void add(int id, String name, int sourceX, int sourceY,
            int sourceWidth, int sourceHeight, int cellsWide, int cellsHigh) {
        TILES.put(id, new TileDefinition(id, name,
                new Rectangle(sourceX, sourceY, sourceWidth, sourceHeight),
                cellsWide, cellsHigh));
    }

    public static TileDefinition get(int id) {
        TileDefinition definition = TILES.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown tile ID: " + id);
        }
        return definition;
    }

    public static boolean contains(int id) {
        return TILES.containsKey(id);
    }

    public static Map<Integer, TileDefinition> all() {
        return Map.copyOf(TILES);
    }
}
