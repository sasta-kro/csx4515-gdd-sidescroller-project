package gdd.level;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public class TileMap {

    private static final String TILE_SHEET_PATH = "src/images/obstacles/tiles.png";
    private static final BufferedImage TILE_SHEET = toBufferedImage(new ImageIcon(TILE_SHEET_PATH).getImage());

    private static final Map<Integer, BufferedImage> TILE_IMAGES = new HashMap<>();
    private static final Map<Integer, boolean[][]> TILE_COLLISION_MASKS = new HashMap<>();

    private final int[][] terrain;
    private final int columns;
    private final boolean[][] collisionBlocks;

    public TileMap(int[][] terrain) {
        this.terrain = terrain;
        columns = terrain[0].length;
        collisionBlocks = new boolean[terrain.length * blocksPerTerrainCell()][columns * blocksPerTerrainCell()];
        buildCollisionBlocks();
    }

    public void draw(Graphics g, int stageTick) {
        int cameraX = stageTick * WORLD_SCROLL_SPEED;
        int firstColumn = Math.max(0, cameraX / TILE_SIZE - 4);
        int lastColumn = Math.min(columns - 1, (cameraX + BOARD_WIDTH) / TILE_SIZE + 1);

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
                g.drawImage(tileImage(definition), screenX, screenY, width, height, null);
            }
        }

        drawHitboxes(g, cameraX, firstColumn, lastColumn);
    }

    public boolean intersects(Rectangle screenBounds, int stageTick) {
        int cameraX = stageTick * WORLD_SCROLL_SPEED;
        Rectangle worldBounds = new Rectangle(
                screenBounds.x + cameraX,
                screenBounds.y,
                screenBounds.width,
                screenBounds.height);

        int firstColumn = Math.max(0, Math.floorDiv(worldBounds.x, TERRAIN_HITBOX_BLOCK_SIZE));
        int lastColumn = Math.min(collisionBlocks[0].length - 1, Math.floorDiv(worldBounds.x + worldBounds.width - 1, TERRAIN_HITBOX_BLOCK_SIZE));
        int firstRow = Math.max(0, Math.floorDiv(worldBounds.y, TERRAIN_HITBOX_BLOCK_SIZE));
        int lastRow = Math.min(collisionBlocks.length - 1, Math.floorDiv(worldBounds.y + worldBounds.height - 1, TERRAIN_HITBOX_BLOCK_SIZE));

        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {

                if (collisionBlocks[row][column]) {
                    return true;
                }
            }
        }
        return false;
    }


    /// Finds the shortest vertical or horizontal movement that puts screenBounds outside all solid terrain while keeping it inside allowedBounds.
    /// A zero point means that there is no collision (or no valid correction).
    public Point getCollisionCorrection(Rectangle screenBounds, Rectangle allowedBounds, int stageTick) {
        if (!intersects(screenBounds, stageTick)) {
            return new Point();
        }

        int maximumDistance = Math.max(allowedBounds.width, allowedBounds.height);

        for (int distance = 1; distance <= maximumDistance; distance++) {
            Point[] candidates = {
                new Point(-distance, 0),
                new Point(distance, 0),
                new Point(0, -distance),
                new Point(0, distance)
            };

            for (Point candidate : candidates) {
                Rectangle correctedBounds = new Rectangle(screenBounds);
                correctedBounds.translate(candidate.x, candidate.y);

                if (allowedBounds.contains(correctedBounds) && !intersects(correctedBounds, stageTick)) {
                    return candidate;
                }
            }
        }

        return new Point();
    }

    public int getColumns() {
        return columns;
    }

    private void drawHitboxes(Graphics g, int cameraX, int firstColumn, int lastColumn) {
        if (!DEV_SHOW_ENTITY_HITBOXES) {
            return;
        }

        int firstBlockColumn = Math.max(0, firstColumn * blocksPerTerrainCell());
        int lastBlockColumn = Math.min(collisionBlocks[0].length - 1, (lastColumn + 1) * blocksPerTerrainCell() - 1);
        Graphics2D debugGraphics = (Graphics2D) g.create();

        // hitbox transparent fill
        debugGraphics.setColor(new Color(255, 40, 40, 60));
        for (int row = 0; row < collisionBlocks.length; row++) {
            for (int column = firstBlockColumn; column <= lastBlockColumn; column++) {

                if (!collisionBlocks[row][column]) {
                    continue;
                }

                debugGraphics.fillRect(
                        column * TERRAIN_HITBOX_BLOCK_SIZE - cameraX,
                        row * TERRAIN_HITBOX_BLOCK_SIZE,
                        TERRAIN_HITBOX_BLOCK_SIZE,
                        TERRAIN_HITBOX_BLOCK_SIZE);
            }
        }

        // hitbox outline
        debugGraphics.setColor(Color.RED);
        for (int row = 0; row < collisionBlocks.length; row++) {
            for (int column = firstBlockColumn; column <= lastBlockColumn; column++) {

                if (!collisionBlocks[row][column]) {
                    continue;
                }

                int x = column * TERRAIN_HITBOX_BLOCK_SIZE - cameraX;
                int y = row * TERRAIN_HITBOX_BLOCK_SIZE;
                int farX = x + TERRAIN_HITBOX_BLOCK_SIZE;
                int farY = y + TERRAIN_HITBOX_BLOCK_SIZE;

                if (!isSolidBlock(row - 1, column)) {
                    debugGraphics.drawLine(x, y, farX, y);
                }
                if (!isSolidBlock(row + 1, column)) {
                    debugGraphics.drawLine(x, farY, farX, farY);
                }
                if (!isSolidBlock(row, column - 1)) {
                    debugGraphics.drawLine(x, y, x, farY);
                }
                if (!isSolidBlock(row, column + 1)) {
                    debugGraphics.drawLine(farX, y, farX, farY);
                }
            }
        }

        debugGraphics.dispose();
    }

    private void buildCollisionBlocks() {
        int blocksPerCell = blocksPerTerrainCell();

        for (int row = 0; row < terrain.length; row++) {
            for (int column = 0; column < columns; column++) {

                int tile = terrain[row][column];
                if (tile <= 0) {
                    continue;
                }

                boolean[][] tileMask = tileCollisionMask(TileRegistry.get(tile));
                int firstBlockRow = row * blocksPerCell;
                int firstBlockColumn = column * blocksPerCell;

                for (int maskRow = 0; maskRow < tileMask.length; maskRow++) {
                    for (int maskColumn = 0; maskColumn < tileMask[maskRow].length; maskColumn++) {

                        if (tileMask[maskRow][maskColumn]) {
                            collisionBlocks[firstBlockRow + maskRow][firstBlockColumn + maskColumn] = true;
                        }
                    }
                }
            }
        }
    }

    private static boolean[][] tileCollisionMask(TileDefinition definition) {
        return TILE_COLLISION_MASKS.computeIfAbsent(
                definition.id, ignored -> createTileCollisionMask(definition));
    }

    private static boolean[][] createTileCollisionMask(TileDefinition definition) {
        int renderWidth = definition.cellsWide * TILE_SIZE;
        int renderHeight = definition.cellsHigh * TILE_SIZE;

        BufferedImage scaledTile = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaledTile.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(tileImage(definition), 0, 0, renderWidth, renderHeight, null);
        graphics.dispose();

        int maskColumns = renderWidth / TERRAIN_HITBOX_BLOCK_SIZE;
        int maskRows = renderHeight / TERRAIN_HITBOX_BLOCK_SIZE;
        boolean[][] mask = new boolean[maskRows][maskColumns];
        int pixelsPerBlock = TERRAIN_HITBOX_BLOCK_SIZE * TERRAIN_HITBOX_BLOCK_SIZE;
        int minimumOpaquePixels = (int) Math.ceil(pixelsPerBlock * TERRAIN_HITBOX_MIN_OPAQUE_COVERAGE);

        for (int row = 0; row < maskRows; row++) {
            for (int column = 0; column < maskColumns; column++) {
                int opaquePixels = 0;

                for (int y = row * TERRAIN_HITBOX_BLOCK_SIZE;
                        y < (row + 1) * TERRAIN_HITBOX_BLOCK_SIZE; y++) {

                    for (int x = column * TERRAIN_HITBOX_BLOCK_SIZE;
                            x < (column + 1)
                                    * TERRAIN_HITBOX_BLOCK_SIZE; x++) {

                        if ((scaledTile.getRGB(x, y) >>> 24) != 0) {
                            opaquePixels++;
                        }
                    }
                }

                mask[row][column] = opaquePixels >= minimumOpaquePixels;
            }
        }

        return mask;
    }

    private boolean isSolidBlock(int row, int column) {
        return row >= 0 && row < collisionBlocks.length
                && column >= 0 && column < collisionBlocks[row].length
                && collisionBlocks[row][column];
    }

    private static int blocksPerTerrainCell() {
        return TILE_SIZE / TERRAIN_HITBOX_BLOCK_SIZE;
    }

    private static BufferedImage tileImage(TileDefinition definition) {
        return TILE_IMAGES.computeIfAbsent(definition.id, ignored -> {
            if (definition.imagePath != null) {
                return toBufferedImage(
                        new ImageIcon(definition.imagePath).getImage());
            }

            Rectangle source = definition.source;
            return TILE_SHEET.getSubimage(source.x, source.y, source.width, source.height);
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
