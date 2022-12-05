package de.yonedash.solity.entity;

import de.yonedash.solity.BoundingBox;
import de.yonedash.solity.Vec2D;
import de.yonedash.solity.scene.Scene;

public class EntityNPC extends EntityCharacter {

    public EntityNPC(Vec2D position) {
        super(new BoundingBox(position.clone(), new Vec2D(50, 20).multiply(1.125)));
    }

    @Override
    protected void move(Scene scene, double dt, Vec2D moveMotion) {

    }

}
