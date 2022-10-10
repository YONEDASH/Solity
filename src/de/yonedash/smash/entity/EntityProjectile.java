package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.graphics.GraphicsUtils;
import de.yonedash.smash.graphics.TextureIndividual;
import de.yonedash.smash.resource.Texture;
import de.yonedash.smash.scene.Scene;

import java.awt.*;

public class EntityProjectile extends EntityBase {

    protected final Vec2D origin;
    protected final Entity shooter;
    protected double rotation;
    protected final double moveSpeed, maxDistance, damage;

    protected final TextureIndividual texture;

    public EntityProjectile(Texture texture, BoundingBox boundingBox, Entity shooter, double rotation, double moveSpeed, double damage, double maxDistance) {
        super(boundingBox);
        this.texture = new TextureIndividual(texture);
        this.origin = boundingBox.center();
        this.shooter = shooter;
        this.rotation = rotation;
        this.moveSpeed = moveSpeed;
        this.maxDistance = maxDistance;
        this.damage = damage;
    }


    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        this.texture.update(dt);

        // Calculate how far projectile has travelled in %
        double d = this.boundingBox.center().distanceSqrt(this.origin) / maxDistance;

        double fadeThreshold = 0.075;
        double fadeBegin = 1.0 - fadeThreshold;
        if (d >= fadeBegin) {
            double f = Math.min((d - fadeBegin) / fadeThreshold, 1);
            GraphicsUtils.setAlpha(g2d, 1.0f - (float) f);
        }

        Vec2D center = this.boundingBox.center();
        double rotationDegView = this.rotation + 90.0;
        GraphicsUtils.rotate(g2d, rotationDegView, scene.scaleToDisplay(center.x), scene.scaleToDisplay(center.y));
        g2d.drawImage(
                this.texture.getImage(),
                scene.scaleToDisplay(this.boundingBox.position.x), scene.scaleToDisplay(this.boundingBox.position.y),
                scene.scaleToDisplay(this.boundingBox.size.x), scene.scaleToDisplay(this.boundingBox.size.y),
                null
        );
        GraphicsUtils.rotate(g2d, -rotationDegView, scene.scaleToDisplay(center.x), scene.scaleToDisplay(center.y));

        if (d >= fadeBegin)
            GraphicsUtils.setAlpha(g2d, 1f);

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
    public boolean collide(Scene scene, LevelObject levelObject, BoundingBox objectBoundingBox) {
        if (!scene.instance.world.entitiesLoaded.contains(this))
            return false;

        // levelObject.getBoundingBox().size = levelObject.getBoundingBox().position = new Vec2D(0, 0);
        scene.instance.world.entitiesLoaded.remove(this);

        scene.instance.audioProcessor.play(scene.instance.library.hitSound);


        double particleHitScale = 1.0;
        EntityParticle particle = new EntityParticle(this.boundingBox.clone().scale(particleHitScale), scene.instance.atlas.animHit, 0.0, 0.0, 150.0, this.getZ(), false);
        scene.instance.world.entitiesLoaded.add(particle);

        return true;
    }

    @Override
    public boolean collide(Scene scene, Entity entity) {
        if (shooter == entity
                || entity instanceof EntityProjectile
                || !(entity instanceof EntityDamageable)
                || !scene.instance.world.entitiesLoaded.contains(this)
                || (shooter instanceof EntityEnemy && entity instanceof EntityEnemy))
            return false;

        // Ignore player if dashing
        if (entity instanceof EntityPlayer player && player.isDashing())
            return false;

        scene.instance.audioProcessor.play(scene.instance.library.hitSound);

        scene.instance.world.entitiesLoaded.remove(this);

        // Damage entity
        EntityDamageable entityDamageable = (EntityDamageable) entity;
        entityDamageable.setHealth(entityDamageable.getHealth() - damage);

        if (entityDamageable.getHealth() <= 0 && scene.instance.world.entitiesLoaded.contains(entity)) {
            if (!(entity instanceof EntityPlayer))
                scene.instance.world.entitiesLoaded.remove(entity);

            if (entity instanceof EntityEnemy) {
                scene.instance.world.entitiesLoaded.add(new EntityCoin(entity.getBoundingBox().center().clone()));
            }

            EntityParticle particle = new EntityParticle(entity.getBoundingBox().clone().scale(2.0), scene.instance.atlas.animAfterDeath, 0.0, 0.0, 350.0, entity.getZ() - 1, false);
            scene.instance.world.entitiesLoaded.add(particle);
        }

        double particleHitScale = 0.75;
        EntityParticle particle = new EntityParticle(this.boundingBox.clone().scale(particleHitScale), scene.instance.atlas.animHit, 0.0, 0.0, 150.0, this.getZ(), false);
        scene.instance.world.entitiesLoaded.add(particle);

        return true;
    }

    public Entity getShooter() {
        return shooter;
    }

    public double getRotation() {
        return rotation;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

}
