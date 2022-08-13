package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.Vec2D;
import de.yonedash.smash.resource.Texture;
import de.yonedash.smash.scene.Scene;

import java.awt.*;

public class EntityBottle extends EntityItem {

    private final Color color;

    public EntityBottle(Vec2D position, Color color) {
        super(new BoundingBox(position, new Vec2D(50.0, 20.0)));
        this.boundingBox.position.subtract(this.getBoundingBox().size.clone().multiply(0.5));
        this.color = color;
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        Texture texture = switch (this.color) {
            case RED -> scene.instance.atlas.potionRed;
            case GREEN -> scene.instance.atlas.potionGreen;
            case YELLOW -> scene.instance.atlas.potionYellow;
        };

        BoundingBox texBounds = this.boundingBox.clone().scale(2.0);
        g2d.drawImage(
                texture.getBufferedImage(),
                scene.scaleToDisplay(texBounds.position.x), scene.scaleToDisplay(texBounds.position.y),
                scene.scaleToDisplay(texBounds.size.x), scene.scaleToDisplay(texBounds.size.x),
                null
        );
    }

    @Override
    public boolean pickup(Scene scene, EntityPlayer entityPlayer) {
        return true;
    }

    public enum Color {
        RED, GREEN, YELLOW
    }

}
