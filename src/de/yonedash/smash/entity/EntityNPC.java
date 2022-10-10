package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.Vec2D;
import de.yonedash.smash.scene.Scene;

public class EntityNPC extends EntityCharacter {

    public EntityNPC(Vec2D position) {
        super(new BoundingBox(position.clone(), new Vec2D(50, 20).multiply(1.125)));
    }

    @Override
    protected void move(Scene scene, double dt, Vec2D moveMotion) {

    }

}
