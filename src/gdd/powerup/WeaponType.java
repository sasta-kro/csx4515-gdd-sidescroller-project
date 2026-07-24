package gdd.powerup;

public enum WeaponType {
    BASE("Base Bubble"),
    MULTI_SHOT("Multi-shot"),
    MEGA_SHOT("Mega-shot"),
    SPLIT_SHOT("Split-shot");

    private final String displayName;

    WeaponType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
