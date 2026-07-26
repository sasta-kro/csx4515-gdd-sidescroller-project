package gdd;

import java.awt.Rectangle;

public class TileDefinition {

    public final int id;
    public final String name;
    public final Rectangle source;
    public final int cellsWide;
    public final int cellsHigh;

    public TileDefinition(int id, String name, Rectangle source,
            int cellsWide, int cellsHigh) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.cellsWide = cellsWide;
        this.cellsHigh = cellsHigh;
    }
}
