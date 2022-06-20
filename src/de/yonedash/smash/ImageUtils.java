package de.yonedash.smash;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {

    public static boolean isBufferedImageBlank(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        for (int i = 0; i < width * height; i++) {
            int y = i / width;
            int x = i - (y * width);
            int rgba = new Color(bufferedImage.getRGB(x, y), true).getRGB();
            int a = ((rgba >> 24) & 0xFF);
            if (a > 0)
                return false;
        }
        return true;
    }

}
