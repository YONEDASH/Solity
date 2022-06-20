package de.yonedash.smash.entity;

import de.yonedash.smash.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Comparator;

public class EntityEnemy extends EntityCharacter {

    private Entity target;

    protected double entityMoveSpeed = 1;

    public EntityEnemy(BoundingBox boundingBox) {
        super(boundingBox);
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        super.draw(scene, g2d, dt);
    }

    Vec2D targetPosition;
    int pathInterest;

    @Override
    public void update(Scene scene, double dt) {
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

        double maxDistance = Tile.TILE_SIZE * 10.0;

        super.update(scene, dt);
    }

    @Override
    protected void move(Scene scene, double dt, Vec2D moveMotion) {
        this.motion.add(moveMotion);
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject) {
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
