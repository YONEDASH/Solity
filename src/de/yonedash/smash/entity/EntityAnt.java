package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.Vec2D;
import de.yonedash.smash.graphics.LightSource;

import java.awt.*;

public class EntityAnt extends EntityEnemy {
    public EntityAnt(Vec2D position) {
        super(new BoundingBox(position, new Vec2D(60, 12)));
        this.entityMoveSpeed = 0.12;
        this.lightSource = new LightSource(Color.RED, Math.random() * 8);
    }
}
