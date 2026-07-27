package gdd;

import gdd.audio.AudioManager;
import gdd.audio.MusicTrack;
import gdd.audio.SoundEffect;
import gdd.scene.BossScene;
import gdd.scene.EndScene;
import gdd.scene.GameScene;
import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JFrame {

    private GameScene currentScene;
    private RunState runState;
    private final AudioManager audio = new AudioManager();

    public Game() {
        initUI();
        loadTitle();
    }

    private void initUI() {
        setTitle("Ocean Invaders");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
    }

    public void loadTitle() {
        runState = null;
        showScene(new TitleScene(this), MusicTrack.MENU);
    }

    public void startNewGame() {
        runState = new RunState();
        loadScene1();
    }

    public void startFromScene2() {
        runState = new RunState();
        loadScene2();
    }

    public void startFromBoss() {
        runState = new RunState();
        loadBossScene();
    }

    public void loadScene1() {
        showScene(new Scene1(this, runState), MusicTrack.SCENE_1);
    }

    public void loadScene2() {
        showScene(new Scene2(this, runState), MusicTrack.SCENE_2);
    }

    public void loadBossScene() {
        showScene(new BossScene(this, runState), MusicTrack.BOSS);
    }

    public void showGameOver() {
        showScene(new EndScene(this, false), MusicTrack.DEATH);
    }

    public void showVictory() {
        showScene(new EndScene(this, true), null);
    }

    public void playSound(SoundEffect soundEffect) {
        audio.playSound(soundEffect);
    }

    public void setMusicPaused(boolean paused) {
        if (paused) {
            audio.pauseMusic();
        } else {
            audio.resumeMusic();
        }
    }

    public void quit() {
        if (currentScene != null) {
            currentScene.stop();
        }
        audio.close();
        dispose();
    }

    private void showScene(JPanel panel, MusicTrack musicTrack) {
        if (currentScene != null) {
            currentScene.stop();
        }

        if (musicTrack == null) {
            audio.stopMusic();
        } else {
            audio.playMusic(musicTrack);
        }

        panel.setPreferredSize(new Dimension(Global.BOARD_WIDTH, Global.BOARD_HEIGHT));
        setContentPane(panel);
        currentScene = (GameScene) panel; // casting
        currentScene.start();
        pack();  // REALLY IMPORTANT: sets window size based on inside-components size
        setLocationRelativeTo(null); // centers the window on screen
    }
}
