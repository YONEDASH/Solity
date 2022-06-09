package de.yonedash.smash.resource;

import java.awt.image.BufferedImage;

public interface Texture {

    BufferedImage getBufferedImage();

    void flush();

}
