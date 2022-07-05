package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.resource.Texture;

public class EntityParticleLeaf extends EntityParticle {

    public EntityParticleLeaf(BoundingBox boundingBox, Texture texture, double rotationDirection, double moveSpeed, double targetTimeAlive, int z) {
        super(boundingBox, texture, rotationDirection, moveSpeed, targetTimeAlive, z, true);
    }

}
