package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.RunState;
import gdd.SpawnManager;
import gdd.TransitionMode;
import gdd.powerup.PowerUp;
import gdd.sprite.Bubble;
import gdd.sprite.Enemy;
import gdd.sprite.EnemyProjectile;
import gdd.sprite.Explosion;
import gdd.sprite.Mine;
import gdd.sprite.Octopus;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel implements GameScene {

    private Game game;
    private RunState runState;
    private List<Enemy> enemies = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Bubble> playerBubbles = new ArrayList<>();
    private List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private List<Mine> mines = new ArrayList<>();

    private Player player;
    private SpawnManager spawnManager;

    /// Timer in ticks/frames for how long the player has been in this stage
    private int stageTick;

    private KeyAdapter input = new SceneInput();
    private Timer timer;
    private int backgroundOffsetNear;
    private int backgroundOffsetFar;
    private boolean paused;
    private boolean finished;
    private boolean transitioning;
    private int transitionTicks;
    private int playerDeathTicks; // for death animation

    public Scene1(Game game, RunState runState) {
        this.game = game;
        this.runState = runState;
        setFocusable(true);
        setBackground(new Color(4, 42, 70));
    }

    @Override
    public void start() {
        resetStage();
        addKeyListener(input);
        timer = new Timer(TIMER_DELAY_MS, event -> gameTick(event)); // check gameTick method for more info
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
        stageTick = 0;
        paused = false;
        finished = false;
        transitioning = false;
        playerDeathTicks = 0;
        player = new Player(runState);
        spawnManager = new SpawnManager(player, 1);
        spawnManager.setMode(DEFAULT_SPAWN_MODE);
        loadPlaceholderStageContent();
    }

    // TODO: parallax bg (or the other way around??)
    private void loadPlaceholderStageContent() {
        mines.add(new Mine(BOARD_WIDTH + 120, 180));
        mines.add(new Mine(BOARD_WIDTH + 520, 460));
    }

    /* this is basically equal to doGameCycle() inside a GameCycle class like this

    timer = new Timer(TIMER_DELAY_MS, new GameCycle());
    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            gameTick(event);
        }
    }
     */
    private void gameTick(ActionEvent event) {
        if (!paused && !finished) { // handling a pause
            updateGame();
        }
        repaint();
    }

    // equals to update() used by the prof
    void updateGame() {
        // only active when player is dying
        if (playerDeathTicks > 0) {
            updatePlayerDeath();
            return;
        }

        if (transitioning) {
            updateTransition();
            return;
        }

        stageTick++;

        updateBackgroundScroll();
        updatePlayer();

        spawnManager.update(stageTick, enemies, powerUps);

        spawnPlaceholderObstacles();
        updateEnemies();
        updateProjectiles();
        updatePowerUps();
        updateObstacles();
        updateExplosions();
        advanceSpriteAnimations();
        handleCollisions();
        removeDeadEntities();

        if (player.isDead()) {
            player.setDying(true);
            playerDeathTicks = PLAYER_DEATH_TICKS;
//            enemyProjectiles.clear();  // why remove this? cuz like keeping it makes it look more realistic?
            return;
        }

        // stage/scene ends when it reaches the end of the timer
        if (stageTick >= stageDurationTicks()) {
            completeStage();
        }
    }

    private int getRemainingTicks() {
        return Math.max(0, stageDurationTicks() - stageTick);
    }

    // parallax is implemented here
    private void updateBackgroundScroll() {
        // The near and far background layers are moving at different speeds.
        // These offsets are changing here, and gameTick() is repainting afterward.
        backgroundOffsetNear = (backgroundOffsetNear + WORLD_SCROLL_SPEED) % 90;
        backgroundOffsetFar = (backgroundOffsetFar + 1) % 150;
    }

    private void updatePlayerDeath() {
        // Normal gameplay is staying paused while the death delay is running.
        // The background is continuing to scroll during this delay.
        updateBackgroundScroll();

        // Existing explosions are continuing their animations, and finished explosions are being removed.
        for (Explosion explosion : explosions) {
            explosion.act();
        }
        explosions.removeIf(explosion -> !explosion.isVisible());

        // death counter countdown
        playerDeathTicks--;

        // The game-over screen opens when the timer is reaches zero.
        if (playerDeathTicks <= 0) {
            finishAsGameOver();
        }
    }

    private void updateTransition() {
        updateBackgroundScroll();
        player.act();

        transitionTicks--;
        if (transitionTicks <= 0) {
            moveToNextStage();
        }
    }

    private void updatePlayer() {
        player.act();
        playerBubbles.addAll(player.createBubbles());
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            if (!enemy.isVisible()) {
                continue;
            }

            enemy.act();

            if (enemy instanceof Octopus) {
                Octopus octopus = (Octopus) enemy;
                EnemyProjectile rock = octopus.shootRockIfReady();

                if (rock != null) {
                    enemyProjectiles.add(rock);
                }
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

    private void updatePowerUps() {
        for (PowerUp powerUp : powerUps) {
            powerUp.act();
        }
    }

    private void updateObstacles() {
        for (Mine mine : mines) {
            mine.act();
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

        for (PowerUp powerUp : powerUps) {
            powerUp.advanceAnimation();
        }

        for (Bubble bubble : playerBubbles) {
            bubble.advanceAnimation();
        }

        for (EnemyProjectile projectile : enemyProjectiles) {
            projectile.advanceAnimation();
        }

        for (Mine mine : mines) {
            mine.advanceAnimation();
        }

        for (Explosion explosion : explosions) {
            explosion.advanceAnimation();
        }
    }

    private void spawnPlaceholderObstacles() {
        if (stageTick > 0 && stageTick % secondsToTicks(9) == 0) {
            int y = 90 + (stageTick / secondsToTicks(9) * 97)
                    % (BOARD_HEIGHT - 190);
            mines.add(new Mine(BOARD_WIDTH + 40, y));
        }
    }

    private void handleCollisions() {
        handlePlayerBubbleCollisions();
        handleEnemyProjectileCollisions();
        handleEnemyContact();
        handlePowerUpContact();
        handleObstacleContact();
    }

    private void handlePlayerBubbleCollisions() {
        for (Bubble bubble : playerBubbles) {
            if (!bubble.isVisible()) {
                continue;
            }

            // check collisions with enemies
            for (Enemy enemy : enemies) {
                if (bubble.collidesWith(enemy)) {
                    boolean killed = enemy.damage(bubble.getDamage());
                    bubble.die();
                    if (killed) {
                        runState.addScore(enemy.getScoreValue());
                        addSmallExplosion(enemy);
                    }
                    break;
                }
            }

            if (!bubble.isVisible()) {
                continue;
            }

            // check collision with mines
            for (Mine mine : mines) {
                if (bubble.collidesWith(mine)) {
                    bubble.die();
                    triggerMine(mine);
                    break;
                }
            }
        }
    }

    private void handleEnemyProjectileCollisions() {
        for (EnemyProjectile projectile : enemyProjectiles) {
            if (!projectile.isVisible()) {
                continue;
            }

            if (projectile.collidesWith(player)) {
                player.damage(projectile.getDamage());
                projectile.die();
                continue;
            }

            for (Mine mine : mines) {
                if (projectile.collidesWith(mine)) {
                    projectile.die();
                    triggerMine(mine);
                    break;
                }
            }

        }
    }

    private void handleEnemyContact() {
        for (Enemy enemy : enemies) {
            if (enemy.collidesWith(player)) {
                player.damage(enemy.getContactDamage());
            }

            for (Mine mine : mines) {
                if (enemy.collidesWith(mine)) {
                    triggerMine(mine);
                }
            }
        }
    }

    private void handlePowerUpContact() {
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isVisible() && powerUp.collidesWith(player)) {
                powerUp.upgrade(player);
            }
        }
    }

    private void handleObstacleContact() {
        for (Mine mine : mines) {
            if (mine.collidesWith(player)) {
                triggerMine(mine);
            }
        }
    }

    private void triggerMine(Mine firstMine) {
        Queue<Mine> queue = new ArrayDeque<>();
        queue.add(firstMine);

        while (!queue.isEmpty()) {
            Mine mine = queue.remove();
            if (!mine.trigger()) {
                continue;
            }

            int centerX = mine.getCenterX();
            int centerY = mine.getCenterY();
            explosions.add(new Explosion(centerX, centerY, MINE_EXPLOSION_RADIUS));

            if (distanceTo(centerX, centerY, player) <= MINE_EXPLOSION_RADIUS) {
                player.damage(MINE_DAMAGE);
            }

            for (Enemy enemy : enemies) {
                if (!enemy.isVisible()) {
                    continue;
                }

                boolean insideExplosion = distanceTo(centerX, centerY, enemy) <= MINE_EXPLOSION_RADIUS;

                if (!insideExplosion) {
                    continue;
                }

                boolean killed = enemy.damage(MINE_DAMAGE);

                if (killed) {
                    addSmallExplosion(enemy);
                }
            }

            for (Mine nearby : mines) {
                if (nearby.isVisible()
                        && distanceTo(centerX, centerY, nearby)
                        <= MINE_EXPLOSION_RADIUS) {
                    queue.add(nearby);
                }
            }
        }
    }

    private double distanceTo(int x, int y, gdd.sprite.Sprite sprite) {
        double centerX = sprite.getX() + sprite.getRenderWidth() / 2.0;
        double centerY = sprite.getY() + sprite.getRenderHeight() / 2.0;
        return Math.hypot(centerX - x, centerY - y);
    }

    private void addSmallExplosion(gdd.sprite.Sprite sprite) {
        if (sprite.isDying()) {
            return;
        }

        explosions.add(new Explosion(
                sprite.getX() + sprite.getRenderWidth() / 2,
                sprite.getY() + sprite.getRenderHeight() / 2));
    }

    private void removeDeadEntities() {
        enemies.removeIf(enemy -> !enemy.isVisible());
        powerUps.removeIf(powerUp -> !powerUp.isVisible());
        playerBubbles.removeIf(bubble -> !bubble.isVisible());
        enemyProjectiles.removeIf(projectile -> !projectile.isVisible());
        explosions.removeIf(explosion -> !explosion.isVisible());
        mines.removeIf(mine -> !mine.isVisible());
    }

    private void completeStage() {
        if (SCENE_TRANSITION_MODE == TransitionMode.SEAMLESS && !transitioning) {
            clearStageEntities();
            transitioning = true;
            transitionTicks = SEAMLESS_TRANSITION_TICKS;
            return;
        }

        moveToNextStage();
    }

    private void moveToNextStage() {
        finished = true;
        player.syncTo(runState);
        game.loadScene2();
    }

    private void finishAsGameOver() {
        finished = true;
        player.syncTo(runState);
        game.showGameOver();
    }

    private void clearStageEntities() {
        enemies.clear();
        powerUps.clear();
        playerBubbles.clear();
        enemyProjectiles.clear();
        explosions.clear();
        mines.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackground(g);
        drawTransition(g);
        drawEntities(g);
        drawHud(g);

        if (paused) {
            drawPauseOverlay(g);
        }
    }

    private void drawTransition(Graphics g) {
        if (!transitioning) {
            return;
        }

        double progress = 1.0 - transitionTicks / (double) SEAMLESS_TRANSITION_TICKS;
        int nextBackgroundX = (int) Math.round(BOARD_WIDTH - progress * BOARD_WIDTH);
        g.setColor(new Color(3, 34, 62));
        g.fillRect(nextBackgroundX, 42, BOARD_WIDTH - nextBackgroundX, BOARD_HEIGHT - 42);
    }

    private void drawBackground(Graphics g) {
        g.setColor(new Color(4, 56, 88));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(30, 105, 135, 130));
        for (int x = -backgroundOffsetFar; x < getWidth(); x += 150) {
            g.fillRect(x, 90 + (x & 63), 38, 18);
        }

        g.setColor(new Color(95, 195, 215, 150));
        for (int x = -backgroundOffsetNear; x < getWidth(); x += 90) {
            g.drawOval(x, 150 + Math.abs(x % 240), 12, 12);
            g.drawOval(x + 20, 180 + Math.abs(x % 170), 6, 6);
        }
    }

    private void drawEntities(Graphics g) {
        for (Mine mine : mines) {
            mine.draw(g);
        }

        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g);
        }

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

    private void drawHud(Graphics g) {
        g.setColor(new Color(0, 15, 28, 210));
        g.fillRect(0, 0, getWidth(), 42);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, scaledFontSize(13)));

        int remainingTicks = getRemainingTicks();
        String timerText = String.format("%d:%02d",
                remainingTicks / TARGET_FPS / 60,
                remainingTicks / TARGET_FPS % 60);

        g.drawString("STAGE 1", 12, 18);
        g.drawString("TIME " + timerText, 12, 36);
        g.drawString("SCORE " + runState.getScore(), 125, 18);
        g.drawString("HP " + player.getHealth() + "/" + PLAYER_MAX_HEALTH,
                125, 36);
        g.drawString("SPEED " + player.getSpeedLevel() + "/2", 285, 18);
        g.drawString("SPD " + ticksAsSeconds(player.getSpeedPowerupTicks()),
                285, 36);
        g.drawString(player.getWeaponType().getDisplayName(), 430, 18);
        g.drawString("LV " + player.getMultiShotLevel()
                + "  " + ticksAsSeconds(player.getWeaponPowerupTicks()),
                430, 36);
    }

    private String ticksAsSeconds(int ticks) {
        if (ticks <= 0) {
            return "--";
        }
        return String.format("%.1fs", ticks / (double) TARGET_FPS);
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
            // kinda like a toggle logic
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                paused = !paused;
                return;
            }

            // for pause menu
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
