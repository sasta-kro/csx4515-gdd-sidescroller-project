package gdd.audio;

import gdd.powerup.WeaponType;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioAssetTest {

    public static void main(String[] args) throws Exception {
        Set<String> registeredPaths = new HashSet<>();

        for (MusicTrack track : MusicTrack.values()) {
            registeredPaths.add(track.getPath());
        }
        for (SoundEffect soundEffect : SoundEffect.values()) {
            registeredPaths.add(soundEffect.getPath());
        }

        Set<String> wavPaths;
        try (var paths = Files.walk(Path.of("src/audio"))) {
            wavPaths = paths
                    .filter(path -> path.toString().endsWith(".wav"))
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }

        assert wavPaths.equals(registeredPaths)
                : "Every WAV asset must be registered";

        for (String path : registeredPaths) {
            verifyAudioFormat(path);
        }

        assert SoundEffect.forWeapon(WeaponType.BASE)
                == SoundEffect.BASE_SHOT;
        assert SoundEffect.forWeapon(WeaponType.MULTI_SHOT)
                == SoundEffect.MULTI_SHOT;
        assert SoundEffect.forWeapon(WeaponType.MEGA_SHOT)
                == SoundEffect.MEGA_SHOT;
        assert SoundEffect.forWeapon(WeaponType.SPLIT_SHOT)
                == SoundEffect.SPLIT_SHOT;
        assert SoundEffect.MULTI_SHOT.getPath().equals(
                SoundEffect.SPLIT_SHOT.getPath())
                : "Multi-shot and split-shot must share their audio";
        assert SoundEffect.SNAKE_ATTACK.getPath().equals(
                SoundEffect.JELLYFISH_ATTACK.getPath())
                : "Snake and Jellyfish must share their attack audio";
        assert SoundEffect.JELLYFISH_ATTACK.getGainOffsetDb()
                == SoundEffect.SNAKE_ATTACK.getGainOffsetDb() - 6.0f
                : "Jellyfish attack must play at half amplitude";
        assert MusicTrack.MENU.getPath().equals(
                MusicTrack.SCENE_2.getPath())
                : "Menu and Scene 2 must share their music";
        assert !MusicTrack.SCENE_1.getPath().equals(
                MusicTrack.SCENE_2.getPath())
                : "Scene 1 must use the former Scene 2 music";

        System.out.println("AudioAssetTest passed");
    }

    private static void verifyAudioFormat(String path) throws Exception {
        File file = new File(path);
        assert file.isFile() : "Missing audio file: " + path;

        try (AudioInputStream stream =
                AudioSystem.getAudioInputStream(file)) {
            AudioFormat format = stream.getFormat();
            assert format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
                    : path + " must use signed PCM";
            assert format.getSampleRate() == 44_100.0f
                    : path + " must use 44.1 kHz audio";
            assert format.getSampleSizeInBits() == 16
                    : path + " must use 16-bit audio";
            assert format.getChannels() == 1 || format.getChannels() == 2
                    : path + " must be mono or stereo";
        }
    }
}
