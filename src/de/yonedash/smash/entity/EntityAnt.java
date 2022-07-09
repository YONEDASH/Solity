package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.graphics.LightSource;

import java.awt.*;

public class EntityAnt extends EntityEnemy {
    public EntityAnt(BoundingBox boundingBox) {
        super(boundingBox);
        this.entityMoveSpeed = 0.12;
        this.lightSource = new LightSource(Color.RED, Math.random() * 8);
    }
}
