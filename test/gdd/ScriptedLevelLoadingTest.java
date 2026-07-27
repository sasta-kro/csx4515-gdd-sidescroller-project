package gdd;

import static gdd.Global.*;
import gdd.level.LevelLoader;
import gdd.powerup.PowerUp;
import gdd.spawn.SpawnDetails;
import gdd.spawn.SpawnManager;
import gdd.spawn.SpawnMode;
import gdd.sprite.enemy.Enemy;
import gdd.sprite.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptedLevelLoadingTest {

    public static void main(String[] args) {
        verifiesScene1Files();
        verifiesScene2Files();
        verifiesScene1Spawns();
        verifiesScene2Spawns();
    }

    private static void verifiesScene1Files() {
        int[][] terrain = LevelLoader.loadTerrain(
                SCENE1_TERRAIN_PATH);
        Map<Integer, List<SpawnDetails>> schedule = LevelLoader.loadEvents(
                SCENE1_EVENTS_PATH);

        assertEquals(14, terrain.length, "Scene 1 terrain rows");
        assertEquals(735, terrain[0].length, "Scene 1 terrain columns");
        assertEquals(228, eventCount(schedule), "Scene 1 events");
        assertEquals(2, schedule.get(17441).size(),
                "Scene 1 same-tick pair");
        assertEquals("Mine", schedule.get(24).get(0).type,
                "Scene 1 first world event");
    }

    private static void verifiesScene2Files() {
        int[][] terrain = LevelLoader.loadTerrain(
                SCENE2_TERRAIN_PATH);
        Map<Integer, List<SpawnDetails>> schedule = LevelLoader.loadEvents(
                SCENE2_EVENTS_PATH);

        assertEquals(14, terrain.length, "Scene 2 terrain rows");
        assertEquals(735, terrain[0].length, "Scene 2 terrain columns");
        assertEquals(180, eventCount(schedule), "Scene 2 events");
        assertEquals(2, schedule.get(2252).size(),
                "Scene 2 same-tick pair");
        assertEquals("Coral", schedule.get(56).get(0).type,
                "Scene 2 first world event");
    }

    private static void verifiesScene1Spawns() {
        SpawnCounts counts = spawnAll(1, SCENE1_EVENTS_PATH,
                stageDurationTicks());

        assertEquals(116, counts.enemies, "Scene 1 spawned enemies");
        assertEquals(58, counts.powerUps, "Scene 1 spawned powerups");
        assertEquals(54, counts.worldEvents,
                "Scene 1 spawned world events");
    }

    private static void verifiesScene2Spawns() {
        SpawnCounts counts = spawnAll(2, SCENE2_EVENTS_PATH,
                stageDurationTicks());

        assertEquals(82, counts.enemies, "Scene 2 spawned enemies");
        assertEquals(28, counts.powerUps, "Scene 2 spawned powerups");
        assertEquals(70, counts.worldEvents,
                "Scene 2 spawned world events");
    }

    private static SpawnCounts spawnAll(int stageNumber,
            String eventsPath, int lastTick) {
        Player player = new Player(new RunState());
        SpawnManager manager = new SpawnManager(player, stageNumber,
                SpawnMode.SCRIPTED, LevelLoader.loadEvents(eventsPath));
        List<Enemy> enemies = new ArrayList<>();
        List<PowerUp> powerUps = new ArrayList<>();
        int worldEvents = 0;

        for (int tick = 1; tick <= lastTick; tick++) {
            worldEvents += manager.update(
                    tick, enemies, powerUps).size();
        }

        return new SpawnCounts(
                enemies.size(), powerUps.size(), worldEvents);
    }

    private static int eventCount(
            Map<Integer, List<SpawnDetails>> schedule) {
        return schedule.values().stream().mapToInt(List::size).sum();
    }

    private static void assertEquals(Object expected, Object actual,
            String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected
                    + ", got " + actual);
        }
    }

    private static class SpawnCounts {

        final int enemies;
        final int powerUps;
        final int worldEvents;

        SpawnCounts(int enemies, int powerUps, int worldEvents) {
            this.enemies = enemies;
            this.powerUps = powerUps;
            this.worldEvents = worldEvents;
        }
    }
}
