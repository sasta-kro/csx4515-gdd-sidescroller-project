package gdd;

import gdd.scene.BossScene;
import gdd.scene.EndScene;
import gdd.scene.GameScene;
import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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

    public void loadScene1() {
        ensureRunState();
        showScene(new Scene1(this, runState));
    }

    public void loadScene2() {
        ensureRunState();
        showScene(new Scene2(this, runState));
    }

    public void loadBossScene() {
        ensureRunState();
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

    private void ensureRunState() {
        if (runState == null) {
            runState = new RunState();
        }
    }

    private void showScene(JPanel panel) {
        if (!(panel instanceof GameScene)) {
            throw new IllegalArgumentException("Scene panels must implement GameScene");
        }

        if (currentScene != null) {
            currentScene.stop();
        }

        panel.setPreferredSize(new Dimension(Global.BOARD_WIDTH, Global.BOARD_HEIGHT));
        setContentPane(panel);
        currentScene = (GameScene) panel;
        currentScene.start();
        pack();
        setLocationRelativeTo(null);

        Component component = (Component) currentScene;
        SwingUtilities.invokeLater(component::requestFocusInWindow);
    }
}
