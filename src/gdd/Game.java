package gdd;

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
        showScene(new TitleScene(this));
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
        showScene(new Scene1(this, runState));
    }

    public void loadScene2() {
        showScene(new Scene2(this, runState));
    }

    public void loadBossScene() {
        showScene(new BossScene(this, runState));
    }

    public void showGameOver() {
        showScene(new EndScene(this, false));
    }

    public void showVictory() {
        showScene(new EndScene(this, true));
    }

    public void quit() {
        if (currentScene != null) {
            currentScene.stop();
        }
        dispose();
    }

    private void showScene(JPanel panel) {
        if (currentScene != null) {
            currentScene.stop();
        }

        panel.setPreferredSize(new Dimension(Global.BOARD_WIDTH, Global.BOARD_HEIGHT));
        setContentPane(panel);
        currentScene = (GameScene) panel; // casting
        currentScene.start();
        pack();  // REALLY IMPORTANT: sets window size based on inside-components size
        setLocationRelativeTo(null); // centers the window on screen
    }
}
