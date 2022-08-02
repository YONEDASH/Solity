package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.scene.Scene;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class EntityEnemy extends EntityCharacter {

    private Entity target;

    protected double entityMoveSpeed = 1, shotDamage = 1.0, shotSpeed = 0.6;

    public EntityEnemy(BoundingBox boundingBox) {
        super(boundingBox);
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        super.draw(scene, g2d, dt);
    }

    Vec2D targetPosition;
    int pathInterest;

    public double shootIdleTime;

    private double reTargetIdleTime;

    @Override
    public void update(Scene scene, double dt) {
        super.update(scene, dt);

        // Check if entity is loaded in a chunk
        boolean insideChunk = false;
        for (Chunk chunk : scene.instance.world.chunksLoaded) {
            if (chunk.getBoundingBox().isColliding(this.boundingBox, 0)) {
                insideChunk = true;
                break;
            }
        }

        // Don't update entity if it is not in a loaded chunk
        if (!insideChunk)
            return;

        Vec2D center = this.boundingBox.center();

        World world = scene.instance.world;

        double maxDistance = Tile.TILE_SIZE * 8.0;
        double shootDelay = 1000.0 * 1; // TODO DIFFICULTY HERE
        double shotDistance = maxDistance;
        double shotSize = 60.0;
        Vec2D projSize = new Vec2D(shotSize, shotSize);

        // If there is no target or target is too far away, look for a new target
        if (this.target == null || target.getBoundingBox().center().distanceSqrt(center) >= maxDistance) {

            if (this.reTargetIdleTime < 250.0) {
                this.reTargetIdleTime += dt;
                return;
            }

            this.reTargetIdleTime = -100 * world.random(scene, dt);

            // Loop through found entities
            for (Entity temp : findNewTargets(world)) {
                Vec2D tempCenter = temp.getBoundingBox().center();

                if (tempCenter.distanceSqrt(center) >= maxDistance)
                    continue;

                // If target can not be seen, target is not viable
                LevelObject casted = MathUtils.rayCast(world, new BoundingBox(center.clone().subtract(projSize.clone().multiply(0.5)), projSize), center.rotationTo(tempCenter), 50.0, center.distanceSqrt(tempCenter));
                if (casted != null) {
                    continue;
                }

                // Set new temp as target
                this.target = temp;
                break;
            }

            // If there was no target found, do nothing
            if (this.target == null)
                return;

            // Reset shoot idle time
            if (world.random(scene, dt) > 0)
                this.shootIdleTime = 0.0 - world.random(scene, dt) * shootDelay * 0.1;

        }

        this.shootIdleTime += dt;

        // defines how good the enemy can predict their shots
        double intelligence = 1.0; // range 0 - 1

        if (this.shootIdleTime >= shootDelay) {
            this.shootIdleTime = 0.0 - world.random(scene, dt) * shootDelay * 0.5;

            // Motion prediction
            Vec2D targetPosition = this.target.getBoundingBox().center();
            Vec2D targetMotion = this.target.getMotion().clone();

            // Cancel shoot if target is not seen and remove target
            LevelObject casted = MathUtils.rayCast(world, new BoundingBox(center.clone().subtract(projSize.clone().multiply(0.5)), projSize), center.rotationTo(targetPosition), 100.0, center.distanceSqrt(targetPosition));
            if (casted != null) {
                this.target = null;
                return;
            }

            targetPosition = targetPosition.add(targetMotion.clone().multiply(intelligence));

            double rotation = center.rotationTo(targetPosition);

            EntityProjectile proj = new EntityProjectile(
                    scene.instance.atlas.projCanonBall,
                    new BoundingBox(center.clone().subtract(projSize.clone().multiply(0.5)),
                            projSize),
                    this, rotation,
                    shotSpeed, shotDamage, shotDistance);

            world.entitiesLoaded.add(proj);
        }

        if (this.targetPosition != null && this.boundingBox.contains(this.targetPosition)) {
            this.targetPosition = null;
        }

        if (this.targetPosition == null) {
            double angle = world.random(scene, dt) * 360.0;
            double distance = Math.abs(Tile.TILE_SIZE * 2 * world.random(scene, dt));

            LevelObject levelObject = MathUtils.rayCast(world, this.boundingBox, angle, Tile.TILE_SIZE * 0.2, distance);

            if (levelObject != null)
                distance -= (distance - levelObject.getBoundingBox().center().distanceSqrt(center)) + Tile.TILE_SIZE;

            this.targetPosition = center.clone().add(angle, distance);
        }

        double dx = Math.min(Math.max(this.targetPosition.x - center.x, -this.entityMoveSpeed), this.entityMoveSpeed);
        double dy = Math.min(Math.max(this.targetPosition.y - center.y, -this.entityMoveSpeed), this.entityMoveSpeed);
        move(scene, dt, new Vec2D(scene.time(dx, dt), scene.time(dy, dt)));
    }

    private ArrayList<Entity> findNewTargets(World world) {
        Vec2D center = this.boundingBox.center();
        ArrayList<Entity> targets = new ArrayList<>();
        for (Entity entity : world.entitiesLoaded) {
            if (entity instanceof EntityPlayer) {
                targets.add(entity);
            }
        }
        targets.sort(Comparator.comparingDouble(o -> o.getBoundingBox().center().distanceSqrt(center)));
        return targets;
    }

    @Override
    protected void move(Scene scene, double dt, Vec2D moveMotion) {
        this.motion.add(moveMotion);
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject, BoundingBox objectBoundingBox) {
        return true;
    }

    @Override
    public boolean collide(Scene scene, Entity entity) {
        return true;
    }

    @Override
    public int getZ() {
        return 0;
    }

}
