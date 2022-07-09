package de.yonedash.smash;

import de.yonedash.smash.entity.*;
import de.yonedash.smash.graphics.EntityFog;
import de.yonedash.smash.graphics.GraphicsUtils;
import de.yonedash.smash.graphics.VisualEffect;
import de.yonedash.smash.resource.Texture;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class SceneInGame extends Scene {

    private final Vec2D cameraPos;

    private final EntityPlayer player;

    private boolean zoomingOut = false;

    public SceneInGame(Instance instance) {
        super(instance);

        this.player = new EntityPlayer(new BoundingBox(new Vec2D(0, 0), new Vec2D(40 * 2, 10 * 2)));
        instance.world.entitiesLoaded.add(this.player);

        // Initialize camera vec
        this.cameraPos = calculateCameraTargetPos().subtract(new Vec2D(instance.display.getWidth(), instance.display.getHeight()).multiply(0.5));

        // Load fonts
        this.instance.lexicon.load();

        // Load textures
        this.instance.atlas.load();

        // Load items
        this.instance.itemRegistry.load();

        player.setItemInHand(instance.itemRegistry.fork);

        // Scatter enemies
        for (int i = 0; i < 500; i++) {
            EntityAnt ant = new EntityAnt(new BoundingBox(findEnemySpawn(), new Vec2D(60, 12)));
            this.instance.world.entitiesLoaded.add(ant);
        }

        // Zoom from player to map
        this.zoomOut(7.5);

        OpenSimplexNoise noise = new OpenSimplexNoise(123);
    }

    private Vec2D findEnemySpawn() {
        World world = this.instance.world;
        double x = (world.topLeft.x + world.bottomRight.x) * Math.random();
        double y = (world.topLeft.y + world.bottomRight.y) * Math.random();
        double space = Tile.TILE_SIZE * 1.5;
        BoundingBox spawn = new BoundingBox(new Vec2D(x - (space / 2), y - (space / 2)), new Vec2D(space, space));
        for (Chunk chunk : this.instance.world.chunks) {
            if (Arrays.stream(chunk.getLevelObjects()).anyMatch(levelObject -> levelObject.hasCollision() && Arrays.stream(levelObject.getCollisionBoxes()).anyMatch(boundingBox -> boundingBox.isColliding(spawn, 0))))
                return findEnemySpawn();
        }
        return spawn.center();
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

        Vec2D projSize = new Vec2D(40, 40);
        EntityProjectile proj = new EntityProjectile(
                new BoundingBox(this.player.getBoundingBox().center().clone().subtract(projSize.clone().multiply(0.5)),
                       projSize),
                this.player, this.player.getBoundingBox().center().rotationTo(this.mouseWorldPosition),
                0.875, Tile.TILE_SIZE * 5);
        this.instance.world.entitiesLoaded.add(proj);

        int particleCount = (int) (proj.getMoveSpeed() / 0.1 * Constants.PARTICLE_PROJECTILE_COUNT_FACTOR);
        double dirSplitDeg = 5.5;
        boolean dirSplit = false;
        for (int i = 0; i < particleCount; i++) {
            double f = 0.8 * ((i + 1) / (double) particleCount);
            for (int d = 0; d <= (dirSplit ? 2 : 0); d++) {
                EntityParticle particle = new EntityParticle(proj.getBoundingBox().clone(), this.instance.atlas.animSlash, proj.getRotation() + (d == 1 ? dirSplitDeg : d == 2 ? -dirSplitDeg : 0.0), proj.getMoveSpeed() * f, 300.0 * f, proj.getZ() + 1, true);
                this.instance.world.entitiesLoaded.add(particle);
            }
        }
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
        if (key == KeyEvent.VK_M) {
            scaleFactor += 0.05;
        }
        if (key == KeyEvent.VK_N) {
            scaleFactor -= 0.05;
            if (scaleFactor <= 0)
                scaleFactor = 0.05;
        }
    }

    private double timeNoChunksRefreshed;

    double testD;

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

        World world = this.instance.world;

        // Update fog variables

        // Update condition (f.e. this variable controls density)
        world.weatherOffset += super.time(0.00001, dt);
        // Update weather progress
        world.weatherProgress = world.simplexNoise.eval(world.weatherOffset, 0.0);
        // Make fog move
        world.fogOffset += super.time(world.weatherProgress > 0 ? -0.1 : 0.1, dt);


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

        long collisionTime = System.currentTimeMillis();

        int tilesLoaded = 0;

        int countDrawnOnScreen = 0;

        g2d.setStroke(new BasicStroke(scaleToDisplay(2)));

        ArrayList<DisplayEntity> zSortedLevelObjects = new ArrayList<>();

        // Update entities
        this.instance.world.entitiesLoaded.forEach(entity -> entity.update(this, dt));

        ArrayList<Entity> entitiesToCheckCollision = new ArrayList<>(this.instance.world.entitiesLoaded);

        // No need to check collision for visual effects and  for entities which are not in a loaded chunk
        entitiesToCheckCollision.removeIf(entity -> entity instanceof VisualEffect || this.instance.world.chunksLoaded.stream().noneMatch(chunk -> entity.getBoundingBox().isColliding(chunk.getBoundingBox(), 0)));

        boolean emitParticlesForLoadedTiles = Constants.PARTICLE_EMIT_IN_LOADED_CHUNKS;

        for (Chunk chunk : this.instance.world.chunksLoaded) {
            LevelObject[] levelObjects = chunk.getLevelObjects();
            tilesLoaded += levelObjects.length;

            // Handle collision with level
            for (Entity entity : entitiesToCheckCollision) {
                // Create a list which is going to be sorted by the tile's by distance to entity
                // This list is used for collision detection and the distance to the entity determines
                // the order in which the collision is going to be checked.
                // This way many bugs are bypassed.
                ArrayList<LevelObject> distanceSortedLevelObjects = new ArrayList<>(List.of(levelObjects));
                Vec2D posEntityCenter = entity.getBoundingBox().center();

                distanceSortedLevelObjects.sort(Comparator.comparingDouble(o -> o.getBoundingBox().center().distanceSqrt(posEntityCenter)));

                for (LevelObject levelObject : distanceSortedLevelObjects) {
                    for (BoundingBox bb : levelObject.getCollisionBoxes()) {
                        if (levelObject.hasCollision() && entity.getBoundingBox().isColliding(bb, 0) && entity.collide(this, levelObject, bb)) {
                            entity.getBoundingBox().handleCollision(bb);
                        }
                    }
                }
            }

            // Try to emit particles for each tile
            if (emitParticlesForLoadedTiles)
                Arrays.stream(levelObjects).filter(levelObject -> levelObject instanceof Tile).forEach(tile -> ((Tile) tile).emitParticles(this, dt));

            // Add level objects from chunk to list to draw them later
            zSortedLevelObjects.addAll(Arrays.stream(levelObjects).toList().stream().filter(obj -> createScaledToDisplay(obj.getBoundingBox()).isColliding(cameraView, 0)).toList());
        }


        // Handle collision with entities
        ArrayList<Entity> reversedEntityList = new ArrayList<>(entitiesToCheckCollision);
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

        // If fog is disabled remove fog from list
        if (!Constants.FOG_ENABLED)
            zSortedLevelObjects.removeIf(displayEntity -> displayEntity instanceof EntityFog);

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

            // Draw entities
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

        world.waypoint = new Vec2D(82 * Tile.TILE_SIZE, 58 * Tile.TILE_SIZE);
        // Draw waypoint arrow
        if (world.waypoint != null) {
            BoundingBox playerBB = player.getBoundingBox();
            Vec2D center = playerBB.center();
            double rotationToWaypoint = center.rotationTo(world.waypoint);
            double arrowDistance = world.waypoint.distanceSqrt(center);
            double playerDistance = Tile.TILE_SIZE * 1.5;
            double minDistance = Tile.TILE_SIZE * 1.3;
            boolean isClose = arrowDistance < minDistance;
            double arrowSize = 100.0;

            Vec2D arrowPos = !isClose ? center.clone().add(rotationToWaypoint, Math.min(arrowDistance, playerDistance))
                    : world.waypoint.clone().add(-90, arrowSize * 1.25);

            rotationToWaypoint -= 90.0;

            if (isClose) {
                rotationToWaypoint = 0.0;

                // Up and down animation
                double d = ((System.currentTimeMillis() % 1000) / 1000.0) * 2;

                if (d > 1)
                    d = 1 - d + 1;
                d /= 2;

                double bounceY = arrowSize * d;
                arrowPos.add(new Vec2D(0, bounceY));
            }

            GraphicsUtils.setAlpha(g2d, 0.75f);

            GraphicsUtils.rotate(g2d, rotationToWaypoint, super.scaleToDisplay(arrowPos.x), super.scaleToDisplay(arrowPos.y));
            g2d.drawImage(this.instance.atlas.arrow.getImage(), super.scaleToDisplay(arrowPos.x - arrowSize / 2),
                    super.scaleToDisplay(arrowPos.y - arrowSize / 2), super.scaleToDisplay(arrowSize), super.scaleToDisplay(arrowSize), null);
            GraphicsUtils.rotate(g2d, -rotationToWaypoint, super.scaleToDisplay(arrowPos.x), super.scaleToDisplay(arrowPos.y));

            GraphicsUtils.setAlpha(g2d, 1.0f);
        }


        // Draw mouse crosshair
        if (!this.zoomingOut) {
            int crosshairSize = super.scaleToDisplay(100.0);
            double crosshairRotation = mouseWorldPosition.rotationTo(player.getBoundingBox().center());
            GraphicsUtils.rotate(g2d, crosshairRotation, super.scaleToDisplay(mouseWorldPosition.x), super.scaleToDisplay(mouseWorldPosition.y));
            if (mouseWorldPosition.distanceSqrt(this.player.getBoundingBox().center()) < 450.0)
                g2d.setColor(Color.WHITE);
            else
                g2d.setColor(Color.GRAY);
            Texture texCrosshair = this.instance.atlas.crossHair;
            g2d.drawImage(texCrosshair.getImage(), super.scaleToDisplay(mouseWorldPosition.x) - crosshairSize / 2, super.scaleToDisplay(mouseWorldPosition.y) - crosshairSize / 2, crosshairSize, crosshairSize, null);
            GraphicsUtils.rotate(g2d, -crosshairRotation, super.scaleToDisplay(mouseWorldPosition.x), super.scaleToDisplay(mouseWorldPosition.y));
        }

        // Revert camera position translation
        g2d.translate(+this.cameraPos.x, +this.cameraPos.y);

        // Draw HUD

        // Set font
        g2d.setFont(this.instance.lexicon.equipmentPro.deriveFont((float) scaleToDisplay(40.0)));

        // Draw Player Health
        g2d.setColor(Color.RED);
        this.fontRenderer.drawString(g2d, "Player Health: " + null, scaleToDisplay(20.0), scaleToDisplay(30.0), FontRenderer.LEFT, FontRenderer.TOP, true);

        // Draw Player Dashes
        g2d.setColor(Color.GREEN);
        this.fontRenderer.drawString(g2d, "Player Dashes Left: " + null, scaleToDisplay(20.0), scaleToDisplay(90.0), FontRenderer.LEFT, FontRenderer.TOP, true);

        // Draw Player Heal Potions
        g2d.setColor(Color.CYAN);
        this.fontRenderer.drawString(g2d, "Player Heal Potions Left: " + null, scaleToDisplay(20.0), scaleToDisplay(150.0), FontRenderer.LEFT, FontRenderer.TOP, true);

        // Draw Player Inventory
        // Draw Item in Hand
        g2d.setColor(Color.YELLOW);
        this.fontRenderer.drawString(g2d, "Item In Hand: " + player.getItemInHand().getName(), scaleToDisplay(20.0), scaleToDisplay(210.0), FontRenderer.LEFT, FontRenderer.TOP, true);

        // Draw Wave
        g2d.setColor(new Color(239, 93, 93));
        g2d.setFont(this.instance.lexicon.compassPro.deriveFont((float) scaleToDisplay(90.0)));
        BoundingBox waveTextBounds = this.fontRenderer.drawString(g2d, "Wave " + null, width / 2, height - scaleToDisplay(140.0), FontRenderer.CENTER, FontRenderer.BOTTOM, true);
        g2d.setColor(new Color(208, 202, 202));
        g2d.setFont(this.instance.lexicon.compassPro.deriveFont((float) scaleToDisplay(40.0)));
        this.fontRenderer.drawString(g2d, null + " kills", width / 2, (int) (waveTextBounds.position.y + waveTextBounds.size.y) + scaleToDisplay(20.0), FontRenderer.CENTER, FontRenderer.TOP, true);

        // Set font
        g2d.setFont(this.instance.lexicon.equipmentPro.deriveFont((float) scaleToDisplay(50.0)));
        g2d.setColor(Color.WHITE);

        Runtime runtime = Runtime.getRuntime();
        double memoryUsage = (runtime.totalMemory() - runtime.freeMemory()) * 1e-6;
        double memoryTotal = (runtime.totalMemory()) * 1e-6;
        this.fontRenderer.drawString(g2d, (Math.round(this.instance.gameLoop.getFramesPerSecond() * 10.0) / 10.0) + " FPS, " + (Math.round(memoryUsage * 10.0) / 10.0) + "M / " +  (Math.round(memoryTotal * 10.0) / 10.0) + "M", width / 2, super.scaleToDisplay(50.0 / this.scaleFactor), FontRenderer.CENTER, FontRenderer.TOP,false);

        if (Constants.SHOW_COLLISION || Constants.SHOW_CHUNK_BORDERS) {
            String[] extraInfo = {
                    "chunks=" + this.instance.world.chunksLoaded.size() + "/" + this.instance.world.chunks.size() + ", tiles=" + tilesLoaded + ", entities=" + this.instance.world.entitiesLoaded.size() + " " + countDrawnOnScreen + " drawn",
                    "dt=" + dt + "ms",
                    "chu_t=" + chunkRefreshDelay + "ms / " + chunkTime + "ms",
                    "col_t=" + collisionTime + "ms",
                    "weather=" + world.weatherProgress
            };
            BoundingBox genericBounds = this.fontRenderer.bounds(g2d, "X");
            Vec2D infoPos = new Vec2D(50, 400 + genericBounds.size.y + Math.abs(genericBounds.position.y));
            for (String info : extraInfo) {
                BoundingBox bounds = this.fontRenderer.drawString(g2d, info, super.scaleToDisplay((infoPos.x) / this.scaleFactor), super.scaleToDisplay((infoPos.y) / this.scaleFactor), FontRenderer.LEFT, FontRenderer.TOP,true);
                infoPos.add(new Vec2D(0, bounds.size.y * 2));
            }
        }

        // Update camera

        // Scale target position to display
        Vec2D cameraTargetScaledPos = super.createScaledToDisplay(this.calculateCameraTargetPos());
        // Calculate speed
        float cameraSpeed = super.time(0.003969420f, dt);
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

                // Add entities
                if (chunk.getEntities().size() > 0) {
                    this.instance.world.entitiesLoaded.addAll(chunk.getEntities());
                    chunk.getEntities().clear();
                }
            }
        }

        // Unload entities
        for (Entity entity : this.instance.world.entitiesLoaded) {
            if (this.instance.world.chunksLoaded.stream().noneMatch(chunk -> entity.getBoundingBox().isColliding(chunk.getBoundingBox(), 0))) {
                this.instance.world.entitiesLoaded.remove(entity);
                for (Chunk chunk : this.instance.world.chunks) {
                    if (chunk.getBoundingBox().isColliding(entity.getBoundingBox(), 0)) {
                        chunk.getEntities().add(entity);
                        break;
                    }
                }
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
