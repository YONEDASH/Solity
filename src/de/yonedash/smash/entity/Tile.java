package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.Scene;
import de.yonedash.smash.resource.Texture;

import java.awt.*;

public class Tile extends LevelObject {

    private final Texture texture;

    public Tile(BoundingBox boundingBox, int z, Texture texture) {
        super(boundingBox, z);
        this.texture = texture;
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        double padding = 1.0 / scene.calculateDisplayScaleFactor();
        g2d.drawImage(this.texture.getBufferedImage(),
                scene.scaleToDisplay(this.boundingBox.position.x - padding),
                scene.scaleToDisplay(this.boundingBox.position.y - padding),
                scene.scaleToDisplay(this.boundingBox.size.x + padding * 2),
                scene.scaleToDisplay(this.boundingBox.size.y + padding * 2),
                null
        );
    }
}
