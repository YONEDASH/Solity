package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;

public class EntityAnt extends EntityEnemy {
    public EntityAnt(BoundingBox boundingBox) {
        super(boundingBox);
        this.entityMoveSpeed = 0.12;
    }
}
