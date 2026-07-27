package gdd.audio;

import gdd.powerup.WeaponType;

public enum SoundEffect {

    BASE_SHOT("src/audio/sfx/player/base-shot.wav"),
    MULTI_SHOT("src/audio/sfx/player/multi-shot.wav"),
    MEGA_SHOT("src/audio/sfx/player/mega-shot.wav"),
    SPLIT_SHOT("src/audio/sfx/player/split-shot.wav"),
    POWERUP_COLLECT("src/audio/sfx/powerups/weapon-upgrade.wav"),
    POWERUP_TIMEOUT("src/audio/sfx/powerups/upgrade-timeout.wav"),
    EXPLOSION("src/audio/sfx/world/mine-explosion.wav"),
    CORAL_BREAK_ROCK_THROW(
            "src/audio/sfx/world/coral-break-rock-throw.wav"),
    BOSS_LASER_CHARGE("src/audio/sfx/boss/laser-charge.wav");

    private final String path;

    SoundEffect(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static SoundEffect forWeapon(WeaponType weaponType) {
        return switch (weaponType) {
            case MULTI_SHOT -> MULTI_SHOT;
            case MEGA_SHOT -> MEGA_SHOT;
            case SPLIT_SHOT -> SPLIT_SHOT;
            default -> BASE_SHOT;
        };
    }
}
