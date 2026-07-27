package gdd;

import gdd.spawn.SpawnMode;
import java.awt.Color;

public final class Global {

    private Global() {
    }

    // Development settings
    public static final boolean DEV_USE_SHORT_STAGE_TIMERS = false;
    public static final int DEV_STAGE_DURATION_SECONDS = 15;
    public static final boolean DEV_SHOW_ENTITY_HITBOXES = false;

    // Level files
    public static final String SCENE1_TERRAIN_PATH = "src/levels/test-scene1-terrain.csv";
    public static final String SCENE1_EVENTS_PATH = "src/levels/test-scene1-events.csv";
    public static final String SCENE2_TERRAIN_PATH = "src/levels/scene2-terrain.csv";
    public static final String SCENE2_EVENTS_PATH = "src/levels/scene2-events.csv";

    // Window and timing
    public static final int BOARD_WIDTH = 716;
    public static final int BOARD_HEIGHT = 700;
    public static final int TARGET_FPS = 60;
    public static final int TIMER_DELAY_MS = 1000 / TARGET_FPS;
    public static final int STAGE_DURATION_SECONDS = 5 * 60;
    public static final int INITIAL_SPAWN_DELAY_SECONDS = 2;
    public static final SpawnMode DEFAULT_SPAWN_MODE = SpawnMode.SCRIPTED;
    public static final TransitionMode SCENE_TRANSITION_MODE = TransitionMode.SEAMLESS;
    public static final int SEAMLESS_TRANSITION_TICKS = secondsToTicks(1.5);
    public static final int PLAYER_DEATH_TICKS = secondsToTicks(1);

    // Shared rendering and collision tuning
    public static final double RENDER_SCALE = 1.0 ;
    public static final double HITBOX_SCALE = 1.0;
    public static final int WORLD_SCROLL_SPEED = 2;
    public static final double BACKGROUND_SCROLL_SPEED = WORLD_SCROLL_SPEED * 0.5;
    public static final double MIDGROUND_SCROLL_SPEED = WORLD_SCROLL_SPEED * 0.75;
    public static final int TILE_SIZE = 50;
    public static final int TERRAIN_HITBOX_BLOCK_SIZE = 10;
    public static final double TERRAIN_HITBOX_MIN_OPAQUE_COVERAGE = 0.20;

    // Player
    public static final int PLAYER_START_X = 90;
    public static final int PLAYER_START_Y = BOARD_HEIGHT / 2;
    public static final int PLAYER_WIDTH = 48;
    public static final int PLAYER_HEIGHT = 48;
    public static final int PLAYER_MAX_HEALTH = 5;
    public static final int PLAYER_BASE_SPEED = 4;
    public static final int PLAYER_SPEED_LEVEL_1 = 5;
    public static final int PLAYER_SPEED_LEVEL_2 = 6;
    public static final int PLAYER_INVINCIBILITY_TICKS = secondsToTicks(0.75);

    // Player bubbles
    public static final int BUBBLE_WIDTH = 14;
    public static final int BUBBLE_HEIGHT = 10;
    public static final int BUBBLE_SPEED = 8;
    public static final int BUBBLE_DAMAGE = 1;
    public static final int BASE_SHOT_COOLDOWN_TICKS = secondsToTicks(0.3);
    public static final int BURST_INTERVAL_TICKS = Math.max(1, secondsToTicks(0.08));

    // Power-ups
    public static final int SPEED_POWERUP_TICKS = secondsToTicks(15);
    public static final int MULTI_SHOT_TICKS = secondsToTicks(15);
    public static final int WEAPON_POWERUP_TICKS = secondsToTicks(10);
    public static final int POWERUP_WIDTH = 28;
    public static final int POWERUP_HEIGHT = 28;
    public static final double POWERUP_WAVE_AMPLITUDE = 18.0;
    public static final double POWERUP_WAVE_SPEED = 0.08;
    public static final int POWERUP_SPAWN_MIN_TICKS = secondsToTicks(2);
    public static final int POWERUP_SPAWN_MAX_TICKS = secondsToTicks(4);

    // Enemies
    public static final int ENEMY_CONTACT_DAMAGE = 1;
    public static final int ENEMY_PROJECTILE_DAMAGE = 1;
    public static final int RANDOM_SPAWN_MIN_TICKS = secondsToTicks(0.8);
    public static final int RANDOM_SPAWN_MAX_TICKS = secondsToTicks(1.8);

    // Obstacles
    public static final int MINE_SIZE = 45*2;
    public static final int MINE_DAMAGE = 1;
    public static final int MINE_EXPLOSION_RADIUS = 62;
    public static final int CORAL_WIDTH = 44;
    public static final int CORAL_HEIGHT = 62;
    public static final int CORAL_CONTACT_DAMAGE = 1;
    public static final boolean WALL_DAMAGE_ENABLED = true;
    public static final int WALL_DAMAGE = 1;

    // Boss
    public static final int BOSS_MAX_HEALTH = 50;
    public static final int BOSS_SCALE = 8;
    public static final int BOSS_PHASE_TWO_HEALTH = BOSS_MAX_HEALTH / 2;
    public static final int BOSS_CONTACT_DAMAGE = 1;
    public static final int BOSS_PHASE_ONE_COOLDOWN_TICKS = secondsToTicks(3);
    public static final int BOSS_PHASE_TWO_COOLDOWN_TICKS = secondsToTicks(2);
    public static final int BOSS_LASER_CHARGE_TICKS = secondsToTicks(1);
    public static final int BOSS_LASER_DURATION_TICKS = secondsToTicks(2);
    public static final int BOSS_LASER_INTERVAL_TICKS = secondsToTicks(0.10);
    public static final int BOSS_BITE_WARNING_TICKS = secondsToTicks(0.75);
    public static final int BOMBER_EXPLOSION_RADIUS = 50;
    public static final int BOMBER_EXPLOSION_DAMAGE = 1;
    public static final int BOSS_DEATH_TICKS = secondsToTicks(1.5);
    public static final int VICTORY_INPUT_DELAY_TICKS = secondsToTicks(3);

    // Placeholder colors. Replacing a sprite image does not change its hitbox.
    public static final Color COLOR_PLAYER = new Color(245, 139, 45);
    public static final Color COLOR_SPEED = new Color(255, 105, 180);
    public static final Color COLOR_MULTI = new Color(88, 220, 216);
    public static final Color COLOR_MEGA = new Color(255, 214, 64);
    public static final Color COLOR_SPLIT = new Color(154, 88, 220);
    public static final Color COLOR_HEAL = new Color(78, 220, 120);

    public static int stageDurationTicks() {
        if (DEV_USE_SHORT_STAGE_TIMERS) {
            return secondsToTicks(DEV_STAGE_DURATION_SECONDS);
        }
        else {
            return secondsToTicks(STAGE_DURATION_SECONDS);
        }
    }

    public static int secondsToTicks(double seconds) {
        int rounded = (int) Math.round(seconds * TARGET_FPS);

        if (rounded < 1) {
            return 1;
        } else {
            return rounded;
        }
    }

    public static int scaledFontSize(int baseFontSize) {
        return (int) Math.round(baseFontSize * RENDER_SCALE);
    }
}
