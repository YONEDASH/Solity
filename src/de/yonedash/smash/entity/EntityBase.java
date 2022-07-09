package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.graphics.LightSource;

public abstract class EntityBase implements Entity {

    protected final BoundingBox boundingBox;
    protected final Vec2D motion;

    protected Direction viewDirection;

    protected LightSource lightSource;

    public EntityBase(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.motion = new Vec2D(0, 0);
        this.viewDirection = Direction.SOUTH;
    }

    @Override
    public void update(Scene scene, double dt) {
        // Apply motion
        this.boundingBox.position.add(this.motion);
        this.motion.multiply(0.1);
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
}
