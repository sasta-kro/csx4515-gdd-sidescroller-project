package gdd;

import static gdd.Global.*;
import gdd.powerup.Heal;
import gdd.powerup.MegaShot;
import gdd.powerup.MultiShot;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.SplitShot;
import gdd.sprite.Enemy;
import gdd.sprite.Jellyfish;
import gdd.sprite.Octopus;
import gdd.sprite.Player;
import gdd.sprite.Snake;
import gdd.sprite.Swordfish;
import gdd.sprite.Turtle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpawnManager {

    private final Random random;
    private final Player player;
    private final int stageNumber;
    private final SpawnMode mode;
    private final Map<Integer, List<SpawnDetails>> scriptedSpawns;
    private int nextEnemyTick;
    private int nextPowerupTick;

    public SpawnManager(Player player, int stageNumber, SpawnMode mode,
            Map<Integer, List<SpawnDetails>> scriptedSpawns) {
        this.player = player;
        this.stageNumber = stageNumber;
        this.mode = mode;
        this.scriptedSpawns = scriptedSpawns;
        random = new Random();
        int initialDelay = secondsToTicks(INITIAL_SPAWN_DELAY_SECONDS);
        nextEnemyTick = initialDelay;
        nextPowerupTick = initialDelay + POWERUP_SPAWN_MIN_TICKS;
    }

    public List<SpawnDetails> update(int stageTick, List<Enemy> enemies,
            List<PowerUp> powerUps) {
        if (mode == SpawnMode.SCRIPTED) {
            return spawnScripted(stageTick, enemies, powerUps);
        }

        if (stageTick >= nextEnemyTick) {
            enemies.add(createRandomEnemy());
            nextEnemyTick = stageTick + randomBetween(
                    RANDOM_SPAWN_MIN_TICKS, RANDOM_SPAWN_MAX_TICKS);
        }

        if (stageTick >= nextPowerupTick) {
            powerUps.add(createRandomPowerUp());
            nextPowerupTick = stageTick + randomBetween(
                    POWERUP_SPAWN_MIN_TICKS, POWERUP_SPAWN_MAX_TICKS);
        }

        return List.of();
    }

    private List<SpawnDetails> spawnScripted(int stageTick,
            List<Enemy> enemies,
            List<PowerUp> powerUps) {
        List<SpawnDetails> details = scriptedSpawns.get(stageTick);
        if (details == null) {
            return List.of();
        }

        List<SpawnDetails> worldEvents = new ArrayList<>();
        for (SpawnDetails detail : details) {
            if (detail.type.startsWith("PowerUp-")) {
                powerUps.add(createPowerUp(detail.type, detail.x, detail.y));
            } else if (detail.type.equals("Mine")
                    || detail.type.equals("Coral")) {
                worldEvents.add(detail);
            } else {
                enemies.add(createEnemy(detail.type, detail.x, detail.y));
            }
        }
        return worldEvents;
    }

    private Enemy createRandomEnemy() {
        int y = randomBetween(60, BOARD_HEIGHT - 120);
        int x = BOARD_WIDTH + 40;

        if (stageNumber == 1) {
            return random.nextBoolean()
                    ? new Jellyfish(player, x, y)
                    : new Turtle(player, x, y);
        }

        switch (random.nextInt(5)) {
            case 0:
                return new Jellyfish(player, x, y);
            case 1:
                return new Turtle(player, x, y);
            case 2:
                return new Octopus(player, x, y);
            case 3:
                return new Swordfish(player, x, y);
            default:
                int snakeX = randomBetween(0, BOARD_WIDTH - 28);
                return new Snake(player, snakeX, random.nextBoolean());
        }
    }

    private PowerUp createRandomPowerUp() {
        int x = BOARD_WIDTH + 30;
        int y = randomBetween(80, BOARD_HEIGHT - 130);
        switch (random.nextInt(5)) {
            case 0:
                return new SpeedUp(x, y);
            case 1:
                return new MultiShot(x, y);
            case 2:
                return new MegaShot(x, y);
            case 3:
                return new SplitShot(x, y);
            default:
                return new Heal(x, y);
        }
    }

    private Enemy createEnemy(String type, int x, int y) {
        switch (type) {
            case "Jellyfish":
                return new Jellyfish(player, x, y);
            case "Turtle":
                return new Turtle(player, x, y);
            case "Octopus":
                return new Octopus(player, x, y);
            case "Swordfish":
                return new Swordfish(player, x, y);
            case "SnakeTop":
                return new Snake(player, x, true);
            case "SnakeBottom":
                return new Snake(player, x, false);
            default:
                throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
    }

    private PowerUp createPowerUp(String type, int x, int y) {
        switch (type) {
            case "PowerUp-Speed":
                return new SpeedUp(x, y);
            case "PowerUp-Multi":
                return new MultiShot(x, y);
            case "PowerUp-Mega":
                return new MegaShot(x, y);
            case "PowerUp-Split":
                return new SplitShot(x, y);
            case "PowerUp-Heal":
                return new Heal(x, y);
            default:
                throw new IllegalArgumentException("Unknown power-up type: " + type);
        }
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt(Math.max(1, max - min + 1));
    }
}
