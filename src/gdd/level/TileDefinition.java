package gdd.level;

import java.awt.Rectangle;

public class TileDefinition {

    public final int id;
    public final String name;
    public final Rectangle source;
    public final String imagePath;
    public final int cellsWide;
    public final int cellsHigh;
    public final int rotationQuarterTurns;

    public TileDefinition(int id, String name, Rectangle source,
            int cellsWide, int cellsHigh) {
        this(id, name, source, null, cellsWide, cellsHigh, 0);
    }

    public TileDefinition(int id, String name, Rectangle source,
            int cellsWide, int cellsHigh, int rotationQuarterTurns) {
        this(id, name, source, null, cellsWide, cellsHigh,
                rotationQuarterTurns);
    }

    public TileDefinition(int id, String name, String imagePath,
            int cellsWide, int cellsHigh) {
        this(id, name, null, imagePath, cellsWide, cellsHigh, 0);
    }

    private TileDefinition(int id, String name, Rectangle source,
            String imagePath, int cellsWide, int cellsHigh,
            int rotationQuarterTurns) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.imagePath = imagePath;
        this.cellsWide = cellsWide;
        this.cellsHigh = cellsHigh;
        this.rotationQuarterTurns = rotationQuarterTurns;
    }
}
