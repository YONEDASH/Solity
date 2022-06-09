package de.yonedash.smash;

import de.yonedash.smash.entity.LevelObject;

public class MathUtils {

    public static LevelObject rayCast(World world, Vec2D vec2D, double rotation, double stepSize, double maxDistance) {
        Vec2D point = vec2D.clone();
        stepSize = Math.max(1, stepSize);
        maxDistance = Math.max(1, maxDistance);
        double d = 0;
        while (d < maxDistance + stepSize) {
            for (Chunk chunk : world.chunksLoaded) {
                for (LevelObject levelObject : chunk.getLevelObjects()) {
                    BoundingBox bb = levelObject.getBoundingBox();
                    if (bb.contains(point)) {
                        return levelObject;
                    }
                }
            }
            point = point.add(rotation, stepSize);
            d += stepSize;
        }

        return null;
    }



    public static LevelObject rayCast(World world, BoundingBox boundingBox, double rotation, double stepSize, double maxDistance) {
        boundingBox = boundingBox.clone();
        stepSize = Math.max(1, stepSize);
        maxDistance = Math.max(1, maxDistance);
        double d = 0;
        while (d < maxDistance + stepSize) {
            for (Chunk chunk : world.chunksLoaded) {
                for (LevelObject levelObject : chunk.getLevelObjects()) {
                    BoundingBox bb = levelObject.getBoundingBox();
                    if (levelObject.hasCollision() && bb.isColliding(boundingBox, 0)) {
                        return levelObject;
                    }
                }
            }
            boundingBox.position.add(rotation, stepSize);
            d += stepSize;
        }

        return null;
    }


}
