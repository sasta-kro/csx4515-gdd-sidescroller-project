package gdd.audio;

public enum MusicTrack {

    MENU("src/audio/music/menu.wav", true),
    SCENE_1("src/audio/music/scene1.wav", true),
    SCENE_2("src/audio/music/scene2.wav", true),
    BOSS("src/audio/music/boss.wav", true),
    DEATH("src/audio/music/death.wav", false);

    private final String path;
    private final boolean looping;

    MusicTrack(String path, boolean looping) {
        this.path = path;
        this.looping = looping;
    }

    public String getPath() {
        return path;
    }

    public boolean isLooping() {
        return looping;
    }
}
