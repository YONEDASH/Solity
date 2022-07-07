package de.yonedash.smash.entity;

import de.yonedash.smash.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EntityFog extends EntityBase implements VisualEffect {

    private final BufferedImage image;

    public EntityFog(Chunk chunk) {
        // To prevent a bug (for which the fix would take unnecessary time) from not loading the fog,
        // we need to offset the fog by 1 into the chunk for it to be loaded like intended
        super(chunk.getBoundingBox().clone().add(new BoundingBox(new Vec2D(1, 1), Vec2D.zero())));

        // Create image
        this.image = new BufferedImage((int) (Chunk.CHUNK_SIZE * Constants.FOG_QUALITY_FACTOR), (int) (Chunk.CHUNK_SIZE * Constants.FOG_QUALITY_FACTOR), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        g2d.drawImage(
                image,
                scene.scaleToDisplay(this.boundingBox.position.x - 1),
                scene.scaleToDisplay(this.boundingBox.position.y - 1),
                scene.scaleToDisplay(this.boundingBox.size.x),
                scene.scaleToDisplay(this.boundingBox.size.y),
                null
        );
    }

    public void generateFog(Scene scene) {
        OpenSimplexNoise simplexNoise = scene.instance.world.simplexNoise;

        double fogOffset = scene.instance.world.fogOffset;
        double fogSize = 1500.0f;
        double fogX = this.boundingBox.position.x - 1 + fogOffset;
        double fogY = this.boundingBox.position.y - 1 + fogOffset * 0.4;

        double fogHorizontalScale = this.boundingBox.size.x / this.image.getWidth();
        double fogVerticalScale = this.boundingBox.size.y / this.image.getHeight();
        float fogThickness = 1.175f;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                double value = simplexNoise.eval(
                        (fogX + (x * fogHorizontalScale)) / fogSize, (fogY + (y * fogVerticalScale)) / fogSize
                );

                double grade = (value + 1.0) / 2.0;
                float brightness = (float) grade * fogThickness;
                float alpha = (float) grade * 0.1f + ((brightness - (float) grade) * 0.8f);

                int a = (int) (Math.min(1.0f, alpha) * 255);
                int b = (int) (Math.min(1.0f, brightness) * 255);
                int argb = (a << 24) | (b << 16 ) | (b << 8) | b;

                image.setRGB(x, y, argb);
            }
        }
    }

    @Override
    public int getZ() {
        return 1;
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
