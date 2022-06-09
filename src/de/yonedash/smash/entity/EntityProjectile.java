package de.yonedash.smash.entity;

import de.yonedash.smash.*;

import java.awt.*;

public class EntityProjectile extends EntityBase {

    protected final Vec2D origin;
    protected final Entity shooter;
    protected double rotation;
    protected final double moveSpeed, maxDistance;

    public EntityProjectile(BoundingBox boundingBox, Entity shooter, double rotation, double moveSpeed, double maxDistance) {
        super(boundingBox);
        this.origin = boundingBox.center();
        this.shooter = shooter;
        this.rotation = rotation;
        this.moveSpeed = moveSpeed;
        this.maxDistance = maxDistance;
    }


    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        Vec2D center = this.boundingBox.center();
        double rotationDegView = this.rotation + 90.0;
        g2d.setColor(Color.WHITE);
        GraphicsUtils.rotate(g2d, rotationDegView, scene.scaleToDisplay(center.x), scene.scaleToDisplay(center.y));
        g2d.drawImage(
                scene.instance.atlas.fork.getBufferedImage(),
                scene.scaleToDisplay(this.boundingBox.position.x), scene.scaleToDisplay(this.boundingBox.position.y),
                scene.scaleToDisplay(this.boundingBox.size.x), scene.scaleToDisplay(this.boundingBox.size.y),
                null
        );
        GraphicsUtils.rotate(g2d, -rotationDegView, scene.scaleToDisplay(center.x), scene.scaleToDisplay(center.y));
    }

    @Override
    public int getZ() {
        return 0;
    }

    @Override
    public void update(Scene scene, double dt) {
        double moveSpeed = scene.time(this.moveSpeed, dt);
        this.motion.add(rotation, moveSpeed);

        // Check if projectile is loaded in a chunk
        boolean insideChunk = false;
        for (Chunk chunk : scene.instance.world.chunksLoaded) {
            if (chunk.getBoundingBox().isColliding(this.boundingBox, 0)) {
                insideChunk = true;
                break;
            }
        }

        // Remove projectile if it is not loaded or max distance has been reached
        if (!insideChunk || this.boundingBox.center().distanceSqrt(this.origin) >= maxDistance)
            scene.instance.world.entitiesLoaded.remove(this);

        super.update(scene, dt);
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject) {
        // levelObject.getBoundingBox().size = levelObject.getBoundingBox().position = new Vec2D(0, 0);
        scene.instance.world.entitiesLoaded.remove(this);
        return true;
    }

    @Override
    public boolean collide(Scene scene, Entity entity) {
        if (shooter == entity)
            return false;

        scene.instance.world.entitiesLoaded.remove(this);
        scene.instance.world.entitiesLoaded.remove(entity);
        return true;
    }

    public Entity getShooter() {
        return shooter;
    }
}
