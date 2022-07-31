package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.scene.Scene;
import de.yonedash.smash.graphics.LightSource;

import java.awt.*;

// The function of this class is to represent each tile in the game world
public class LevelObject implements CollisionAware, DisplayEntity {

    // This constant integer determines the tiles' size in pixels (independent of the scale)
    public static final int TILE_SIZE = 140;

    protected BoundingBox[] collisionBoxes;
    protected BoundingBox boundingBox;
    protected int z;

    protected boolean hasDynamicZ;
    protected double dynamicFactor;

    protected LightSource lightSource;

    public LevelObject(BoundingBox boundingBox, int z) {
        this.boundingBox = boundingBox;
        this.collisionBoxes = new BoundingBox[0];
        this.z = z;
        this.hasDynamicZ = false;
    }

    public void draw(Scene scene, Graphics2D g2d, double dt) {
        g2d.setColor(Color.MAGENTA);
        g2d.drawRect(
                scene.scaleToDisplay(this.boundingBox.position.x),
                scene.scaleToDisplay(this.boundingBox.position.y),
                scene.scaleToDisplay(this.boundingBox.size.x),
                scene.scaleToDisplay(this.boundingBox.size.y)
        );
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public void setCollisionBoxes(BoundingBox[] collisionBoxes) {
        this.collisionBoxes = collisionBoxes;
    }

    public BoundingBox[] getCollisionBoxes() {
        return this.collisionBoxes;
    }

    @Override
    public boolean hasCollision() {
        return this.collisionBoxes.length > 0 ;
    }

    @Override
    public int getY() {
        return (int) (this.boundingBox.position.y + (hasDynamicZ ? this.boundingBox.size.y * this.dynamicFactor : 0));
    }

    @Override
    public int getZ() {
        return this.z;
    }

    public void setDynamic(double dynamicFactor) {
        this.hasDynamicZ = true;
        this.dynamicFactor = dynamicFactor;
        this.z = 0;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public LightSource getLightSource() {
        return lightSource;
    }
}
