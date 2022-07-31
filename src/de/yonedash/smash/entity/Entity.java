package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.scene.Scene;
import de.yonedash.smash.Vec2D;

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
