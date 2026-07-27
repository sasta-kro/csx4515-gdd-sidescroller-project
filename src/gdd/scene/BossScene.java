package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.RunState;
import gdd.audio.SoundEffect;
import gdd.level.LevelLoader;
import gdd.level.TileMap;
import gdd.powerup.Heal;
import gdd.powerup.MegaShot;
import gdd.powerup.MultiShot;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.SplitShot;
import gdd.sprite.enemy.Anglerfish;
import gdd.sprite.enemy.BomberFish;
import gdd.sprite.Bubble;
import gdd.sprite.enemy.Enemy;
import gdd.sprite.enemy.EnemyProjectile;
import gdd.sprite.obstacle.Explosion;
import gdd.sprite.Player;
import gdd.ui.GameHud;
import gdd.ui.PauseMenu;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    private static final int PICKUP_INITIAL_DELAY_TICKS = secondsToTicks(3);
    private static final int PICKUP_MIN_INTERVAL_TICKS = secondsToTicks(6);
    private static final int PICKUP_MAX_INTERVAL_TICKS = secondsToTicks(10);
    private static final int PICKUP_SPAWN_X = BOARD_WIDTH + 20;
    private static final int PICKUP_MIN_Y = 125;
    private static final int PICKUP_MAX_Y = BOARD_HEIGHT - 270;
    private static final int LEFT_CORNER_COLUMNS = 1;
    private static final int RIGHT_CORNER_COLUMNS = 2;

    private Game game;
    private RunState runState;
    private List<Enemy> enemies = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Bubble> playerBubbles = new ArrayList<>();
    private List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private final Random random = new Random();

    private Player player;
    private Anglerfish boss;
    private TileMap tileMap;
    private KeyAdapter input = new SceneInput();
    private Timer timer;
    private boolean paused;
    private boolean finished;
    private int pickupSpawnTicks;

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
        pickupSpawnTicks = PICKUP_INITIAL_DELAY_TICKS;

        player = new Player(runState);
        tileMap = new TileMap(
                LevelLoader.loadTerrain(BOSS_TERRAIN_PATH));
        resolveTerrainOverlap();
        boss = new Anglerfish(player);
        Rectangle verticalBossBounds = tileMap.getVerticalOpenBounds(
                LEFT_CORNER_COLUMNS * TILE_SIZE,
                (tileMap.getColumns() - RIGHT_CORNER_COLUMNS) * TILE_SIZE);
        boss.setVerticalHitboxBounds(verticalBossBounds.y,
                verticalBossBounds.y + verticalBossBounds.height);
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
        if (player.isDying()) {
            updatePlayerDeath();
            return;
        }

        updatePlayer();
        updatePowerUps();
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
            player.startDeathAnimation();
        }
    }

    private void updatePlayerDeath() {
        player.advanceAnimation();

        // The fight stays frozen, but existing explosions finish animating.
        for (Explosion explosion : explosions) {
            explosion.act();
            explosion.advanceAnimation();
        }
        explosions.removeIf(explosion -> !explosion.isVisible());

        if (player.isDeathAnimationFinished()) {
            finishAsGameOver();
        }
    }

    private void updatePlayer() {
        player.act();
        resolveTerrainOverlap();
        playerBubbles.addAll(player.createBubbles());
        if (player.consumeShotStarted()) {
            playSound(SoundEffect.forWeapon(player.getWeaponType()));
        }
        if (player.consumePowerupExpired()) {
            playSound(SoundEffect.POWERUP_TIMEOUT);
        }
    }

    private void updatePowerUps() {
        if (!boss.isDying() && --pickupSpawnTicks <= 0) {
            powerUps.add(createRandomPowerUp());
            pickupSpawnTicks = randomBetween(
                    PICKUP_MIN_INTERVAL_TICKS,
                    PICKUP_MAX_INTERVAL_TICKS);
        }

        for (PowerUp powerUp : powerUps) {
            powerUp.act();
        }
    }

    private PowerUp createRandomPowerUp() {
        int y = randomBetween(PICKUP_MIN_Y, PICKUP_MAX_Y);
        return switch (random.nextInt(7)) { // weight of heal is 3/4
            case 0 -> new SpeedUp(PICKUP_SPAWN_X, y);
            case 1 -> new MultiShot(PICKUP_SPAWN_X, y);
            case 2 -> new MegaShot(PICKUP_SPAWN_X, y);
            case 3 -> new SplitShot(PICKUP_SPAWN_X, y);
            default -> new Heal(PICKUP_SPAWN_X, y);
        };
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act();
            }
        }

        if (boss.consumeLaserChargeStarted()) {
            playSound(SoundEffect.BOSS_LASER_CHARGE);
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

        for (PowerUp powerUp : powerUps) {
            powerUp.advanceAnimation();
        }

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
                    playSound(SoundEffect.EXPLOSION);
                    if (explosion.reaches(player)) {
                        damagePlayer(BOMBER_EXPLOSION_DAMAGE);
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
        handlePowerUpContact();
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

            if (bubble.isVisible()
                    && tileMap.intersects(bubble.getBounds(), 0)) {
                bubble.die();
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
                damagePlayer(projectile.getDamage());
                projectile.die();
                continue;
            }

            if (projectile.isVisible()
                    && tileMap.intersects(projectile.getBounds(), 0)) {
                projectile.die();
            }
        }
    }

    private void handleEnemyContact() {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible() && enemy.collidesWith(player)) {
                damagePlayer(enemy.getContactDamage());
            }
        }
    }

    private void handlePowerUpContact() {
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isVisible() && powerUp.collidesWith(player)) {
                powerUp.upgrade(player);
                playSound(SoundEffect.POWERUP_COLLECT);
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
        powerUps.removeIf(powerUp -> !powerUp.isVisible());
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
        powerUps.clear();
        playerBubbles.clear();
        enemyProjectiles.clear();
        explosions.clear();
        tileMap = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackground(g);
        drawEnemies(g);
        if (tileMap != null) {
            tileMap.draw(g, 0);
        }
        drawForegroundEntities(g);
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

    private void drawEnemies(Graphics g) {
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
    }

    private void drawForegroundEntities(Graphics g) {
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g);
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

    private void resolveTerrainOverlap() {
        Rectangle playerBounds = player.getBounds();
        Point correction = tileMap.getCollisionCorrection(
                playerBounds, getPlayerHitboxMovementArea(playerBounds), 0);
        player.setX(player.getX() + correction.x);
        player.setY(player.getY() + correction.y);
    }

    private Rectangle getPlayerHitboxMovementArea(
            Rectangle playerBounds) {
        int hitboxOffsetX = playerBounds.x - player.getX();
        int hitboxOffsetY = playerBounds.y - player.getY();
        return new Rectangle(
                hitboxOffsetX, hitboxOffsetY,
                BOARD_WIDTH - player.getRenderWidth() + playerBounds.width,
                BOARD_HEIGHT - 32 - player.getRenderHeight()
                        + playerBounds.height);
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
        PauseMenu.draw(g, getWidth(), getHeight());
    }

    private void playSound(SoundEffect soundEffect) {
        if (game != null) {
            game.playSound(soundEffect);
        }
    }

    private void damagePlayer(int amount) {
        if (player.damage(amount)) {
            playSound(SoundEffect.PLAYER_HURT);
        }
    }

    private class SceneInput extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                paused = !paused;
                if (game != null) {
                    game.setMusicPaused(paused);
                }
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
