package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.Vec2D;
import de.yonedash.smash.scene.Scene;

import java.awt.*;

public class EntityCoin extends EntityItem {

    public EntityCoin(Vec2D position) {
        super(new BoundingBox(position, new Vec2D(50.0, 50.0)));
        this.boundingBox.position.subtract(this.getBoundingBox().size.clone().multiply(0.5));
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        g2d.drawImage(
                scene.instance.atlas.coin.getBufferedImage(),
                scene.scaleToDisplay(this.boundingBox.position.x), scene.scaleToDisplay(this.boundingBox.position.y),
                scene.scaleToDisplay(this.boundingBox.size.x), scene.scaleToDisplay(this.boundingBox.size.y),
                null
        );
    }

    @Override
    public boolean pickup(Scene scene, EntityPlayer entityPlayer) {
        scene.instance.world.saveGame.addCoins(1);

        return true;
    }
}
