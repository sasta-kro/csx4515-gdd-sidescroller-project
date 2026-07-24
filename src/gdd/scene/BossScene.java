package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.RunState;
import gdd.sprite.Anglerfish;
import gdd.sprite.BomberFish;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class BossScene extends Scene1 {

    private Anglerfish boss;

    public BossScene(Game game, RunState runState) {
        super(game, runState, 3);
    }

    @Override
    protected void setupSpecialStageContent() {
        boss = new Anglerfish(player);
        enemies.add(boss);
    }

    @Override
    protected void updateSpecialStage() {
        if (boss == null) {
            return;
        }

        enemyProjectiles.addAll(boss.takePendingProjectiles());
        enemies.addAll(boss.takePendingSummons());

        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (enemy instanceof BomberFish) {
                BomberFish bomber = (BomberFish) enemy;
                if (bomber.isVisible() && bomber.shouldExplode()) {
                    explosions.add(new Explosion(
                            bomber.getX() + bomber.getRenderWidth() / 2,
                            bomber.getY() + bomber.getRenderHeight() / 2,
                            50));
                    player.damage(1);
                    bomber.die();
                }
            }
        }

        if (boss.isDeathFinished()) {
            finishAsVictory();
        }
    }

    @Override
    protected void onEnemyKilled(Enemy enemy) {
        if (enemy != boss) {
            return;
        }

        enemyProjectiles.clear();
        for (Enemy other : enemies) {
            if (other instanceof BomberFish) {
                other.die();
            }
        }
    }

    @Override
    protected void drawSpecialStage(Graphics2D g) {
        if (boss == null) {
            return;
        }

        int barX = 170;
        int barY = 52;
        int barWidth = 380;
        int healthWidth = (int) Math.round(
                barWidth * boss.getHealth() / (double) BOSS_MAX_HEALTH);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(barX - 3, barY - 3, barWidth + 6, 22);
        g.setColor(new Color(210, 70, 95));
        g.fillRect(barX, barY, healthWidth, 16);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, 16);
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.drawString("ANGLERFISH  " + boss.getHealth() + "/" + BOSS_MAX_HEALTH
                + "  " + boss.getAttackName(), barX, barY + 34);
    }
}
