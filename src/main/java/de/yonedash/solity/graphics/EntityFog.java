package de.yonedash.solity.graphics;

import de.yonedash.solity.*;
import de.yonedash.solity.config.GraphicsConfig;
import de.yonedash.solity.entity.Entity;
import de.yonedash.solity.entity.EntityBase;
import de.yonedash.solity.entity.LevelObject;
import de.yonedash.solity.scene.Scene;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EntityFog extends EntityBase implements VisualEffect {

    private BufferedImage image;

    public EntityFog(Chunk chunk) {
        // To prevent a bug (for which the fix would take unnecessary time) from not loading the fog,
        // we need to offset the fog by 1 into the chunk for it to be loaded like intended
        super(chunk.getBoundingBox().clone().add(new BoundingBox(new Vec2D(1, 1), Vec2D.zero())));
    }

    public void createFog(GraphicsConfig graphicsConfig) {
        // Create image
        double quality = graphicsConfig.fogQuality;
        this.image = new BufferedImage((int) (Chunk.CHUNK_SIZE * quality), (int) (Chunk.CHUNK_SIZE * quality), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        if (image == null)
            return;

        g2d.drawImage(
                image,
                scene.scaleToDisplay(this.boundingBox.position.x - 1),
                scene.scaleToDisplay(this.boundingBox.position.y - 1),
                scene.scaleToDisplay(this.boundingBox.size.x),
                scene.scaleToDisplay(this.boundingBox.size.y),
                null
        );
    }

    public void updateFog(double fogOffset, double weatherProgress, OpenSimplexNoise simplexNoise) {
        if (image == null || simplexNoise == null)
            return;

        double fogSize = 1500.0f;
        double fogX = this.boundingBox.position.x - 1 + fogOffset;
        double fogY = this.boundingBox.position.y - 1 + fogOffset * 0.4;

        double fogHorizontalScale = this.boundingBox.size.x / this.image.getWidth();
        double fogVerticalScale = this.boundingBox.size.y / this.image.getHeight();
        float fogSpread = 1.175f;
        float fogDensity = 2.0f * (float) ((weatherProgress + 1.0f) / 2.0f);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image == null) break;

                double value = simplexNoise.eval(
                        (fogX + (x * fogHorizontalScale)) / fogSize, (fogY + (y * fogVerticalScale)) / fogSize
                );

                double grade = (value + 1.0) / 2.0;
                float brightness = (float) grade * fogSpread;
                float alpha = (float) grade * 0.2f + ((brightness - (float) grade) * (1.0f - 0.98f + fogDensity));

                int a = (int) (Math.min(1.0f, alpha * 1.2) * 255);
                int b = (int) (Math.min(1.0f, brightness) * 255 * 0.9);
                int argb = (a << 24) | (b << 16 ) | (b << 8) | b;

                if (x <= image.getWidth() && y <= image.getHeight())
                    image.setRGB(x, y, argb);
            }
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public int getZ() {
        return 100;
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject, BoundingBox objectBoundingBox) {
        return false;
    }

    @Override
    public boolean collide(Scene scene, Entity entity) {
        return false;
    }
}
