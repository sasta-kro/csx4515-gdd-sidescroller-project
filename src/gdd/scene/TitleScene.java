package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TitleScene extends JPanel implements GameScene {

    private static final int FRAME_SIZE = 48;
    private static final int TITLE_FLASH_TICKS = 24;
    private static final int MIN_SPAWN_TICKS = 35;
    private static final int MAX_SPAWN_TICKS = 80;

    private static final String BACKGROUND_PATH =
            "src/images/background/scene2_bg/background.png";
    private static final String MIDGROUND_PATH =
            "src/images/background/scene2_bg/midground.png";

    private static final ImageIcon backgroundImage =
            new ImageIcon(BACKGROUND_PATH);
    private static final ImageIcon midgroundImage =
            new ImageIcon(MIDGROUND_PATH);
    private static final double MIDGROUND_SCALE =
            BOARD_HEIGHT / (double) midgroundImage.getIconHeight();
    private static final double BACKGROUND_SCALE = MIDGROUND_SCALE * 2;
    private static final int BACKGROUND_WIDTH = (int) Math.round(
            backgroundImage.getIconWidth() * BACKGROUND_SCALE);
    private static final int BACKGROUND_HEIGHT = (int) Math.round(
            backgroundImage.getIconHeight() * BACKGROUND_SCALE);
    private static final int MIDGROUND_WIDTH = (int) Math.round(
            midgroundImage.getIconWidth() * MIDGROUND_SCALE);
    private static final int MIDGROUND_HEIGHT = (int) Math.round(
            midgroundImage.getIconHeight() * MIDGROUND_SCALE);

    private static final SwimmerType[] SWIMMER_TYPES = {
        new SwimmerType(
                "src/images/enemies/jellyfish/Walk.png",
                72, 0.8, 1.3, 10),
        new SwimmerType(
                "src/images/enemies/turtle/Walk.png",
                108, 0.55, 0.9, 12),
        new SwimmerType(
                "src/images/enemies/octopus/Idle.png",
                82, 0.65, 1.0, 14),
        new SwimmerType(
                "src/images/enemies/swordfish/Walk.png",
                108, 1.15, 1.75, 8),
        new SwimmerType(
                "src/images/enemies/snake/Walk.png",
                84, 0.75, 1.15, 10)
    };

    private final Game game;
    private final KeyAdapter input = new TitleInput();
    private final Random random = new Random();
    private final List<TitleSwimmer> swimmers = new ArrayList<>();

    private Timer timer;
    private int animationTick;
    private int spawnTicks;
    private double backgroundOffset;
    private double midgroundOffset;

    public TitleScene(Game game) {
        this.game = game;
        setFocusable(true);
        setBackground(new Color(4, 32, 56));
        seedInitialSwimmers();
    }

    @Override
    public void start() {
        addKeyListener(input);
        timer = new Timer(TIMER_DELAY_MS, event -> updateTitle());
        timer.start();
        requestFocusInWindow();
    }

    @Override
    public void stop() {
        removeKeyListener(input);
        if (timer != null) {
            timer.stop();
        }
        swimmers.clear();
    }

    void updateTitle() {
        animationTick++;
        backgroundOffset = wrap(
                backgroundOffset + 0.10, BACKGROUND_WIDTH);
        midgroundOffset = wrap(
                midgroundOffset + 0.24, MIDGROUND_WIDTH);

        for (TitleSwimmer swimmer : swimmers) {
            swimmer.update();
        }
        swimmers.removeIf(swimmer ->
                swimmer.x > BOARD_WIDTH + swimmer.size);

        if (--spawnTicks <= 0) {
            spawnSwimmer(randomType(), -120 - random.nextInt(100));
            spawnTicks = randomBetween(MIN_SPAWN_TICKS, MAX_SPAWN_TICKS);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(graphics);
        drawSwimmers(graphics);
        drawTitleContent(graphics);
        graphics.dispose();
    }

    private void drawBackground(Graphics2D g) {
        drawScrollingLayer(g, backgroundImage.getImage(),
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT, backgroundOffset);
        drawScrollingLayer(g, midgroundImage.getImage(),
                MIDGROUND_WIDTH, MIDGROUND_HEIGHT, midgroundOffset);

        g.setColor(new Color(0, 12, 24, 45));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawScrollingLayer(Graphics2D g, Image image,
            int layerWidth, int layerHeight, double offset) {
        Graphics2D layerGraphics = (Graphics2D) g.create();
        layerGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        layerGraphics.translate(-offset, 0);

        for (int x = 0; x < getWidth() + layerWidth; x += layerWidth) {
            layerGraphics.drawImage(image, x, 0,
                    layerWidth, layerHeight, null);
        }

        layerGraphics.dispose();
    }

    private void drawSwimmers(Graphics2D g) {
        for (TitleSwimmer swimmer : swimmers) {
            swimmer.draw(g);
        }
    }

    private void drawTitleContent(Graphics2D g) {
        g.setColor(new Color(0, 10, 20, 112));
        g.fillRect(0, 68, getWidth(), 142);
        g.fillRect(0, 245, getWidth(), 235);

        boolean flash = (animationTick / TITLE_FLASH_TICKS) % 2 == 0;
        Color titleColor = flash
                ? new Color(126, 245, 235)
                : new Color(255, 230, 126);

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(50)));
        drawCenteredShadowed(g, "OCEAN INVADERS",
                145, titleColor, new Color(3, 32, 53));

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(13)));
        drawCenteredShadowed(g, "AN UNDERSEA ARCADE ASSAULT",
                182, new Color(187, 226, 224), new Color(3, 32, 53));

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(29)));
        drawCenteredShadowed(g, "[1]  START GAME",
                310, new Color(255, 236, 137), new Color(45, 24, 20));

        int startWidth = g.getFontMetrics().stringWidth("[1]  START GAME");
        int centerX = getWidth() / 2;
        g.setColor(new Color(91, 222, 210));
        g.fillRect(centerX - startWidth / 2, 322, startWidth, 2);

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(18)));
        drawCenteredShadowed(g, "[2]  START FROM STAGE 2",
                371, Color.WHITE, new Color(3, 22, 36));
        drawCenteredShadowed(g, "[3]  START BOSS FIGHT",
                411, new Color(255, 177, 147), new Color(3, 22, 36));
        drawCenteredShadowed(g, "[Q]  QUIT",
                451, new Color(179, 207, 210), new Color(3, 22, 36));

        g.setFont(new Font("Monospaced", Font.BOLD,
                scaledFontSize(11)));
        drawCenteredShadowed(g, "CSX4515 GAME DESIGN AND DEVELOPMENT",
                620, new Color(153, 207, 207), new Color(3, 22, 36));
        drawCenteredShadowed(g, "SECTION 542  |  SEMESTER 1/2026",
                641, new Color(153, 207, 207), new Color(3, 22, 36));
        drawCenteredShadowed(g,
                "SAI AIKE SHWE TUN AUNG  &  EKATERINA KAZAKOVA",
                668, new Color(218, 236, 231), new Color(3, 22, 36));
    }

    private void drawCenteredShadowed(Graphics2D g, String text, int y,
            Color color, Color shadow) {
        int x = (getWidth() - g.getFontMetrics().stringWidth(text)) / 2;
        g.setColor(shadow);
        g.drawString(text, x + 3, y + 3);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private void seedInitialSwimmers() {
        int spacing = BOARD_WIDTH / SWIMMER_TYPES.length;
        for (int index = 0; index < SWIMMER_TYPES.length; index++) {
            int x = index * spacing - random.nextInt(90);
            spawnSwimmer(SWIMMER_TYPES[index], x);
        }

        spawnTicks = randomBetween(MIN_SPAWN_TICKS, MAX_SPAWN_TICKS);
    }

    private void spawnSwimmer(SwimmerType type, double x) {
        double sizeScale = 0.82 + random.nextDouble() * 0.32;
        int size = (int) Math.round(type.baseSize * sizeScale);
        int y = randomBetween(30, BOARD_HEIGHT - size - 25);
        double speed = type.minSpeed
                + random.nextDouble() * (type.maxSpeed - type.minSpeed);
        double opacity = 0.68 + random.nextDouble() * 0.25;
        double bobPhase = random.nextDouble() * Math.PI * 2;

        swimmers.add(new TitleSwimmer(
                type, x, y, size, speed, opacity, bobPhase));
    }

    private SwimmerType randomType() {
        return SWIMMER_TYPES[random.nextInt(SWIMMER_TYPES.length)];
    }

    private int randomBetween(int minimum, int maximum) {
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private double wrap(double value, int length) {
        return value >= length ? value - length : value;
    }

    private class TitleInput extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_1 -> game.startNewGame();
                case KeyEvent.VK_2 -> game.startFromScene2();
                case KeyEvent.VK_3 -> game.startFromBoss();
                case KeyEvent.VK_Q -> game.quit();
                default -> {}
            }
        }
    }

    private static final class SwimmerType {

        private final Image sheet;
        private final int frameCount;
        private final int baseSize;
        private final double minSpeed;
        private final double maxSpeed;
        private final int animationInterval;

        private SwimmerType(String sheetPath, int baseSize,
                double minSpeed, double maxSpeed, int animationInterval) {
            ImageIcon icon = new ImageIcon(sheetPath);
            sheet = icon.getImage();
            frameCount = icon.getIconWidth() / FRAME_SIZE;
            this.baseSize = baseSize;
            this.minSpeed = minSpeed;
            this.maxSpeed = maxSpeed;
            this.animationInterval = animationInterval;
        }
    }

    private static final class TitleSwimmer {

        private final SwimmerType type;
        private final int y;
        private final int size;
        private final double speed;
        private final double opacity;
        private double x;
        private double bobPhase;
        private int frame;
        private int frameTicks;

        private TitleSwimmer(SwimmerType type, double x, int y,
                int size, double speed, double opacity, double bobPhase) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.opacity = opacity;
            this.bobPhase = bobPhase;
        }

        private void update() {
            x += speed;
            bobPhase += 0.035;

            if (++frameTicks >= type.animationInterval) {
                frameTicks = 0;
                frame = (frame + 1) % type.frameCount;
            }
        }

        private void draw(Graphics2D g) {
            int drawX = (int) Math.round(x);
            int drawY = y + (int) Math.round(Math.sin(bobPhase) * 7);
            int sourceX = frame * FRAME_SIZE;

            Graphics2D spriteGraphics = (Graphics2D) g.create();
            spriteGraphics.setComposite(AlphaComposite.SrcOver.derive(
                    (float) opacity));
            spriteGraphics.drawImage(type.sheet,
                    drawX, drawY, drawX + size, drawY + size,
                    sourceX, 0, sourceX + FRAME_SIZE, FRAME_SIZE, null);
            spriteGraphics.dispose();
        }
    }
}
