package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.graphics.GraphicsUtils;
import de.yonedash.smash.resource.Texture;
import de.yonedash.smash.graphics.TextureIndividual;

import java.awt.*;

public class EntityParticle extends EntityBase implements VisualEffect {

    private final TextureIndividual texture;
    private final int z;

    private double rotationDirection, moveSpeed, timeAlive;
    private final double targetTimeAlive;

    private boolean rotate;

    public EntityParticle(BoundingBox boundingBox, Texture texture, double rotationDirection, double moveSpeed, double targetTimeAlive, int z, boolean rotate) {
        super(boundingBox);
        this.texture = new TextureIndividual(texture);
        this.texture.setLooping(false);
        this.z = z;
        this.targetTimeAlive = targetTimeAlive;
        this.rotate = rotate;
        changeDirection(rotationDirection, moveSpeed);
    }

    public void changeDirection(double rotationDirection, double moveSpeed) {
        this.rotationDirection = rotationDirection;
        this.moveSpeed = moveSpeed;
    }

    @Override
    public void update(Scene scene, double dt) {
        double moveSpeed = scene.time(this.moveSpeed, dt);
        this.motion.add(this.rotationDirection, moveSpeed);

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
        this.texture.update(dt);

        Vec2D center = this.boundingBox.center();
        double rotationDegView = this.rotationDirection + 0.0;

        double progress = this.timeAlive / this.targetTimeAlive;
        double alpha = progress > 0.8 ? 1.0 - ((progress - 0.8) / 0.2) : 1;

        final Composite compositeBefore = g2d.getComposite();
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) alpha);
        g2d.setComposite(alphaComposite);

        GraphicsUtils.rotate(g2d, rotationDegView, scene.scaleToDisplay(center.x), scene.scaleToDisplay(center.y));
        g2d.drawImage(
                this.texture.getImage(),
                scene.scaleToDisplay(this.boundingBox.position.x), scene.scaleToDisplay(this.boundingBox.position.y),
                scene.scaleToDisplay(this.boundingBox.size.x), scene.scaleToDisplay(this.boundingBox.size.y),
                null
        );
        GraphicsUtils.rotate(g2d, -rotationDegView, scene.scaleToDisplay(center.x), scene.scaleToDisplay(center.y));

        g2d.setComposite(compositeBefore);
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject, BoundingBox objectBoundingBox) {
        return false;
    }

    @Override
    public boolean collide(Scene scene, Entity entity) {
        return false;
    }
}
