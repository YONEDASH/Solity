package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;

// Any entity class that implements this interface will be affected by collision
public interface CollisionAware {

    BoundingBox getBoundingBox();

    boolean hasCollision();

}
