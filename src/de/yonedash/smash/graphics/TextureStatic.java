package de.yonedash.smash.graphics;

import de.yonedash.smash.ImageUtils;
import de.yonedash.smash.resource.Texture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextureStatic implements Texture {

    private final TextureAtlas atlas;
    private final int width, height;
    private final BufferedImage bufferedImage;
    private final boolean blank;

    protected TextureStatic(TextureAtlas atlas, BufferedImage bufferedImage) {
        this.atlas = atlas;
        this.bufferedImage = bufferedImage;
        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();
        this.blank = ImageUtils.isBufferedImageBlank(this.bufferedImage);
    }

    @Override
    public Image getImage() {
        return this.bufferedImage;
    }

    @Override
    public BufferedImage getBufferedImage() {
        return this.bufferedImage;
    }

    @Override
    public boolean isBlank() {
        return this.blank;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void flush() {
        this.atlas.texturesLoaded.remove(this);
        this.bufferedImage.flush();
    }

}
