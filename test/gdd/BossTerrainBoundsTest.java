package gdd;

import static gdd.Global.*;
import gdd.level.LevelLoader;
import gdd.level.TileMap;
import gdd.sprite.Player;
import gdd.sprite.enemy.Anglerfish;
import java.awt.Rectangle;

public class BossTerrainBoundsTest {

    public static void main(String[] args) {
        clampsBossToStraightWallHitboxes();
    }

    private static void clampsBossToStraightWallHitboxes() {
        TileMap tileMap = new TileMap(
                LevelLoader.loadTerrain(BOSS_TERRAIN_PATH));
        Rectangle openBounds = tileMap.getVerticalOpenBounds(
                TILE_SIZE,
                (tileMap.getColumns() - 2) * TILE_SIZE);

        assertEquals(50, openBounds.y, "ceiling hitbox edge");
        assertEquals(650, openBounds.y + openBounds.height,
                "floor hitbox edge");

        Anglerfish boss = new Anglerfish(
                new Player(new RunState()));
        boss.setVerticalHitboxBounds(openBounds.y,
                openBounds.y + openBounds.height);

        boss.setY(-BOARD_HEIGHT);
        boss.act();
        assertEquals(openBounds.y, boss.getBounds().y,
                "boss top clamp");

        boss.setY(BOARD_HEIGHT);
        boss.act();
        assertEquals(openBounds.y + openBounds.height,
                boss.getBounds().y + boss.getBounds().height,
                "boss bottom clamp");
    }

    private static void assertEquals(int expected, int actual,
            String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected "
                    + expected + ", got " + actual);
        }
    }
}
