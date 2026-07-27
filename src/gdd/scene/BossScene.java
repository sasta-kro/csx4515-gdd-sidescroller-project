package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.RunState;
import gdd.sprite.enemy.Anglerfish;
import gdd.sprite.enemy.BomberFish;
import gdd.sprite.Bubble;
import gdd.sprite.enemy.Enemy;
import gdd.sprite.enemy.EnemyProjectile;
import gdd.sprite.obstacle.Explosion;
import gdd.sprite.Player;
import gdd.ui.GameHud;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class BossScene extends JPanel implements GameScene {

    private static final String BACKGROUND_IMAGE_PATH = "src/images/background/scene2_bg/background.png";
    private static final String MIDGROUND_IMAGE_PATH = "src/images/background/scene2_bg/midground.png";

    private static final ImageIcon backgroundImage = new ImageIcon(BACKGROUND_IMAGE_PATH);
    private static final ImageIcon midgroundImage = new ImageIcon(MIDGROUND_IMAGE_PATH);
    private static final double BACKGROUND_SCALE = BOARD_HEIGHT / (double) midgroundImage.getIconHeight();
    private static final double BACKGROUND_TILE_SCALE = BACKGROUND_SCALE * 2;
    private static final int BACKGROUND_TILE_WIDTH = (int) Math.round(backgroundImage.getIconWidth() * BACKGROUND_TILE_SCALE);
    private static final int BACKGROUND_TILE_HEIGHT = (int) Math.round(backgroundImage.getIconHeight() * BACKGROUND_TILE_SCALE);
    private static final int MIDGROUND_WIDTH = (int) Math.round(midgroundImage.getIconWidth() * BACKGROUND_SCALE);
    private static final int MIDGROUND_HEIGHT = (int) Math.round(midgroundImage.getIconHeight() * BACKGROUND_SCALE);

    private Game game;
    private RunState runState;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bubble> playerBubbles = new ArrayList<>();
    private List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();

    private Player player;
    private Anglerfish boss;
    private KeyAdapter input = new SceneInput();
    private Timer timer;
    private boolean paused;
    private boolean finished;
    private int playerDeathTicks;

    public BossScene(Game game, RunState runState) {
        this.game = game;
        this.runState = runState;
        setFocusable(true);
        setBackground(new Color(3, 34, 62));
    }

    @Override
    public void start() {
        resetStage();
        addKeyListener(input);
        timer = new Timer(TIMER_DELAY_MS, event -> gameTick(event));
        timer.start();
        requestFocusInWindow();
    }

    @Override
    public void stop() {
        removeKeyListener(input);
        timer.stop();
        clearStageEntities();
    }

    private void resetStage() {
        clearStageEntities();
        paused = false;
        finished = false;
        playerDeathTicks = 0;

        player = new Player(runState);
        boss = new Anglerfish(player);
        enemies.add(boss);
    }

    private void gameTick(ActionEvent event) {
        if (!paused && !finished) {
            updateGame();
        }
        repaint();
    }

    // This is the boss scene's version of the professor's update() method.
    void updateGame() {
        if (playerDeathTicks > 0) {
            updatePlayerDeath();
            return;
        }

        updatePlayer();
        updateEnemies();
        updateProjectiles();
        updateExplosions();
        advanceSpriteAnimations();

        if (updateBossFight()) {
            return;
        }

        handleCollisions();
        removeDeadEntities();

        if (player.isDead()) {
            player.setDying(true);
            playerDeathTicks = PLAYER_DEATH_TICKS;
        }
    }

    private void updatePlayerDeath() {
        // Existing explosions continue during the death delay.
        updateExplosions();
        explosions.removeIf(explosion -> !explosion.isVisible());
        playerDeathTicks--;

        if (playerDeathTicks <= 0) {
            finishAsGameOver();
        }
    }

    private void updatePlayer() {
        player.act();
        playerBubbles.addAll(player.createBubbles());
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act();
            }
        }
    }

    private void updateProjectiles() {
        for (Bubble bubble : playerBubbles) {
            bubble.act();
        }

        for (EnemyProjectile projectile : enemyProjectiles) {
            projectile.act();
        }
    }

    private void updateExplosions() {
        for (Explosion explosion : explosions) {
            explosion.act();
        }
    }

    private void advanceSpriteAnimations() {
        player.advanceAnimation();

        for (Enemy enemy : enemies) {
            enemy.advanceAnimation();
        }

        for (Bubble bubble : playerBubbles) {
            bubble.advanceAnimation();
        }

        for (EnemyProjectile projectile : enemyProjectiles) {
            projectile.advanceAnimation();
        }

        for (Explosion explosion : explosions) {
            explosion.advanceAnimation();
        }
    }

    private boolean updateBossFight() {
        enemyProjectiles.addAll(boss.takePendingProjectiles());
        enemies.addAll(boss.takePendingSummons());

        for (Enemy enemy : enemies) {
            if (enemy instanceof BomberFish) {
                BomberFish bomber = (BomberFish) enemy;

                if (bomber.isVisible() && bomber.shouldExplode()) {
                    Explosion explosion = new Explosion(
                            bomber.getX() + bomber.getRenderWidth() / 2,
                            bomber.getY() + bomber.getRenderHeight() / 2,
                            BOMBER_EXPLOSION_RADIUS);
                    explosions.add(explosion);
                    if (explosion.reaches(player)) {
                        player.damage(BOMBER_EXPLOSION_DAMAGE);
                    }
                    bomber.die();
                }
            }
        }

        if (boss.isDeathFinished()) {
            finishAsVictory();
            return true;
        }

        return false;
    }

    private void handleCollisions() {
        handlePlayerBubbleCollisions();
        handleEnemyProjectileCollisions();
        handleEnemyContact();
    }

    private void handlePlayerBubbleCollisions() {
        for (Bubble bubble : playerBubbles) {
            if (!bubble.isVisible()) {
                continue;
            }

            for (Enemy enemy : enemies) {
                if (bubble.collidesWith(enemy)) {
                    boolean killed = enemy.damage(bubble.getDamage());
                    bubble.die();

                    if (killed) {
                        runState.addScore(enemy.getScoreValue());
                        addSmallExplosion(enemy);
                        handleKilledBoss(enemy);
                    }
                    break;
                }
            }
        }
    }

    private void handleKilledBoss(Enemy killedEnemy) {
        if (killedEnemy == boss) {
            enemyProjectiles.clear();

            for (Enemy enemy : enemies) {
                if (enemy instanceof BomberFish) {
                    enemy.die();
                }
            }
        }
    }

    private void handleEnemyProjectileCollisions() {
        for (EnemyProjectile projectile : enemyProjectiles) {
            if (projectile.isVisible() && projectile.collidesWith(player)) {
                player.damage(projectile.getDamage());
                projectile.die();
            }
        }
    }

    private void handleEnemyContact() {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible() && enemy.collidesWith(player)) {
                player.damage(enemy.getContactDamage());
            }
        }
    }

    private void addSmallExplosion(gdd.sprite.Sprite sprite) {
        explosions.add(new Explosion(
                sprite.getX() + sprite.getRenderWidth() / 2,
                sprite.getY() + sprite.getRenderHeight() / 2));
    }

    private void removeDeadEntities() {
        enemies.removeIf(enemy -> !enemy.isVisible() && enemy != boss);
        playerBubbles.removeIf(bubble -> !bubble.isVisible());
        enemyProjectiles.removeIf(projectile -> !projectile.isVisible());
        explosions.removeIf(explosion -> !explosion.isVisible());
    }

    private void finishAsGameOver() {
        finished = true;
        player.syncTo(runState);
        game.showGameOver();
    }

    private void finishAsVictory() {
        finished = true;
        player.syncTo(runState);
        game.showVictory();
    }

    private void clearStageEntities() {
        enemies.clear();
        playerBubbles.clear();
        enemyProjectiles.clear();
        explosions.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackground(g);
        drawEntities(g);
        drawBossHealth(g);
        drawHud(g);

        if (paused) {
            drawPauseOverlay(g);
        }
    }

    private void drawBackground(Graphics g) {
        for (int y = 0; y < getHeight(); y += BACKGROUND_TILE_HEIGHT) {
            for (int x = 0; x < getWidth(); x += BACKGROUND_TILE_WIDTH) {
                g.drawImage(backgroundImage.getImage(), x, y,
                        BACKGROUND_TILE_WIDTH, BACKGROUND_TILE_HEIGHT, null);
            }
        }

        for (int x = 0; x < getWidth(); x += MIDGROUND_WIDTH) {
            g.drawImage(midgroundImage.getImage(), x, 0,
                    MIDGROUND_WIDTH, MIDGROUND_HEIGHT, null);
        }
    }

    private void drawEntities(Graphics g) {
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }

        for (Bubble bubble : playerBubbles) {
            bubble.draw(g);
        }

        for (EnemyProjectile projectile : enemyProjectiles) {
            projectile.draw(g);
        }

        for (Explosion explosion : explosions) {
            explosion.draw(g);
        }

        player.draw(g);
    }

    private void drawBossHealth(Graphics g) {
        GameHud.drawBossHealth(g, getWidth(), boss.getHealth(),
                BOSS_MAX_HEALTH, boss.getAttackName());
    }

    private void drawHud(Graphics g) {
        GameHud.draw(g, getWidth(), player, runState,
                "STAGE 3", -1);
    }

    private void drawPauseOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, scaledFontSize(38)));
        drawCentered(g, "PAUSED", 250);
        g.setFont(new Font("SansSerif", Font.PLAIN, scaledFontSize(20)));
        drawCentered(g, "[ESC] Resume", 330);
        drawCentered(g, "[R] Restart Game", 366);
        drawCentered(g, "[M] Main Menu", 402);
        drawCentered(g, "[Q] Quit", 438);
    }

    private void drawCentered(Graphics g, String text, int y) {
        int x = (getWidth() - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private class SceneInput extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                paused = !paused;
                return;
            }

            if (paused) {
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_R -> game.startNewGame();
                    case KeyEvent.VK_M -> game.loadTitle();
                    case KeyEvent.VK_Q -> game.quit();
                    default -> {}
                }
                return;
            }

            player.keyPressed(event);
        }

        @Override
        public void keyReleased(KeyEvent event) {
            player.keyReleased(event);
        }
    }
}
