package de.yonedash.smash.resource;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface Texture {

    Image getImage();
    BufferedImage getBufferedImage();
    boolean isBlank();
    int getWidth();
    int getHeight();
    void flush();

    default boolean isBufferedImageBlank(BufferedImage bufferedImage) {
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
