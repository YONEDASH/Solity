package de.yonedash.solity.graphics;

import de.yonedash.solity.Direction;
import de.yonedash.solity.resource.Texture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class TextureAnimated implements Texture {

    private final TextureAtlas atlas;
    private final int width, height;
    private double playbackTime;
    private int playbackIndex;
    private final double playbackLength;
    private final BufferedImage[] bufferedImages;
    private final boolean blank;

    protected TextureAnimated(TextureAtlas atlas, BufferedImage bufferedImage, double playbackSpeed, Direction direction, int width, int height) {
        this.atlas = atlas;
        this.width = width;
        this.height = height;

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

        this.blank = Arrays.stream(this.bufferedImages).anyMatch(bi -> !isBufferedImageBlank(bi));
    }

    protected TextureAnimated(TextureAtlas atlas, double playbackSpeed, Texture... textures) {
        this.atlas = atlas;
        this.width = textures[0].getWidth();
        this.height = textures[1].getWidth();

        this.bufferedImages = new BufferedImage[textures.length];
        for (int i = 0; i < this.bufferedImages.length; i++) {
            this.bufferedImages[i] = textures[i].getBufferedImage();
        }

        double playbackDelay = 1000.0 / playbackSpeed;
        this.playbackLength = playbackDelay * this.bufferedImages.length;

        this.blank = Arrays.stream(this.bufferedImages).anyMatch(bi -> !isBufferedImageBlank(bi));
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
    public Image getImage() {
        return this.bufferedImages[this.playbackIndex];
    }

    public Image getImage(int index) {
        return this.bufferedImages[index];
    }

    public double getPlaybackLength() {
        return playbackLength;
    }

    public int getSize() {
        return this.bufferedImages.length;
    }

    @Override
    public BufferedImage getBufferedImage() {
        return this.bufferedImages[this.playbackIndex];
    }

    public BufferedImage getBufferedImage(int index) {
        return this.bufferedImages[index];
    }


    @Override
    public void flush() {
        this.atlas.texturesLoaded.remove(this);
        Arrays.stream(this.bufferedImages).forEach(Image::flush);
    }

}
