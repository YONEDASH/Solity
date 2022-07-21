package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.graphics.LightSource;

public abstract class EntityBase implements Entity {

    protected final BoundingBox boundingBox;
    protected final Vec2D motion;

    protected int removeState;

    protected Direction viewDirection;

    protected LightSource lightSource;

    public EntityBase(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.motion = new Vec2D(0, 0);
        this.viewDirection = Direction.SOUTH;
        this.removeState = 0;
    }

    @Override
    public void update(Scene scene, double dt) {
        // Apply motion
        this.boundingBox.position.add(this.motion);
        this.motion.multiply(0.1);

        // Handle remove
        if (this.removeState > 0) {
            // Handle remove when hidden
            if (this.removeState == 1 && scene instanceof SceneInGame sig) {
                // Calculate camera view
                Display display = scene.getDisplay();
                BoundingBox cameraView = new BoundingBox(sig.cameraPos, new Vec2D(display.getWidth(), display.getHeight()));

                // Check if entity is not on screen
                if (!cameraView.isColliding(this.boundingBox, 0)) {
                    // And remove it
                    scene.instance.world.entitiesLoaded.remove(this);
                }
            } else {
                // removeState was changed, so lets just remove the entity
                scene.instance.world.entitiesLoaded.remove(this);
            }
        }
    }

    @Override
    public Vec2D getMotion() {
        return this.motion;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public Direction getViewDirection() {
        return viewDirection;
    }

    public void setViewDirection(Direction viewDirection) {
        this.viewDirection = viewDirection;
    }

    @Override
    public int getY() {
        return (int) Math.floor(this.boundingBox.position.y + this.boundingBox.size.y);
    }

    @Override
    public LightSource getLightSource() {
        return lightSource;
    }

    @Override
    public void remove() {
        this.removeState = 2;
    }

    @Override
    public void removeWhenHidden() {
        this.removeState = 1;
    }

}
