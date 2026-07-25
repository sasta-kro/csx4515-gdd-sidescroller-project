package gdd;

import static gdd.Global.*;
import gdd.powerup.WeaponType;
import gdd.sprite.Anglerfish;
import gdd.sprite.Bubble;
import gdd.sprite.Enemy;
import gdd.sprite.Player;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;

public class GameLogicTest {

    public static void main(String[] args) {
        movesPlayerWithHeldKeys();
        appliesDamageInvincibility();
        stacksAndReplacesPowerups();
        persistsPowerupsBetweenStages();
        emitsSixBubbleBurstAtLevelFour();
        followsScriptedSpawnSchedule();
        completesBossDeathDelay();
    }

    private static void movesPlayerWithHeldKeys() {
        RunState state = new RunState();
        Player player = new Player(state);
        int startX = player.getX();
        int startY = player.getY();

        player.keyPressed(key(KeyEvent.VK_D));
        player.keyPressed(key(KeyEvent.VK_S));
        player.act();

        assertEquals(startX + PLAYER_BASE_SPEED, player.getX(), "player x");
        assertEquals(startY + PLAYER_BASE_SPEED, player.getY(), "player y");
    }

    private static void appliesDamageInvincibility() {
        Player player = new Player(new RunState());

        assertTrue(player.damage(1), "first damage should apply");
        assertEquals(4, player.getHealth(), "health after first damage");
        assertTrue(!player.damage(1), "damage during invincibility should fail");

        for (int tick = 0; tick < PLAYER_INVINCIBILITY_TICKS; tick++) {
            player.act();
        }

        assertTrue(player.damage(1), "damage after invincibility should apply");
        assertEquals(3, player.getHealth(), "health after second damage");
    }

    private static void stacksAndReplacesPowerups() {
        Player player = new Player(new RunState());

        player.applySpeedUp();
        player.applySpeedUp();
        player.applySpeedUp();
        assertEquals(2, player.getSpeedLevel(), "speed stack cap");

        for (int count = 0; count < 4; count++) {
            player.applyMultiShot();
        }
        assertEquals(4, player.getMultiShotLevel(), "multi-shot stack cap");
        assertEquals(WeaponType.MULTI_SHOT, player.getWeaponType(),
                "multi-shot active");

        player.applyMegaShot();
        assertEquals(0, player.getMultiShotLevel(), "replacement clears stacks");
        assertEquals(WeaponType.MEGA_SHOT, player.getWeaponType(),
                "mega-shot replacement");
        assertEquals(2, player.getSpeedLevel(), "speed survives weapon replacement");
    }

    private static void emitsSixBubbleBurstAtLevelFour() {
        Player player = new Player(new RunState());
        for (int count = 0; count < 4; count++) {
            player.applyMultiShot();
        }
        player.keyPressed(key(KeyEvent.VK_SPACE));

        List<Bubble> bubbles = new ArrayList<>();
        for (int tick = 0; tick < TARGET_FPS; tick++) {
            player.act();
            bubbles.addAll(player.createBubbles());
            if (bubbles.size() == 6) {
                break;
            }
        }

        assertEquals(6, bubbles.size(), "level-four burst size");
    }

    private static void persistsPowerupsBetweenStages() {
        RunState state = new RunState();
        Player stageOnePlayer = new Player(state);
        stageOnePlayer.applySpeedUp();
        stageOnePlayer.applySpeedUp();
        stageOnePlayer.applyMultiShot();
        stageOnePlayer.applyMultiShot();
        stageOnePlayer.act();

        int remainingSpeedTicks = stageOnePlayer.getSpeedPowerupTicks();
        int remainingWeaponTicks = stageOnePlayer.getWeaponPowerupTicks();
        stageOnePlayer.syncTo(state);

        Player stageTwoPlayer = new Player(state);
        assertEquals(2, stageTwoPlayer.getSpeedLevel(),
                "persisted speed stacks");
        assertEquals(remainingSpeedTicks, stageTwoPlayer.getSpeedPowerupTicks(),
                "persisted speed timer");
        assertEquals(WeaponType.MULTI_SHOT, stageTwoPlayer.getWeaponType(),
                "persisted weapon");
        assertEquals(2, stageTwoPlayer.getMultiShotLevel(),
                "persisted multi-shot stacks");
        assertEquals(remainingWeaponTicks,
                stageTwoPlayer.getWeaponPowerupTicks(),
                "persisted weapon timer");
    }

    private static void followsScriptedSpawnSchedule() {
        Player player = new Player(new RunState());
        SpawnManager manager = new SpawnManager(player, 1, new Random(1));
        manager.setMode(SpawnMode.SCRIPTED);
        List<Enemy> enemies = new ArrayList<>();
        List<gdd.powerup.PowerUp> powerUps = new ArrayList<>();

        int firstSpawn = secondsToTicks(INITIAL_SPAWN_DELAY_SECONDS);
        manager.update(firstSpawn - 1, enemies, powerUps);
        assertEquals(0, enemies.size(), "no early scripted spawn");

        manager.update(firstSpawn, enemies, powerUps);
        assertEquals(1, enemies.size(), "first scripted spawn");
    }

    private static void completesBossDeathDelay() {
        Player player = new Player(new RunState());
        Anglerfish boss = new Anglerfish(player);
        assertTrue(boss.damage(BOSS_MAX_HEALTH), "boss lethal damage");

        for (int tick = 0; tick < BOSS_DEATH_TICKS; tick++) {
            boss.act();
        }

        assertTrue(boss.isDeathFinished(), "boss death delay completes");
        assertTrue(!boss.isVisible(), "boss disappears after death");
    }

    private static KeyEvent key(int keyCode) {
        return new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected
                    + ", got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
