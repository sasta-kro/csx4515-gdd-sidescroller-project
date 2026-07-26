package gdd;

import static gdd.Global.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public class TileMap {

    private static final String TILE_SHEET_PATH = "src/images/obstacles/tiles.png";
    private static final BufferedImage TILE_SHEET = toBufferedImage(
            new ImageIcon(TILE_SHEET_PATH).getImage());
    private static final Map<Integer, BufferedImage> TILE_IMAGES = new HashMap<>();

    private final int[][] terrain;
    private final int columns;

    public TileMap(int[][] terrain) {
        this.terrain = terrain;
        columns = terrain[0].length;
    }

    public void draw(Graphics g, int stageTick) {
        int cameraX = stageTick * WORLD_SCROLL_SPEED;
        int firstColumn = Math.max(0, cameraX / TILE_SIZE - 4);
        int lastColumn = Math.min(columns - 1,
                (cameraX + BOARD_WIDTH) / TILE_SIZE + 1);

        for (int row = 0; row < terrain.length; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                int tile = terrain[row][column];
                if (tile <= 0) {
                    continue;
                }

                TileDefinition definition = TileRegistry.get(tile);
                int screenX = column * TILE_SIZE - cameraX;
                int screenY = row * TILE_SIZE;
                int width = definition.cellsWide * TILE_SIZE;
                int height = definition.cellsHigh * TILE_SIZE;
                g.drawImage(tileImage(definition), screenX, screenY,
                        width, height, null);
            }
        }
    }

    public boolean intersects(Rectangle screenBounds, int stageTick) {
        int cameraX = stageTick * WORLD_SCROLL_SPEED;
        Rectangle worldBounds = new Rectangle(
                screenBounds.x + cameraX,
                screenBounds.y,
                screenBounds.width,
                screenBounds.height);

        int firstColumn = Math.max(0, worldBounds.x / TILE_SIZE);
        int lastColumn = Math.min(columns - 1,
                (worldBounds.x + worldBounds.width - 1) / TILE_SIZE);
        int firstRow = Math.max(0, worldBounds.y / TILE_SIZE);
        int lastRow = Math.min(terrain.length - 1,
                (worldBounds.y + worldBounds.height - 1) / TILE_SIZE);

        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                if (terrain[row][column] != 0) {
                    Rectangle cell = new Rectangle(
                            column * TILE_SIZE, row * TILE_SIZE,
                            TILE_SIZE, TILE_SIZE);
                    if (worldBounds.intersects(cell)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getColumns() {
        return columns;
    }

    private BufferedImage tileImage(TileDefinition definition) {
        return TILE_IMAGES.computeIfAbsent(definition.id, ignored -> {
            Rectangle source = definition.source;
            return TILE_SHEET.getSubimage(source.x, source.y,
                    source.width, source.height);
        });
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }

        BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return bufferedImage;
    }
}
