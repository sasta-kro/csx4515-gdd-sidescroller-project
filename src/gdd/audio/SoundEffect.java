package gdd.audio;

import gdd.powerup.WeaponType;

public enum SoundEffect {

    BASE_SHOT("src/audio/sfx/player/base-shot.wav", 10.0f),
    MULTI_SHOT("src/audio/sfx/player/split-shot.wav", 3.0f),
    MEGA_SHOT("src/audio/sfx/player/mega-shot.wav", -10.0f),
    SPLIT_SHOT("src/audio/sfx/player/split-shot.wav", 3.0f),
    PLAYER_HURT("src/audio/sfx/player/hurt.wav"),
    POWERUP_COLLECT("src/audio/sfx/powerups/weapon-upgrade.wav"),
    POWERUP_TIMEOUT("src/audio/sfx/powerups/upgrade-timeout.wav"),
    EXPLOSION("src/audio/sfx/world/mine-explosion.wav"),
    OCTOPUS_ROCK_THROW(
            "src/audio/sfx/enemies/octopus-rock-throw.wav"),
    CORAL_BREAK(
            "src/audio/sfx/enemies/octopus-rock-throw.wav"),
    SNAKE_ATTACK("src/audio/sfx/enemies/snake-attack.wav", -10.0f),
    JELLYFISH_ATTACK("src/audio/sfx/enemies/snake-attack.wav", -18.0f),
    BOSS_LASER_CHARGE("src/audio/sfx/boss/laser-charge.wav", -20.0f),
    BOSS_BITE("src/audio/sfx/boss/bite.wav", 10.0f);

    private final String path;
    private final float gainOffsetDb;

    SoundEffect(String path) {
        this(path, 0.0f);
    }

    SoundEffect(String path, float gainOffsetDb) {
        this.path = path;
        this.gainOffsetDb = gainOffsetDb;
    }

    public String getPath() {
        return path;
    }

    public float getGainOffsetDb() {
        return gainOffsetDb;
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
