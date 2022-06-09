package de.yonedash.smash;

import de.yonedash.smash.entity.*;
import de.yonedash.smash.resource.Texture;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SceneInGame extends Scene {

    private final Vec2D cameraPos;

    private final EntityPlayer player;

    private boolean zoomingOut = false;

    public SceneInGame(Instance instance) {
        super(instance);

        // Zoom from player to map
        this.zoomOut(7.5);

        this.player = new EntityPlayer(new BoundingBox(new Vec2D(0, 0), new Vec2D(40, 10)));
        instance.world.entitiesLoaded.add(this.player);
//        instance.world.entitiesLoaded.add(new EntityEnemy(new BoundingBox(new Vec2D(100, 100), new Vec2D(20, 20))));
//        instance.world.entitiesLoaded.add(new EntityEnemy(new BoundingBox(new Vec2D(150, 100), new Vec2D(20, 20))));
//        instance.world.entitiesLoaded.add(new EntityEnemy(new BoundingBox(new Vec2D(-70, 120), new Vec2D(20, 20))));

        // Initialize camera vec
        this.cameraPos = calculateCameraTargetPos().subtract(new Vec2D(instance.display.getWidth(), instance.display.getHeight()).multiply(0.5));

        // Load textures
        this.instance.atlas.load();

        // Load items
        this.instance.itemRegistry.load();

        player.setItemInHand(instance.itemRegistry.fork);
    }

    private void zoomOut(double scale) {
        this.zoomingOut = true;
        this.scaleFactor = scale;
    }

    private final Vec2D mousePosition = new Vec2D(0, 0);
    public final Vec2D mouseWorldPosition = new Vec2D(0, 0);

    @Override
    public void mouseDragged(int x, int y, int button) {
        updateMousePosition(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        updateMousePosition(x, y);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        updateMousePosition(x, y);
        updateMouseWorldPosition();

        Vec2D projSize = new Vec2D(20, 20);
        EntityProjectile proj = new EntityProjectile(
                new BoundingBox(this.player.getBoundingBox().center().clone().subtract(projSize.clone().multiply(0.5)),
                       projSize),
                this.player, this.player.getBoundingBox().center().rotationTo(this.mouseWorldPosition),
                0.75, 450.0);
        this.instance.world.entitiesLoaded.add(proj);
    }

    private void updateMousePosition(int x, int y) {
        this.mousePosition.x = x;
        this.mousePosition.y = y;
    }

    private void updateMouseWorldPosition() {
        double displayScale = calculateDisplayScaleFactor();
        this.mouseWorldPosition.x = (this.mousePosition.x + this.cameraPos.x) / displayScale;
        this.mouseWorldPosition.y = (this.mousePosition.y + this.cameraPos.y) / displayScale;
    }

    @Override
    public void keyPressed(int key) {
        if (key == KeyEvent.VK_H) {
            Constants.SHOW_CHUNK_BORDERS = !Constants.SHOW_CHUNK_BORDERS;
            Constants.SHOW_COLLISION = !Constants.SHOW_COLLISION;
        }
    }

    private double timeNoChunksRefreshed;

    @Override
    public void update(Graphics2D g2d, double dt) {
        Display display = getDisplay();
        final int width = display.getWidth(), height = display.getHeight();

        if (display.getInput().isKeyDown(KeyEvent.VK_Y)) {
            zoomOut(7.5);
        }
        if (this.zoomingOut) {
            if (this.scaleFactor > 1.0)
                this.scaleFactor -= time(0.003, dt);
            if (this.scaleFactor <= 1.0) {
                this.scaleFactor = 1.0;
                this.zoomingOut = false;
            }
        }

        // Update mouse world position
        updateMouseWorldPosition();

        g2d.setStroke(new BasicStroke(1));

        // Translate to camera position
        g2d.translate(-this.cameraPos.x, -this.cameraPos.y);

        // Load chunks

        // Create bounding box for camera view
        BoundingBox cameraView = new BoundingBox(this.cameraPos, new Vec2D(width, height));

        long chunkTime = System.currentTimeMillis();

        // Only refresh chunks with every Constants.CHUNK_REFRESH_FRAME_DELAY -> x-th frame
        double chunkRefreshDelay = Math.min(Constants.CHUNK_REFRESH_MAX_DELAY,
                1000.0 / (this.instance.gameLoop.getFramesPerSecond() * Constants.CHUNK_REFRESH_FRAME_DELAY));
        if ((this.timeNoChunksRefreshed += dt) >= chunkRefreshDelay) {
            // Reload chunks
            reloadChunks(cameraView);

            // Reset time
            this.timeNoChunksRefreshed = 0;
        }

        chunkTime = System.currentTimeMillis() - chunkTime;

        boolean playerColliding = false;

        long collisionTime = System.currentTimeMillis();

        int tilesLoaded = 0;

        int countDrawnOnScreen = 0;

        g2d.setStroke(new BasicStroke(scaleToDisplay(2)));

        ArrayList<DisplayEntity> zSortedLevelObjects = new ArrayList<>();

        // Update entities
        this.instance.world.entitiesLoaded.forEach(entity -> entity.update(this, dt));

        for (Chunk chunk : this.instance.world.chunksLoaded) {
            LevelObject[] levelObjects = chunk.getLevelObjects();
            tilesLoaded += levelObjects.length;

            // Handle collision with level
            for (Entity entity : this.instance.world.entitiesLoaded) {
                // Create a list which is going to be sorted by the tile's by distance to entity
                // This list is used for collision detection and the distance to the entity determines
                // the order in which the collision is going to be checked.
                // This way many bugs are bypassed.
                ArrayList<LevelObject> distanceSortedLevelObjects = new ArrayList<>(List.of(levelObjects));
                Vec2D posEntityCenter = entity.getBoundingBox().center();
                ;
                distanceSortedLevelObjects.sort(Comparator.comparingDouble(o -> o.getBoundingBox().center().distanceSqrt(posEntityCenter)));

                for (LevelObject levelObject : distanceSortedLevelObjects) {
                    for (BoundingBox bb : levelObject.getCollisionBoxes()) {
                        if (levelObject.hasCollision() && entity.getBoundingBox().isColliding(bb, 0) && entity.collide(this, levelObject)) {
                            entity.getBoundingBox().handleCollision(bb);
                        }
                    }
                }
            }

            // Add level objects from chunk to list
            zSortedLevelObjects.addAll(Arrays.stream(levelObjects).toList().stream().filter(obj -> createScaledToDisplay(obj.getBoundingBox()).isColliding(cameraView, 0)).toList());
        }

        // Handle collision with entities
        ArrayList<Entity> reversedEntityList = new ArrayList<>(this.instance.world.entitiesLoaded);
        Collections.reverse(reversedEntityList);
        for (Entity entity : reversedEntityList) {
            for (Entity entity2 : reversedEntityList) {
                if (entity == entity2)
                    continue;

                BoundingBox bb = entity2.getBoundingBox();
                if (entity.getBoundingBox().isColliding(bb, 0) && entity.collide(this, entity2) && entity2.collide(this, entity)) {
                    entity.getBoundingBox().handleCollision(bb);
                }
            }
        }

        // Add entities from chunk to list
        zSortedLevelObjects.addAll(this.instance.world.entitiesLoaded.stream().filter(entity -> createScaledToDisplay(entity.getBoundingBox()).isColliding(cameraView, 0)).toList());

        // Sort list by z value
        zSortedLevelObjects.sort(Comparator.comparingInt(DisplayEntity::getZ));

        // Update texture atlas
        this.instance.atlas.update(dt);

        // Draw chunk entities
        ArrayList<DisplayEntity> batch = new ArrayList<>();

        for (int i = 0; i < zSortedLevelObjects.size(); i++) {
            DisplayEntity current = zSortedLevelObjects.get(i);
            DisplayEntity next = i + 1 >= zSortedLevelObjects.size() ? null : zSortedLevelObjects.get(i + 1);

            batch.add(current);

            if (next != null && current.getZ() == next.getZ())
                continue;

            // Sort current batch by y value
            batch.sort(Comparator.comparingInt(DisplayEntity::getY));

            batch.forEach(displayEntity -> displayEntity.draw(this, g2d, dt));
            countDrawnOnScreen += batch.size();
            batch.clear();
        }

        if (Constants.SHOW_CHUNK_BORDERS || Constants.SHOW_COLLISION) {
            // Draw chunk borders
            g2d.setStroke(new BasicStroke(super.scaleToDisplay(2.0)));
            for (Chunk chunk : this.instance.world.chunksLoaded) {
                if (Constants.SHOW_COLLISION) {
                    for (LevelObject levelObject : chunk.getLevelObjects()) {
                        for (BoundingBox bb : levelObject.getCollisionBoxes()) {
                            g2d.setColor(new Color(200, 120, 150, 100));
                            g2d.fillRect(
                                    super.scaleToDisplay(bb.position.x),
                                    super.scaleToDisplay(bb.position.y),
                                    super.scaleToDisplay(bb.size.x),
                                    super.scaleToDisplay(bb.size.y)
                            );
                            g2d.setColor(new Color(0, 0, 0, 100));
                            g2d.drawRect(
                                    super.scaleToDisplay(bb.position.x),
                                    super.scaleToDisplay(bb.position.y),
                                    super.scaleToDisplay(bb.size.x),
                                    super.scaleToDisplay(bb.size.y)
                            );
                        }

                        BoundingBox bb = levelObject.getBoundingBox();
                        g2d.setColor(new Color(100, 100, 100, 200));
                        g2d.drawRect(
                                super.scaleToDisplay(bb.position.x),
                                super.scaleToDisplay(bb.position.y),
                                super.scaleToDisplay(bb.size.x),
                                super.scaleToDisplay(bb.size.y)
                        );
                    }
                }

                if (Constants.SHOW_CHUNK_BORDERS) {
                    g2d.setColor(Color.ORANGE);
                    BoundingBox bb = chunk.getBoundingBox();
                    g2d.drawRect(
                            super.scaleToDisplay(bb.position.x),
                            super.scaleToDisplay(bb.position.y),
                            super.scaleToDisplay(bb.size.x),
                            super.scaleToDisplay(bb.size.y)
                    );
                }
            }
        }

        collisionTime = System.currentTimeMillis() - collisionTime;

        // Draw mouse crosshair
        if (!this.zoomingOut) {
            int crosshairSize = super.scaleToDisplay(50.0);
            double crosshairRotation = mouseWorldPosition.rotationTo(player.getBoundingBox().center());
            GraphicsUtils.rotate(g2d, crosshairRotation, super.scaleToDisplay(mouseWorldPosition.x), super.scaleToDisplay(mouseWorldPosition.y));
            if (mouseWorldPosition.distanceSqrt(this.player.getBoundingBox().center()) < 450.0)
                g2d.setColor(Color.WHITE);
            else
                g2d.setColor(Color.GRAY);
            Texture texCrosshair = this.instance.atlas.crosshair;
            g2d.drawImage(texCrosshair.getBufferedImage(), super.scaleToDisplay(mouseWorldPosition.x) - crosshairSize / 2, super.scaleToDisplay(mouseWorldPosition.y) - crosshairSize / 2, crosshairSize, crosshairSize, null);
            GraphicsUtils.rotate(g2d, -crosshairRotation, super.scaleToDisplay(mouseWorldPosition.x), super.scaleToDisplay(mouseWorldPosition.y));
        }

        // Revert camera position translation
        g2d.translate(+this.cameraPos.x, +this.cameraPos.y);
        g2d.setColor(Color.WHITE);
        g2d.drawString("fps=" + (Math.round(this.instance.gameLoop.getFramesPerSecond() * 100.0) / 100.0), 50, 50);
        g2d.drawString("chunks=" + this.instance.world.chunksLoaded.size() + "/" + this.instance.world.chunks.size() + ", tiles=" + tilesLoaded + ", entities=" + this.instance.world.entitiesLoaded.size() + " " + countDrawnOnScreen + " drawn", 50, 60);
        g2d.drawString("dt=" + dt + "ms", 50, 70);
        g2d.drawString("chu_t=" + chunkRefreshDelay + "ms / " + chunkTime + "ms", 50, 80);
        g2d.drawString("col_t=" + collisionTime + "ms", 50, 90);

        // Update camera

        // Scale target position to display
        Vec2D cameraTargetScaledPos = super.createScaledToDisplay(calculateCameraTargetPos());
        // Calculate speed
        float cameraSpeed = super.time(0.0069f, dt);
        // Move camera by multiplying speed with position delta minus half the screen size (for screen center)
        this.cameraPos.x += cameraSpeed * (cameraTargetScaledPos.x - this.cameraPos.x - (width / 2f));
        this.cameraPos.y += cameraSpeed * (cameraTargetScaledPos.y - this.cameraPos.y - (height / 2f));

    }

    private void reloadChunks(BoundingBox cameraView) {
        // Clear current loaded chunks
        this.instance.world.chunksLoaded.clear();

        // Loop through every chunk
        for (Chunk chunk : this.instance.world.chunks) {
            // Now check if the chunk is on screen
            if (createScaledToDisplay(chunk.getBoundingBox()).isColliding(cameraView, 0)) {
                // If on screen, add to loaded chunks
                this.instance.world.chunksLoaded.add(chunk);
            }
        }
    }

    private Vec2D calculateCameraTargetPos() {
        // Create camera target pos with center position of players bounding box
        Vec2D cameraTargetPos = this.player.getBoundingBox().center();
        // Add/Subtract mouse position, to follow crosshair a bit
        if (this.zoomingOut)
            return cameraTargetPos;
        return cameraTargetPos.subtract(cameraTargetPos.clone().subtract(this.mouseWorldPosition).multiply(Constants.CAMERA_MOUSE_FOLLOW_FACTOR));
    }

}
