package de.yonedash.smash;

import de.yonedash.smash.entity.LevelObject;

import java.util.List;

public class MathUtils {

    public static Vec2D findInterceptionPoint(Vec2D origin1, Vec2D motion1, Vec2D origin2, Vec2D motion2, double length) {
        Vec2D s1 = origin1.clone();
        Vec2D s2 = origin1.clone().add(motion1.clone().multiply(length));
        Vec2D d1 = origin2.clone();
        Vec2D d2 = origin2.clone().add(motion2.clone().multiply(length));

        double a1 = s2.y - s1.y;
        double b1 = s1.x - s2.x;
        double c1 = a1 * s1.x + b1 * s1.y;

        double a2 = d2.y - d1.y;
        double b2 = d1.x - d2.x;
        double c2 = a2 * d1.x + b2 * d1.y;

        double delta = a1 * b2 - a2 * b1;

        if (delta == 0)
            return null;

        return new Vec2D(((b2 * c1 - b1 * c2) / delta), ((a1 * c2 - a2 * c1) / delta));
    }

    public static LevelObject rayCast(World world, Vec2D vec2D, double rotation, double stepSize, double maxDistance) {
        return rayCast(world, vec2D, rotation, stepSize, maxDistance, true);
    }

    public static LevelObject rayCast(World world, Vec2D vec2D, double rotation, double stepSize, double maxDistance, boolean restrictToLoadedChunks) {
        Vec2D point = vec2D.clone();
        stepSize = Math.max(1, stepSize);
        maxDistance = Math.max(1, maxDistance);
        double d = 0;
        List<Chunk> chunkList = restrictToLoadedChunks ? world.chunksLoaded.stream().toList() : world.chunks;
        while (d < maxDistance + stepSize) {
            for (Chunk chunk : chunkList) {
                if (!chunk.getBoundingBox().contains(point))
                    continue;
                for (LevelObject levelObject : chunk.getLevelObjects()) {
                    if (!levelObject.hasCollision())
                        continue;
                    for (BoundingBox bb : levelObject.getCollisionBoxes()) {
                        if (bb.contains(point)) {
                            return levelObject;
                        }
                    }
                }
            }
            point = point.add(rotation, stepSize);
            d += stepSize;
        }

        return null;
    }

    public static LevelObject rayCast(World world, BoundingBox boundingBox, double rotation, double stepSize, double maxDistance) {
        return rayCast(world, boundingBox, rotation, stepSize, maxDistance, true);
    }

    public static LevelObject rayCast(World world, BoundingBox boundingBox, double rotation, double stepSize, double maxDistance, boolean restrictToLoadedChunks) {
        boundingBox = boundingBox.clone();
        stepSize = Math.max(1, stepSize);
        maxDistance = Math.max(1, maxDistance);
        double d = 0;
        List<Chunk> chunkList = restrictToLoadedChunks ? world.chunksLoaded.stream().toList() : world.chunks;
        while (d < maxDistance + stepSize) {
            for (Chunk chunk : chunkList) {
                if (!chunk.getBoundingBox().isColliding(boundingBox, 0))
                    continue;
                for (LevelObject levelObject : chunk.getLevelObjects()) {
                    if (!levelObject.hasCollision())
                        continue;
                    for (BoundingBox bb : levelObject.getCollisionBoxes()) {
                        if (bb.isColliding(boundingBox, 0)) {
                            return levelObject;
                        }
                    }
                }
            }

            boundingBox.position.add(rotation, stepSize);
            d += stepSize;
        }


        return null;
    }


}
