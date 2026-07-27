package gdd.audio;

import static gdd.Global.*;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class AudioManager implements AutoCloseable {

    private record AudioData(AudioFormat format, byte[] samples) {
    }

    private final Map<SoundEffect, AudioData> soundEffects =
            new EnumMap<>(SoundEffect.class);
    private final Set<Clip> activeSoundEffects =
            ConcurrentHashMap.newKeySet();

    private Clip musicClip;
    private MusicTrack musicTrack;
    private boolean musicPaused;

    public AudioManager() {
        if (!AUDIO_ENABLED) {
            return;
        }

        for (SoundEffect soundEffect : SoundEffect.values()) {
            soundEffects.put(soundEffect,
                    loadAudio(soundEffect.getPath()));
        }
    }

    public void playMusic(MusicTrack track) {
        if (!AUDIO_ENABLED) {
            return;
        }

        stopMusic();
        musicTrack = track;
        musicClip = openClip(loadAudio(track.getPath()), MUSIC_GAIN_DB);
        musicPaused = false;
        startMusicClip();
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
        musicTrack = null;
        musicPaused = false;
    }

    public void pauseMusic() {
        if (musicClip == null || musicPaused) {
            return;
        }
        musicClip.stop();
        musicPaused = true;
    }

    public void resumeMusic() {
        if (musicClip == null || !musicPaused) {
            return;
        }
        musicPaused = false;
        startMusicClip();
    }

    public void playSound(SoundEffect soundEffect) {
        if (!AUDIO_ENABLED) {
            return;
        }

        float gain = SFX_GAIN_DB + soundEffect.getGainOffsetDb();
        Clip clip = openClip(soundEffects.get(soundEffect), gain);
        activeSoundEffects.add(clip);
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP
                    && clip.getFramePosition() >= clip.getFrameLength()) {
                activeSoundEffects.remove(clip);
                clip.close();
            }
        });
        clip.start();
    }

    private void startMusicClip() {
        if (musicTrack.isLooping()) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            musicClip.start();
        }
    }

    private AudioData loadAudio(String path) {
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(
                new File(path).getAbsoluteFile())) {
            return new AudioData(stream.getFormat(), stream.readAllBytes());
        } catch (UnsupportedAudioFileException | IOException exception) {
            throw new IllegalStateException(
                    "Could not load audio file: " + path, exception);
        }
    }

    private Clip openClip(AudioData audio, float gainDecibels) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(audio.format(), audio.samples(),
                    0, audio.samples().length);
            applyGain(clip, gainDecibels);
            return clip;
        } catch (LineUnavailableException exception) {
            throw new IllegalStateException(
                    "Could not open an audio playback line", exception);
        }
    }

    private void applyGain(Clip clip, float gainDecibels) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gain = (FloatControl) clip.getControl(
                FloatControl.Type.MASTER_GAIN);
        gain.setValue(Math.max(gain.getMinimum(),
                Math.min(gain.getMaximum(), gainDecibels)));
    }

    @Override
    public void close() {
        stopMusic();
        for (Clip clip : activeSoundEffects) {
            clip.stop();
            clip.close();
        }
        activeSoundEffects.clear();
    }
}
