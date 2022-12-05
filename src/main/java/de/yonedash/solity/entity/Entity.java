package de.yonedash.solity.entity;

import de.yonedash.solity.BoundingBox;
import de.yonedash.solity.scene.Scene;
import de.yonedash.solity.Vec2D;

// This interface represents every entity in the game world/level
public interface Entity extends DisplayEntity {

    void update(Scene scene, double dt);

    boolean collide(Scene scene, LevelObject levelObject, BoundingBox objectBoundingBox);

    boolean collide(Scene scene, Entity entity);

    Vec2D getMotion();

    BoundingBox getBoundingBox();

    void removeWhenHidden(); // Removes entity once it is no longer on screen

    void remove(); // Remove entity when it is updated

}
