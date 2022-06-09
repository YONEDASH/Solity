package de.yonedash.smash.resource;

import de.yonedash.smash.Direction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class TextureAnimated implements Texture {

    private final TextureAtlas atlas;
    private double playbackTime;
    private int playbackIndex;
    private final double playbackLength;
    private final BufferedImage[] bufferedImages;

    protected TextureAnimated(TextureAtlas atlas, BufferedImage bufferedImage, double playbackSpeed, Direction direction, int width, int height) {
        this.atlas = atlas;

        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            int imageHeight = bufferedImage.getHeight();;
            this.bufferedImages = new BufferedImage[imageHeight / height];
            for (int index = 0; index < bufferedImages.length; index++) {
                this.bufferedImages[index] = bufferedImage.getSubimage(0, index * height, width, height);
            }

        } else if (direction == Direction.EAST || direction == Direction.WEST) {
            this.bufferedImages = new BufferedImage[bufferedImage.getWidth() / width];
            for (int index = 0; index < bufferedImages.length; index++) {
                this.bufferedImages[index] = bufferedImage.getSubimage(index * width, 0, width, height);
            }

        } else {
            this.bufferedImages = null;
            throw new IllegalArgumentException("Invalid direction");
        }

        double playbackDelay = 1000.0 / playbackSpeed;
        this.playbackLength = playbackDelay * this.bufferedImages.length;
    }

    public void update(double dt) {
        this.playbackTime += dt;
        if (this.playbackTime >= this.playbackLength)
            this.playbackTime = 0;

        double progress = this.playbackTime / this.playbackLength;
        this.playbackIndex = (int) Math.round(progress * (this.bufferedImages.length - 1));
    }


    public void restart() {
        this.playbackTime = 0;
        this.playbackIndex = 0;
    }

    @Override
    public BufferedImage getBufferedImage() {
        return this.bufferedImages[this.playbackIndex];
    }

    @Override
    public void flush() {
        this.atlas.texturesLoaded.remove(this);
        Arrays.stream(this.bufferedImages).forEach(Image::flush);
    }

}
