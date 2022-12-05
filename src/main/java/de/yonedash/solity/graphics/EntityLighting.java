package de.yonedash.solity.graphics;

import de.yonedash.solity.*;
import de.yonedash.solity.entity.*;
import de.yonedash.solity.scene.Scene;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class EntityLighting extends EntityBase implements VisualEffect {

    private final BufferedImage image;

    public EntityLighting(Chunk chunk) {
        // To prevent a bug (for which the fix would take unnecessary time) from not loading the fog,
        // we need to offset the fog by 1 into the chunk for it to be loaded like intended
        super(chunk.getBoundingBox().clone().add(new BoundingBox(new Vec2D(1, 1), Vec2D.zero())));

        // Create image
       // this.image = null;
        this.image = new BufferedImage((int) (Chunk.CHUNK_SIZE * 0.01), (int) (Chunk.CHUNK_SIZE * 0.01), BufferedImage.TYPE_INT_ARGB);
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

    public void updateLighting(Scene scene, ArrayList<DisplayEntity> litUpEntities) {
        if (this.image == null)
            return;

        Graphics2D g2d = (Graphics2D) this.image.getGraphics();
        if (g2d == null) g2d = this.image.createGraphics();

        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());

        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

        double baseSize = Tile.TILE_SIZE;

        double lightingHorizontalScale = this.image.getWidth() / this.boundingBox.size.x;
        double lightingVerticalScale = this.image.getHeight() / this.boundingBox.size.y;
        double scale = ((lightingVerticalScale + lightingHorizontalScale) / 2.0);

        for (DisplayEntity displayEntity : litUpEntities) {
            Vec2D lightPos = displayEntity.getBoundingBox().center().clone().subtract(this.boundingBox.position).multiply(scale);
            LightSource source = displayEntity.getLightSource();
            if (source == null) continue;

            Color color = source.color();
            float red = color.getRed() / 255.0f;
            float green = color.getGreen() / 255.0f;
            float blue = color.getBlue() / 255.0f;
            float alpha = 1.0f - ((red + green + blue) / 3.0f);

            double radius = baseSize * source.brightness() / 2.0;

            g2d.setColor(new Color(red, green, blue, 1.0f));
            g2d.fillOval((int) (lightPos.x - radius), (int) (lightPos.y - radius), (int) radius, (int) radius);
        }

    }

    @Override
    public int getZ() {
        return Integer.MAX_VALUE;
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
