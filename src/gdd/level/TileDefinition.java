package gdd.level;

import java.awt.Rectangle;

public class TileDefinition {

    public final int id;
    public final String name;
    public final Rectangle source;
    public final String imagePath;
    public final int cellsWide;
    public final int cellsHigh;

    public TileDefinition(int id, String name, Rectangle source,
            int cellsWide, int cellsHigh) {
        this(id, name, source, null, cellsWide, cellsHigh);
    }

    public TileDefinition(int id, String name, String imagePath,
            int cellsWide, int cellsHigh) {
        this(id, name, null, imagePath, cellsWide, cellsHigh);
    }

    private TileDefinition(int id, String name, Rectangle source,
            String imagePath, int cellsWide, int cellsHigh) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.imagePath = imagePath;
        this.cellsWide = cellsWide;
        this.cellsHigh = cellsHigh;
    }
}
