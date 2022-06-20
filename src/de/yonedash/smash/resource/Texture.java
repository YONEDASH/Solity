package de.yonedash.smash.resource;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface Texture {

    BufferedImage getBufferedImage();
    boolean isBlank();
    int getWidth();
    int getHeight();
    void flush();


}
