package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.RunState;
import gdd.SpawnManager;
import gdd.TransitionMode;
import gdd.powerup.PowerUp;
import gdd.sprite.Bubble;
import gdd.sprite.Coral;
import gdd.sprite.Enemy;
import gdd.sprite.EnemyRock;
import gdd.sprite.Explosion;
import gdd.sprite.Mine;
import gdd.sprite.Octopus;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel implements GameScene {

    protected final Game game;
    protected final RunState runState;
    protected final int stageNumber;
    protected final List<Enemy> enemies = new ArrayList<>();
    protected final List<PowerUp> powerUps = new ArrayList<>();
    protected final List<Bubble> playerBubbles = new ArrayList<>();
    protected final List<EnemyRock> enemyProjectiles = new ArrayList<>();
    protected final List<Explosion> explosions = new ArrayList<>();
    protected final List<Mine> mines = new ArrayList<>();
    protected final List<Coral> corals = new ArrayList<>();
    protected final List<Rectangle> walls = new ArrayList<>();

    protected Player player;
    protected SpawnManager spawnManager;

    /// Timer in ticks/frames for how long the player has been in this stage
    protected int stageTick;

    private final KeyAdapter input = new SceneInput();
    private Timer timer;
    private int backgroundOffsetNear;
    private int backgroundOffsetFar;
    private boolean paused;
    private boolean finished;
    private boolean transitioning;
    private int transitionTicks;
    private int playerDeathTicks; // for death animation

    public Scene1(Game game, RunState runState) {
        this(game, runState, 1);
    }

    protected Scene1(Game game, RunState runState, int stageNumber) {
        this.game = game;
        this.runState = runState;
        this.stageNumber = stageNumber;
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
        if (timer != null) {
            timer.stop();
        }
        clearSceneEntities();
    }

    private void resetStage() {
        clearSceneEntities();
        stageTick = 0;
        paused = false;
        finished = false;
        transitioning = false;
        playerDeathTicks = 0;
        player = new Player(runState);

        // only spawn enemies in stage 1 or 2
        if (stageNumber <= 2) {
            spawnManager = new SpawnManager(player, stageNumber);
            spawnManager.setMode(DEFAULT_SPAWN_MODE);
        }

        loadPlaceholderStageContent();
        setupSpecialStageContent();
    }

    // TODO: obstacles
    protected void setupSpecialStageContent() {
    }

    // TODO: parallax bg (or the other way around??)
    protected void loadPlaceholderStageContent() {
        if (stageNumber == 1) {
            mines.add(new Mine(BOARD_WIDTH + 120, 180));
            mines.add(new Mine(BOARD_WIDTH + 520, 460));
        } else if (stageNumber == 2) {
            corals.add(new Coral(BOARD_WIDTH + 170,
                    BOARD_HEIGHT - CORAL_HEIGHT - 42));
            walls.add(new Rectangle(BOARD_WIDTH + 520, 0, 130, 85));
            walls.add(new Rectangle(BOARD_WIDTH + 850,
                    BOARD_HEIGHT - 130, 150, 100));
        }
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
    protected void updateGame() {
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

        if (spawnManager != null) {
            spawnManager.update(stageTick, enemies, powerUps);
        }

        spawnPlaceholderObstacles();
        updateEnemies();
        updateProjectiles();
        updatePowerUps();
        updateObstacles();
        updateExplosions();
        advanceSpriteAnimations();
        updateSpecialStage();
        handleCollisions();
        removeDeadEntities();

        if (player.isDead()) {
            player.setDying(true);
            playerDeathTicks = PLAYER_DEATH_TICKS;
            enemyProjectiles.clear();
            return;
        }

        // stage/scene ends when it reaches the end of the timer
        if (usesStageTimer() && stageTick >= stageDurationTicks()) {
            completeStage();
        }
    }

    protected void updateSpecialStage() {
    }

    // only stage 1 and 2 uses stage timer, cuz boss fight is not timed
    protected boolean usesStageTimer() {
        return stageNumber <= 2;
    }

    private int getRemainingTicks() {
        return Math.max(0, stageDurationTicks() - stageTick);
    }

    // parallax is implemented here
    private void updateBackgroundScroll() {
        // The boss background is staying still during the boss fight.
        if (stageNumber == 3) {
            return;
        }

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
        int oldX = player.getX();
        int oldY = player.getY();
        player.act();

        if (intersectsWall(player.getBounds())) {
            player.restorePosition(oldX, oldY);
            resolveWallOverlap();
            if (WALL_DAMAGE_ENABLED) {
                player.damage(WALL_DAMAGE);
            }
        }

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
                EnemyRock rock = octopus.takeRock();

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

        for (EnemyRock projectile : enemyProjectiles) {
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

        for (Coral coral : corals) {
            coral.act();
        }

        for (Rectangle wall : walls) {
            wall.x -= WORLD_SCROLL_SPEED;
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

        for (EnemyRock projectile : enemyProjectiles) {
            projectile.advanceAnimation();
        }

        for (Mine mine : mines) {
            mine.advanceAnimation();
        }

        for (Coral coral : corals) {
            coral.advanceAnimation();
        }

        for (Explosion explosion : explosions) {
            explosion.advanceAnimation();
        }
    }

    private void spawnPlaceholderObstacles() {
        if (stageNumber == 1 && stageTick > 0
                && stageTick % secondsToTicks(9) == 0) {
            int y = 90 + (stageTick / secondsToTicks(9) * 97)
                    % (BOARD_HEIGHT - 190);
            mines.add(new Mine(BOARD_WIDTH + 40, y));
        }

        if (stageNumber == 2 && stageTick > 0
                && stageTick % secondsToTicks(11) == 0) {
            corals.add(new Coral(BOARD_WIDTH + 40,
                    BOARD_HEIGHT - CORAL_HEIGHT - 42));
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

            for (Enemy enemy : enemies) {
                if (bubble.collidesWith(enemy)) {
                    boolean killed = enemy.damage(bubble.getDamage());
                    bubble.die();
                    if (killed) {
                        runState.addScore(enemy.getScoreValue());
                        addSmallExplosion(enemy);
                        onEnemyKilled(enemy);
                    }
                    break;
                }
            }

            if (!bubble.isVisible()) {
                continue;
            }

            for (Coral coral : corals) {
                if (bubble.collidesWith(coral)) {
                    coral.damage();
                    bubble.die();
                    break;
                }
            }

            if (!bubble.isVisible()) {
                continue;
            }

            for (Mine mine : new ArrayList<>(mines)) {
                if (bubble.collidesWith(mine)) {
                    bubble.die();
                    triggerMine(mine);
                    break;
                }
            }

            if (bubble.isVisible() && intersectsWall(bubble.getBounds())) {
                bubble.die();
            }
        }
    }

    protected void onEnemyKilled(Enemy enemy) {
    }

    private void handleEnemyProjectileCollisions() {
        for (EnemyRock projectile : enemyProjectiles) {
            if (!projectile.isVisible()) {
                continue;
            }

            if (projectile.collidesWith(player)) {
                player.damage(projectile.getDamage());
                projectile.die();
                continue;
            }

            for (Mine mine : new ArrayList<>(mines)) {
                if (projectile.collidesWith(mine)) {
                    projectile.die();
                    triggerMine(mine);
                    break;
                }
            }

            if (projectile.isVisible() && intersectsWall(projectile.getBounds())) {
                projectile.die();
            }
        }
    }

    private void handleEnemyContact() {
        for (Enemy enemy : enemies) {
            if (enemy.collidesWith(player)) {
                player.damage(enemy.getContactDamage());
            }

            for (Mine mine : new ArrayList<>(mines)) {
                if (enemy.collidesWith(mine)) {
                    triggerMine(mine);
                }
            }
        }
    }

    private void handlePowerUpContact() {
        for (PowerUp powerUp : powerUps) {
            for (Mine mine : new ArrayList<>(mines)) {
                if (powerUp.collidesWith(mine)) {
                    powerUp.die();
                    triggerMine(mine);
                    break;
                }
            }

            if (!powerUp.isVisible()) {
                continue;
            }

            if (powerUp.collidesWith(player)) {
                powerUp.upgrade(player);
            }
        }
    }

    private void handleObstacleContact() {
        for (Mine mine : new ArrayList<>(mines)) {
            if (mine.collidesWith(player)) {
                triggerMine(mine);
            }
        }

        for (Coral coral : corals) {
            if (coral.collidesWith(player)) {
                player.damage(1);
                coral.damage();
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
                if (enemy.isVisible()
                        && distanceTo(centerX, centerY, enemy)
                        <= MINE_EXPLOSION_RADIUS) {
                    if (enemy.damage(MINE_DAMAGE)) {
                        addSmallExplosion(enemy);
                        onEnemyKilledByMine(enemy);
                    }
                }
            }

            for (Coral coral : corals) {
                if (coral.isVisible()
                        && distanceTo(centerX, centerY, coral)
                        <= MINE_EXPLOSION_RADIUS) {
                    coral.damage();
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

    protected void onEnemyKilledByMine(Enemy enemy) {
    }

    private double distanceTo(int x, int y, gdd.sprite.Sprite sprite) {
        double centerX = sprite.getX() + sprite.getRenderWidth() / 2.0;
        double centerY = sprite.getY() + sprite.getRenderHeight() / 2.0;
        return Math.hypot(centerX - x, centerY - y);
    }

    private void addSmallExplosion(gdd.sprite.Sprite sprite) {
        explosions.add(new Explosion(
                sprite.getX() + sprite.getRenderWidth() / 2,
                sprite.getY() + sprite.getRenderHeight() / 2));
    }

    private boolean intersectsWall(Rectangle bounds) {
        for (Rectangle wall : walls) {
            if (bounds.intersects(wall)) {
                return true;
            }
        }
        return false;
    }

    private void resolveWallOverlap() {
        Rectangle playerBounds = player.getBounds();
        for (Rectangle wall : walls) {
            if (!playerBounds.intersects(wall)) {
                continue;
            }

            Rectangle overlap = playerBounds.intersection(wall);
            boolean horizontalOverlapIsSmaller = overlap.width < overlap.height;

            // Resolve along the smaller overlap to use the shortest correction.
            if (horizontalOverlapIsSmaller) {
                boolean playerIsLeftOfWall = playerBounds.getCenterX() < wall.getCenterX();

                // Push the player left or right based on its side of the wall.
                if (playerIsLeftOfWall) {
                    player.setX(player.getX() - overlap.width);
                } else {
                    player.setX(player.getX() + overlap.width);
                }
            } else {
                boolean playerIsAboveWall = playerBounds.getCenterY() < wall.getCenterY();

                // Push the player up or down based on its side of the wall.
                if (playerIsAboveWall) {
                    player.setY(player.getY() - overlap.height);
                } else {
                    player.setY(player.getY() + overlap.height);
                }
            }

            // Refresh the hitbox before checking the next wall.
            playerBounds = player.getBounds();
        }
    }

    private void removeDeadEntities() {
        enemies.removeIf(enemy -> !enemy.isVisible());
        powerUps.removeIf(powerUp -> !powerUp.isVisible());
        playerBubbles.removeIf(bubble -> !bubble.isVisible());
        enemyProjectiles.removeIf(projectile -> !projectile.isVisible());
        explosions.removeIf(explosion -> !explosion.isVisible());
        mines.removeIf(mine -> !mine.isVisible());
        corals.removeIf(coral -> !coral.isVisible());

        Iterator<Rectangle> wallsIterator = walls.iterator();
        while (wallsIterator.hasNext()) {
            Rectangle wall = wallsIterator.next();
            if (wall.x + wall.width < 0) {
                wallsIterator.remove();
            }
        }
    }

    protected void completeStage() {
        if (finished) {
            return;
        }

        if (SCENE_TRANSITION_MODE == TransitionMode.SEAMLESS && !transitioning) {
            player.clearTemporaryPowerups();
            clearNonPlayerEntities();
            transitioning = true;
            transitionTicks = SEAMLESS_TRANSITION_TICKS;
            return;
        }

        moveToNextStage();
    }

    private void moveToNextStage() {
        finished = true;
        player.clearTemporaryPowerups();
        player.syncTo(runState);

        if (stageNumber == 1) {
            game.loadScene2();
        } else if (stageNumber == 2) {
            game.loadBossScene();
        }
    }

    protected void finishAsGameOver() {

        finished = true;
        player.syncTo(runState);
        game.showGameOver();
    }

    protected void finishAsVictory() {

        finished = true;
        player.syncTo(runState);
        game.showVictory();
    }

    private void clearSceneEntities() {
        clearNonPlayerEntities();
    }

    /// Although it says clear seen entities, it does not clear every entity.
    /// It only clears non-player entities because the player attributes get copied into run state to be moved to another scene.
    /// Somehow this is relevant to seamless transition logic
    private void clearNonPlayerEntities() {
        enemies.clear();
        powerUps.clear();
        playerBubbles.clear();
        enemyProjectiles.clear();
        explosions.clear();
        mines.clear();
        corals.clear();
        walls.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackground(g);
        drawTransition(g);
        drawWalls(g);
        drawEntities(g);
        drawSpecialStage(g);
        drawHud(g);

        if (paused) {
            drawPauseOverlay(g);
        }
    }

    protected void drawSpecialStage(Graphics g) {
    }

    private void drawTransition(Graphics g) {
        if (!transitioning) {
            return;
        }

        double progress = 1.0 - transitionTicks
                / (double) SEAMLESS_TRANSITION_TICKS;
        int nextBackgroundX = (int) Math.round(
                BOARD_WIDTH - progress * BOARD_WIDTH);
        Color nextColor = stageNumber == 1
                ? new Color(3, 34, 62)
                : new Color(3, 24, 45);
        g.setColor(nextColor);
        g.fillRect(nextBackgroundX, 42,
                BOARD_WIDTH - nextBackgroundX, BOARD_HEIGHT - 42);
    }

    private void drawBackground(Graphics g) {
        Color base = stageNumber == 1
                ? new Color(4, 56, 88)
                : new Color(3, 34, 62);
        g.setColor(base);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (stageNumber == 3) {
            g.setColor(new Color(10, 65, 76));
            g.fillRect(0, getHeight() - 115, getWidth(), 115);
            return;
        }

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

    private void drawWalls(Graphics g) {
        g.setColor(new Color(50, 70, 78));
        for (Rectangle wall : walls) {
            g.fillRect(wall.x, wall.y, wall.width, wall.height);
            g.setColor(new Color(90, 110, 115));
            g.drawRect(wall.x, wall.y, wall.width, wall.height);
            g.setColor(new Color(50, 70, 78));
        }
    }

    private void drawEntities(Graphics g) {
        for (Mine mine : mines) {
            mine.draw(g);
        }

        for (Coral coral : corals) {
            coral.draw(g);
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

        for (EnemyRock projectile : enemyProjectiles) {
            projectile.draw(g);
        }

        for (Explosion explosion : explosions) {
            explosion.draw(g);
        }

        if (player != null) {
            player.draw(g);
        }
    }

    private void drawHud(Graphics g) {
        g.setColor(new Color(0, 15, 28, 210));
        g.fillRect(0, 0, getWidth(), 42);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));

        int remainingTicks = getRemainingTicks();
        String timerText = usesStageTimer()
                ? String.format("%d:%02d",
                        remainingTicks / TARGET_FPS / 60,
                        remainingTicks / TARGET_FPS % 60)
                : "BOSS";

        g.drawString("STAGE " + stageNumber, 12, 18);
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
        g.setFont(new Font("SansSerif", Font.BOLD, 38));
        drawCentered(g, "PAUSED", 250);
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
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
            if (player != null) {
                player.keyReleased(event);
            }
        }
    }
}
