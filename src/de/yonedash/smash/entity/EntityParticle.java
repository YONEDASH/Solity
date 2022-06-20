package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.Scene;
import de.yonedash.smash.resource.Texture;

import java.awt.*;

public class EntityParticle extends EntityBase {

    private final Texture texture;
    private final int z;

    private double rotationDirection, moveSpeed, timeAlive;
    private final double targetTimeAlive;

    public EntityParticle(BoundingBox boundingBox, Texture texture, double rotationDirection, double moveSpeed, double targetTimeAlive, int z) {
        super(boundingBox);
        this.texture = texture;
        this.z = z;
        this.targetTimeAlive = targetTimeAlive;
        changeDirection(rotationDirection, moveSpeed);
    }

    public void changeDirection(double rotationDirection, double moveSpeed) {
        this.rotationDirection = rotationDirection;
        this.moveSpeed = moveSpeed;
    }

    @Override
    public void update(Scene scene, double dt) {
        super.update(scene, dt);

        this.timeAlive += dt;
        // Remove particle if time to live has reached or if it is not in an loaded chunk
        if (this.timeAlive > this.targetTimeAlive
                || !scene.instance.world.chunksLoaded.stream().anyMatch(chunk -> chunk.getBoundingBox().isColliding(this.boundingBox, 0))) {
            scene.instance.world.entitiesLoaded.remove(this);
        }

    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        this.boundingBox.position.add(this.rotationDirection, scene.time(this.moveSpeed, dt));

        double progress = this.timeAlive / this.targetTimeAlive;
        double alpha = progress > 0.8 ? 1.0 - ((progress - 0.8) / 0.2) : 1;

        final Composite compositeBefore = g2d.getComposite();
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) alpha);
        g2d.setComposite(alphaComposite);

        g2d.drawImage(
                this.texture.getBufferedImage(),
                scene.scaleToDisplay(this.boundingBox.position.x - this.boundingBox.size.x / 2.0),
                scene.scaleToDisplay(this.boundingBox.position.y - this.boundingBox.size.y / 2.0),
                scene.scaleToDisplay(this.boundingBox.size.x),
                scene.scaleToDisplay(this.boundingBox.size.y),
                null
        );

        g2d.setComposite(compositeBefore);
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject) {
        return false;
    }

    @Override
    public boolean collide(Scene scene, Entity entity) {
        return false;
    }
}
