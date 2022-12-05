package de.yonedash.solity.entity;

import de.yonedash.solity.BoundingBox;

// Any entity class that implements this interface will be affected by collision
public interface CollisionAware {

    BoundingBox getBoundingBox();

    boolean hasCollision();

}
