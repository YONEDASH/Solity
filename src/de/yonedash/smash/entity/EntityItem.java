package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.scene.Scene;

public abstract class EntityItem extends EntityBase {

    public EntityItem(BoundingBox boundingBox) {
        super(boundingBox);
    }

    @Override
    public int getZ() {
        return 0;
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject, BoundingBox objectBoundingBox) {
        return false;
    }

    @Override
    public boolean collide(Scene scene, Entity entity) {
        if (entity instanceof EntityPlayer player && pickup(scene, player)) {
            remove();
        }

        return false;
    }

    public abstract boolean pickup(Scene scene, EntityPlayer entityPlayer);

}
