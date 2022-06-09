package de.yonedash.smash.entity;

import de.yonedash.smash.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class EntityEnemy extends EntityBase {

    private Entity target;

    public EntityEnemy(BoundingBox boundingBox) {
        super(boundingBox);
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(
                scene.scaleToDisplay(this.boundingBox.position.x),
                scene.scaleToDisplay(this.boundingBox.position.y),
                scene.scaleToDisplay(this.boundingBox.size.x),
                scene.scaleToDisplay(this.boundingBox.size.y)
        );
//
//        if (this.targetPosition != null) {
//            g2d.setColor(Color.GREEN);
//            g2d.drawRect(
//                    scene.scaleToDisplay(this.targetPosition.x - this.boundingBox.size.x / 2.0),
//                    scene.scaleToDisplay(this.targetPosition.y - this.boundingBox.size.y / 2.0),
//                    scene.scaleToDisplay(this.boundingBox.size.x),
//                    scene.scaleToDisplay(this.boundingBox.size.y)
//            );
//            g2d.setColor(Color.RED);
//            g2d.drawString(String.valueOf(this.pathInterest), scene.scaleToDisplay(this.targetPosition.x),
//                    scene.scaleToDisplay(this.targetPosition.y));
//
//            double rotation = this.boundingBox.center().rotationTo(this.targetPosition) - 90.0;
//            g2d.setColor(Color.WHITE);
//            AffineTransform tx = g2d.getTransform();
//            double rad = Math.toRadians(rotation);
//            tx.rotate(rad, scene.scaleToDisplay(this.boundingBox.center().x), scene.scaleToDisplay(this.boundingBox.center().y));
//            g2d.setTransform(tx);
//            g2d.drawRect(scene.scaleToDisplay(this.boundingBox.center().x - 5), scene.scaleToDisplay(this.boundingBox.center().y), 10, 50);
//            tx.rotate(-rad, scene.scaleToDisplay(this.boundingBox.center().x), scene.scaleToDisplay(this.boundingBox.center().y));
//            g2d.setTransform(tx);
//
//            Vec2D test = boundingBox.center().clone().add(this.boundingBox.center().rotationTo(this.targetPosition), 100.0);
//            g2d.setColor(Color.ORANGE);
//            g2d.drawRect(
//                    scene.scaleToDisplay(test.x - this.boundingBox.size.x / 2.0),
//                    scene.scaleToDisplay(test.y - this.boundingBox.size.y / 2.0),
//                    scene.scaleToDisplay(this.boundingBox.size.x),
//                    scene.scaleToDisplay(this.boundingBox.size.y)
//            );
//        }
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

        Vec2D posEntityCenter = this.boundingBox.center();
        if (this.target != null) {
            // Get distance between enemy and target
            double distance = this.target.getBoundingBox().center().distanceSqrt(posEntityCenter);
            // If distance is to great, forget about this target
            if (distance > 1200.0) {
                this.target = null;
                return;
            }
        } else {
            // Try to find closest target
            ArrayList<Entity> distanceSortedEntities = new ArrayList<>(scene.instance.world.entitiesLoaded);
            distanceSortedEntities.sort(Comparator.comparingDouble(o -> o.getBoundingBox().center().distanceSqrt(posEntityCenter)));

            int i = 0;
            Entity tempTarget = null;
            while (i < distanceSortedEntities.size() && (tempTarget = distanceSortedEntities.get(i)) instanceof EntityEnemy) {
                i++;
            }

            if (i < distanceSortedEntities.size())
                this.target = distanceSortedEntities.get(i);

            // If there was no target found, do not pathfind
            if (this.target == null) {
                // Reset target pos
                this.targetPosition = null;
                return;
            }
        }

        double moveSpeed = scene.time(0.2, dt);

        if (this.targetPosition != null) {
            double distanceToTargetPos = this.targetPosition.distanceSqrt(this.boundingBox.center());
            double distanceToTarget = this.target.getBoundingBox().center().distanceSqrt(this.boundingBox.center());

            if (distanceToTarget < distanceToTargetPos || MathUtils.rayCast(scene.instance.world, this.boundingBox, this.boundingBox.center().rotationTo(this.target.getBoundingBox().center()), moveSpeed, distanceToTarget) == null) {
                this.targetPosition = null;
            } else if (distanceToTargetPos < (pathInterest > 0 ? this.target.getBoundingBox().size.average() : moveSpeed)) {
                this.targetPosition = null;
            }
        }

        if (this.targetPosition == null) {
            this.targetPosition = this.target.getBoundingBox().center();
            this.pathInterest = 2;
        }

        double dx = this.targetPosition.x - posEntityCenter.x;
        double dy = this.targetPosition.y - posEntityCenter.y;

        double mx = dx > 0 ? Math.min(moveSpeed, dx) : Math.max(-moveSpeed, dx);
        double my = dy > 0 ? Math.min(moveSpeed, dy) : Math.max(-moveSpeed, dy);

        Vec2D position = this.boundingBox.position;
        Vec2D move = new Vec2D(0, 0);
        move.x += mx;
        move.y += my;

        // check if target intersects with any level objects collision
        double rotation = this.boundingBox.center().rotationTo(this.targetPosition);
        LevelObject casted = MathUtils.rayCast(scene.instance.world, this.boundingBox, rotation, moveSpeed, this.boundingBox.size.average() * 2);
        BoundingBox intersectingObj = casted != null ? casted.getBoundingBox() : null;

        if (intersectingObj != null) {
            move.x -= mx;
            move.y -= my;

            Vec2D newTarget = position.clone().add(this.boundingBox.size.clone().multiply(0.5));
            boolean foundWay = false;
            int maxIterations = 16 * 4;
            int iteration = 0;
            while (iteration <= maxIterations) {
                double r = Math.random();
                int f1 = r > .5 ? 1 : -1;
                double f2 = -f1 * r * 128 * moveSpeed;
                boolean invalid = false;

                int direction = iteration % 4;

                switch (direction) {
                    case 0 -> newTarget.x += moveSpeed * f1 * f2;
                    case 1 -> newTarget.y += moveSpeed * f1 * f2;
                    case 2 -> newTarget.x -= moveSpeed * f1 * f2;
                    case 3 -> newTarget.y -= moveSpeed * f1 * f2;
                }

                BoundingBox test = new BoundingBox(newTarget.clone().add(new Vec2D(-this.boundingBox.size.x / 2.0, -this.boundingBox.size.y / 2.0)), this.boundingBox.size);
                for (Chunk chunk : scene.instance.world.chunksLoaded) {
                    for (LevelObject obj : chunk.getLevelObjects()) {
                        BoundingBox bb = obj.getBoundingBox();
                        if (obj.hasCollision() && bb.isColliding(test, 0)) {
                            invalid = true;

                            Vec2D vec2D = bb.intersect(test);
                            if (vec2D == null)
                                continue;
                            newTarget.add(vec2D);
                            break;
                        }
                    }
                    if (invalid)
                        break;
                }

                if (!invalid && iteration > 3 + (r * maxIterations))
                    break;

                iteration++;
            }

            this.targetPosition = new Vec2D((int) (newTarget.x / moveSpeed) * moveSpeed, (int) (newTarget.y / moveSpeed) * moveSpeed);
            this.pathInterest = 0;


            dx = this.targetPosition.x - posEntityCenter.x;
            dy = this.targetPosition.y - posEntityCenter.y;

            mx = dx > 0 ? Math.min(moveSpeed, dx) : Math.max(-moveSpeed, dx);
            my = dy > 0 ? Math.min(moveSpeed, dy) : Math.max(-moveSpeed, dy);

            move.x += mx;
            move.y += my;
        }

        this.motion.add(move);

        if (Math.abs(move.x) > Math.abs(move.y)) {
            if (move.x > 0)
                this.viewDirection = Direction.EAST;
            else
                this.viewDirection = Direction.WEST;
        } else {
            if (move.y > 0)
                this.viewDirection = Direction.SOUTH;
            else
                this.viewDirection = Direction.NORTH;
        }

        super.update(scene, dt);
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
