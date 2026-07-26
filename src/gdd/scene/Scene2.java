package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.RunState;
import gdd.TransitionMode;
import gdd.level.LevelLoader;
import gdd.level.TileMap;
import gdd.powerup.PowerUp;
import gdd.spawn.SpawnDetails;
import gdd.spawn.SpawnManager;
import gdd.spawn.SpawnMode;
import gdd.sprite.Bubble;
import gdd.sprite.obstacle.Coral;
import gdd.sprite.enemy.Enemy;
import gdd.sprite.enemy.EnemyProjectile;
import gdd.sprite.obstacle.Explosion;
import gdd.sprite.enemy.Octopus;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene2 extends JPanel implements GameScene {

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
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Bubble> playerBubbles = new ArrayList<>();
    private List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private List<Coral> corals = new ArrayList<>();
    private List<Rectangle> walls = new ArrayList<>();

    private Player player;
    private SpawnManager spawnManager;
    private TileMap tileMap;

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

    public Scene2(Game game, RunState runState) {
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
        Map<Integer, List<SpawnDetails>> scriptedSpawns = Map.of();
        if (DEFAULT_SPAWN_MODE == SpawnMode.SCRIPTED) {
            scriptedSpawns = LevelLoader.loadEvents(
                    SCENE2_EVENTS_PATH);
            tileMap = new TileMap(LevelLoader.loadTerrain(
                    SCENE2_TERRAIN_PATH));
        } else {
            tileMap = null;
            loadPlaceholderStageContent();
        }
        spawnManager = new SpawnManager(player, 2,
                DEFAULT_SPAWN_MODE, scriptedSpawns);
    }

    private void loadPlaceholderStageContent() {
        corals.add(new Coral(BOARD_WIDTH + 170,
                BOARD_HEIGHT - CORAL_HEIGHT - 42));
        walls.add(new Rectangle(BOARD_WIDTH + 520, 0, 130, 85));
        walls.add(new Rectangle(BOARD_WIDTH + 850,
                BOARD_HEIGHT - 130, 150, 100));
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

        spawnScriptedWorldEvents(spawnManager.update(
                stageTick, enemies, powerUps));

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
        backgroundOffsetNear
                = (backgroundOffsetNear + WORLD_SCROLL_SPEED) % MIDGROUND_WIDTH;
        backgroundOffsetFar
                = (backgroundOffsetFar + 1) % BACKGROUND_TILE_WIDTH;
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

        if (intersectsTerrain(player.getBounds())) {
            player.restorePosition(oldX, oldY);
            if (WALL_DAMAGE_ENABLED) {
                player.damage(WALL_DAMAGE);
            }
        } else if (intersectsWall(player.getBounds())) {
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

        for (EnemyProjectile projectile : enemyProjectiles) {
            projectile.advanceAnimation();
        }

        for (Coral coral : corals) {
            coral.advanceAnimation();
        }

        for (Explosion explosion : explosions) {
            explosion.advanceAnimation();
        }
    }

    private void spawnPlaceholderObstacles() {
        if (DEFAULT_SPAWN_MODE == SpawnMode.SCRIPTED) {
            return;
        }

        if (stageTick > 0 && stageTick % secondsToTicks(11) == 0) {
            corals.add(new Coral(BOARD_WIDTH + 40,
                    BOARD_HEIGHT - CORAL_HEIGHT - 42));
        }
    }

    private void spawnScriptedWorldEvents(
            List<SpawnDetails> worldEvents) {
        for (SpawnDetails event : worldEvents) {
            if (!event.type.equals("Coral")) {
                throw new IllegalArgumentException(
                        "Scene 2 does not support event type: " + event.type);
            }
            corals.add(new Coral(event.x, event.y));
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

            // check collision with corals
            for (Coral coral : corals) {
                if (bubble.collidesWith(coral)) {
                    coral.damage();
                    bubble.die();
                    break;
                }
            }

            // bubble pops if it touches a wall
            if (bubble.isVisible()
                    && (intersectsTerrain(bubble.getBounds())
                    || intersectsWall(bubble.getBounds()))) {
                bubble.die();
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

            if (projectile.isVisible()
                    && (intersectsTerrain(projectile.getBounds())
                    || intersectsWall(projectile.getBounds()))) {
                projectile.die();
            }
        }
    }

    private void handleEnemyContact() {
        for (Enemy enemy : enemies) {
            if (enemy.collidesWith(player)) {
                player.damage(enemy.getContactDamage());
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
        for (Coral coral : corals) {
            if (coral.collidesWith(player)) {
                player.damage(coral.contactDamage);
                coral.damage();
            }
        }
    }

    private void addSmallExplosion(gdd.sprite.Sprite sprite) {
        if (sprite.isDying()) {
            return;
        }

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

    private boolean intersectsTerrain(Rectangle bounds) {
        return tileMap != null && tileMap.intersects(bounds, stageTick);
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
        corals.removeIf(coral -> !coral.isVisible());
        walls.removeIf(wall -> wall.x + wall.width < 0);
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
        game.loadBossScene();
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
        corals.clear();
        walls.clear();
        tileMap = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackground(g);
        if (tileMap != null) {
            tileMap.draw(g, stageTick);
        }
        drawTransition(g);
        drawWalls(g);
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
        Graphics transitionGraphics = g.create();
        transitionGraphics.clipRect(nextBackgroundX, 42,
                BOARD_WIDTH - nextBackgroundX, BOARD_HEIGHT - 42);

        for (int y = 0; y < getHeight(); y += BACKGROUND_TILE_HEIGHT) {
            for (int x = nextBackgroundX;
                    x < getWidth(); x += BACKGROUND_TILE_WIDTH) {
                transitionGraphics.drawImage(backgroundImage.getImage(), x, y,
                        BACKGROUND_TILE_WIDTH, BACKGROUND_TILE_HEIGHT, null);
            }
        }

        for (int x = nextBackgroundX;
                x < getWidth(); x += MIDGROUND_WIDTH) {
            transitionGraphics.drawImage(midgroundImage.getImage(), x, 0,
                    MIDGROUND_WIDTH, MIDGROUND_HEIGHT, null);
        }

        transitionGraphics.dispose();
    }

    private void drawBackground(Graphics g) {
        for (int y = 0; y < getHeight(); y += BACKGROUND_TILE_HEIGHT) {
            for (int x = -backgroundOffsetFar;
                    x < getWidth(); x += BACKGROUND_TILE_WIDTH) {
                g.drawImage(backgroundImage.getImage(), x, y,
                        BACKGROUND_TILE_WIDTH, BACKGROUND_TILE_HEIGHT, null);
            }
        }

        for (int x = -backgroundOffsetNear;
                x < getWidth(); x += MIDGROUND_WIDTH) {
            g.drawImage(midgroundImage.getImage(), x, 0,
                    MIDGROUND_WIDTH, MIDGROUND_HEIGHT, null);
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

        g.drawString("STAGE 2", 12, 18);
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
