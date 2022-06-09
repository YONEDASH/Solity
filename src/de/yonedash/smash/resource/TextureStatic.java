package de.yonedash.smash.resource;

import java.awt.image.BufferedImage;

public class TextureStatic implements Texture {

    private final TextureAtlas atlas;
    private final BufferedImage bufferedImage;

    protected TextureStatic(TextureAtlas atlas, BufferedImage bufferedImage) {
        this.atlas = atlas;
        this.bufferedImage = bufferedImage;
    }

    @Override
    public BufferedImage getBufferedImage() {
        return this.bufferedImage;
    }

    @Override
    public void flush() {
        this.atlas.texturesLoaded.remove(this);
        this.bufferedImage.flush();
    }
}
