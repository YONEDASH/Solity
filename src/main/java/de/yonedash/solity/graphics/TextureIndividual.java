package de.yonedash.solity.graphics;

import de.yonedash.solity.resource.Texture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextureIndividual implements Texture {

    private final Texture texture;

    private boolean looping;
    private double playbackTime;
    private int playbackIndex;
    private final double playbackLength;

    public TextureIndividual(Texture texture) {
        this.texture = texture;

        if (texture instanceof TextureAnimated textureAnimated) {
            this.playbackIndex = 0;
            this.playbackLength = textureAnimated.getPlaybackLength();
            this.looping = true;
        } else {
            this.playbackLength = 0;
        }
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isLooping() {
        return looping;
    }

    public boolean hasFinished() {
        return !this.looping && texture instanceof TextureAnimated textureAnimated && this.playbackIndex == textureAnimated.getSize() - 1;
    }

    public void update(double dt) {
        if (texture instanceof TextureAnimated textureAnimated) {
            this.playbackTime += dt;
            if (this.playbackTime >= this.playbackLength)
                if (looping)
                    this.playbackTime = 0;
                else {
                    this.playbackIndex = textureAnimated.getSize() - 1;
                    return;
                }

            double progress = this.playbackTime / this.playbackLength;
            this.playbackIndex = (int) Math.round(progress * (textureAnimated.getSize() - 1));
        }
    }

    public void restart() {
        this.playbackTime = 0;
        this.playbackIndex = 0;
    }

    @Override
    public Image getImage() {
        return texture instanceof TextureAnimated textureAnimated ? textureAnimated.getImage(playbackIndex) : texture.getImage();
    }

    @Override
    public BufferedImage getBufferedImage() {
        return texture instanceof TextureAnimated textureAnimated ? textureAnimated.getBufferedImage(playbackIndex) : texture.getBufferedImage();
    }

    @Override
    public boolean isBlank() {
        return this.texture.isBlank();
    }

    @Override
    public int getWidth() {
        return this.texture.getWidth();
    }

    @Override
    public int getHeight() {
        return this.texture.getHeight();
    }

    @Override
    public void flush() {
        this.texture.flush();
    }
}
