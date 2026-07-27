package gdd.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public final class BubbleSprite {

    private static final String SHEET_PATH =
            "src/images/projectiles/bubble.png";
    private static final int FRAME_SIZE = 16;
    private static final BufferedImage NEUTRAL_SHEET =
            toBufferedImage(new ImageIcon(SHEET_PATH).getImage());
    private static final Map<Integer, BufferedImage> TINTED_SHEETS =
            new HashMap<>();

    private static final List<Rectangle> FLIGHT_CLIPS = List.of(
            new Rectangle(0, 0, FRAME_SIZE, FRAME_SIZE));
    private static final List<Rectangle> POP_CLIPS = List.of(
            new Rectangle(FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE),
            new Rectangle(FRAME_SIZE * 2, 0, FRAME_SIZE, FRAME_SIZE));

    private BubbleSprite() {
    }

    public static Image tintedSheet(Color color) {
        return TINTED_SHEETS.computeIfAbsent(
                color.getRGB(), ignored -> tintSheet(color));
    }

    public static List<Rectangle> flightClips() {
        return FLIGHT_CLIPS;
    }

    public static List<Rectangle> popClips() {
        return POP_CLIPS;
    }

    private static BufferedImage tintSheet(Color color) {
        BufferedImage tinted = new BufferedImage(
                NEUTRAL_SHEET.getWidth(), NEUTRAL_SHEET.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < NEUTRAL_SHEET.getHeight(); y++) {
            for (int x = 0; x < NEUTRAL_SHEET.getWidth(); x++) {
                int source = NEUTRAL_SHEET.getRGB(x, y);
                int alpha = source >>> 24;
                int gray = source >> 16 & 0xff;
                float highlight = Math.max(0f, (gray - 200) / 55f);

                int red = tintedChannel(color.getRed(), gray, highlight);
                int green = tintedChannel(color.getGreen(), gray, highlight);
                int blue = tintedChannel(color.getBlue(), gray, highlight);

                tinted.setRGB(x, y, alpha << 24
                        | red << 16 | green << 8 | blue);
            }
        }

        return tinted;
    }

    private static int tintedChannel(int colorChannel, int gray,
            float highlight) {
        int colored = colorChannel * gray / 255;
        return Math.round(colored + (255 - colored) * highlight);
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }

        BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return bufferedImage;
    }
}
