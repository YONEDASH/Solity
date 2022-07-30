package de.yonedash.smash;

import de.yonedash.smash.config.InputConfig;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.entity.*;
import de.yonedash.smash.graphics.EntityFog;
import de.yonedash.smash.graphics.GraphicsUtils;
import de.yonedash.smash.graphics.TextureAnimated;
import de.yonedash.smash.graphics.VisualEffect;
import de.yonedash.smash.localization.BindLocalizer;
import de.yonedash.smash.progression.story.TutorialStory;
import de.yonedash.smash.progression.skills.SkillDash;
import de.yonedash.smash.resource.Texture;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class SceneInWorld extends Scene {

    public final Vec2D cameraPos;

    public final EntityPlayer player;

    private double promptLettersRevealed = 0;
    private double promptAutoNextTimer = 0;

    public SceneInWorld(Instance instance) {
        super(instance);

        World world = this.instance.world;

        // Add player
        this.player = new EntityPlayer(Vec2D.zero());
        this.resetPlayerStats();
        world.entitiesLoaded.add(this.player);

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
//        for (int i = 0; i < 500; i++) {
//            EntityAnt ant = new EntityAnt(findEnemySpawn());
//            this.instance.world.entitiesLoaded.add(ant);
//        }

        // Init & start story
        world.story = new TutorialStory();
        world.story.start();
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

    public final Vec2D mousePosition = new Vec2D(0, 0);
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

        // TODO add device compat
        Vec2D projSize = new Vec2D(40, 40);
        EntityProjectile proj = new EntityProjectile(
                instance.atlas.fork,
                new BoundingBox(this.player.getBoundingBox().center().clone().subtract(projSize.clone().multiply(0.5)),
                       projSize),
                this.player, this.player.getBoundingBox().center().rotationTo(this.mouseWorldPosition),
                0.875, 1.0, Tile.TILE_SIZE * 5);
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
    public void devicePressed(KeyBind.Device device, int code) {
        InputConfig inputConfig = this.instance.inputConfig;
        KeyBind skipDialog = inputConfig.getBind("skipDialog");

        if (skipDialog.equals(device, code) && !skipDialog.isLocked()) {
            // Lock bind in order to prevent holding down the button to keep doing this action
            skipDialog.lock();

            TextPrompt prompt = this.instance.world.getPrompt();
            if (prompt == null)
                return;

            int promptTextLength = prompt.text().length();
            if (promptLettersRevealed < promptTextLength)
                promptLettersRevealed = promptTextLength + 10;
            else if (prompt.waitTime() != TextPrompt.UNSKIPPABLE)
                promptAutoNextTimer = (prompt.waitTime() + 10) * 1000.0;


        }
    }

    @Override
    public void deviceReleased(KeyBind.Device device, int code) {
        InputConfig inputConfig = this.instance.inputConfig;
        KeyBind skipDialog = inputConfig.getBind("skipDialog");

        if (skipDialog.equals(device, code) && skipDialog.isLocked()) {
            skipDialog.unlock();
        }

        KeyBind dash = inputConfig.getBind("dash");

        if (dash.equals(device, code) && dash.isLocked()) {
            dash.unlock();
        }
    }

    @Override
    public void keyPressed(int key) {
        if (key == KeyEvent.VK_H) {
            Constants.SHOW_CHUNK_BORDERS = !Constants.SHOW_CHUNK_BORDERS;
            Constants.SHOW_COLLISION = !Constants.SHOW_COLLISION;
        }
        if (key == KeyEvent.VK_M) {
            instance.world.story.initStep(0);
        }
        if (key == KeyEvent.VK_N) {
            instance.world.story.nextStep();
        }
        if (key == KeyEvent.VK_K) {
            player.setHealth(0);
        }
        if (key == KeyEvent.VK_P) {
            Vec2D center = player.getBoundingBox().center();
            System.out.println("Player Position: " + center + " new Vec2D(" + center.x + ", " + center.y + ")");
        }
        if (key == KeyEvent.VK_R) {
            this.promptLettersRevealed = 0.0;
        }
    }

    private double timeNoChunksRefreshed;

    private TextPrompt currentPrompt;

    private double timeSincePlayerDeath;
    private boolean checkpointLoaded;

    private double healthDisplayed, dashesDisplayed;

    @Override
    public void update(Graphics2D g2d, double dt) {
        Display display = getDisplay();
        final int width = display.getWidth(), height = display.getHeight();

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

        // Draw character health bars
        for (Entity e : world.entitiesLoaded.stream().filter(entity -> entity instanceof EntityCharacter).toList()) {
            EntityCharacter entityCharacter = (EntityCharacter) e;

            if (entityCharacter != player && cameraView.isColliding(createScaledToDisplay(entityCharacter.getBoundingBox()), 0)) {
                entityCharacter.drawHealthBar(g2d, this);
            }
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

        drawWaypointArrow(g2d, world);

        // Draw mouse crosshair
        int crosshairSize = super.scaleToDisplay(100.0);
        double crosshairRotation = mouseWorldPosition.rotationTo(player.getBoundingBox().center());
        GraphicsUtils.rotate(g2d, crosshairRotation, super.scaleToDisplay(mouseWorldPosition.x), super.scaleToDisplay(mouseWorldPosition.y));
        if (mouseWorldPosition.distanceSqrt(this.player.getBoundingBox().center()) < 450.0)
            g2d.setColor(Color.WHITE);
        else
            g2d.setColor(Color.GRAY);
        Texture texCrosshair = this.instance.atlas.uiCrossHair;
        g2d.drawImage(texCrosshair.getImage(), super.scaleToDisplay(mouseWorldPosition.x) - crosshairSize / 2, super.scaleToDisplay(mouseWorldPosition.y) - crosshairSize / 2, crosshairSize, crosshairSize, null);
        GraphicsUtils.rotate(g2d, -crosshairRotation, super.scaleToDisplay(mouseWorldPosition.x), super.scaleToDisplay(mouseWorldPosition.y));

        // Revert camera position translation
        g2d.translate(+this.cameraPos.x, +this.cameraPos.y);

        // Update story
        if (world.story != null) {
            world.story.update(g2d, dt, this);
        }

        // Draw HUD
        drawHUD(g2d, width, height, world, dt);

        // Handle death and draw overlay
       handlePlayerDeath(g2d, width, height, dt);

        // Set font
        g2d.setFont(this.instance.lexicon.arial.deriveFont((float) scaleToDisplay(32.0)));
        g2d.setColor(Color.WHITE);

        Runtime runtime = Runtime.getRuntime();
        double memoryUsage = (runtime.totalMemory() - runtime.freeMemory()) * 1e-6;
        double memoryTotal = (runtime.totalMemory()) * 1e-6;
        this.fontRenderer.drawString(g2d, (Math.round(this.instance.gameLoop.getFramesPerSecond() * 10.0) / 10.0) + " FPS, " + (Math.round(memoryUsage * 10.0) / 10.0) + "M / " +  (Math.round(memoryTotal * 10.0) / 10.0) + "M", width - scaleToDisplay(10.0), scaleToDisplay(10.0), FontRenderer.RIGHT, FontRenderer.TOP,false);

        if (Constants.SHOW_COLLISION || Constants.SHOW_CHUNK_BORDERS) {
            String[] extraInfo = {
                    "chunks=" + this.instance.world.chunksLoaded.size() + "/" + this.instance.world.chunks.size() + ", tiles=" + tilesLoaded + ", entities=" + this.instance.world.entitiesLoaded.size() + " " + countDrawnOnScreen + " drawn",
                    "dt=" + dt + "ms",
                    "chu_t=" + chunkRefreshDelay + "ms / " + chunkTime + "ms",
                    "col_t=" + collisionTime + "ms",
                    "weather=" + world.weatherProgress
            };
            Vec2D genericBounds = this.fontRenderer.bounds(g2d, "X");
            Vec2D infoPos = new Vec2D(50, 400 + genericBounds.y);
            for (String info : extraInfo) {
                BoundingBox bounds = this.fontRenderer.drawString(g2d, info, super.scaleToDisplay((infoPos.x) / this.scaleFactor), super.scaleToDisplay((infoPos.y) / this.scaleFactor), FontRenderer.LEFT, FontRenderer.TOP,false);
                infoPos.add(new Vec2D(0, bounds.size.y * 2 + scaleToDisplay(10.0)));
            }
        }

        // Update camera

        // Scale target position to display
        Vec2D cameraTargetScaledPos = super.createScaledToDisplay(this.calculateCameraTargetPos());
        // Calculate speed
        float cameraSpeed = super.time(0.004f, dt);
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

    private void drawTextPrompt(Graphics2D g2d, int width, int height, World world, double dt) {
        TextPrompt prompt = world.getPrompt();
        if (prompt != null) {
            // Reset timer & counter if there is a new prompt
            if (prompt != currentPrompt) {
                this.promptAutoNextTimer = this.promptLettersRevealed = 0.0;
            }

            // Draw background
            double hSpace = 0.2;
            double vInset = 0.075;
            double vSize = 0.25;
            BoundingBox promptBB = new BoundingBox(new Vec2D(hSpace * width, height - (height * vInset) - (height * vSize)), new Vec2D(width - (hSpace * width * 2), height * vSize));

            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.fillRect((int) promptBB.position.x, (int) promptBB.position.y, (int) promptBB.size.x, (int) promptBB.size.y);

            double bInset = super.scaleToDisplay(12.0);
            BoundingBox promptBorderBB = promptBB.clone();
            promptBorderBB.position.x += bInset;
            promptBorderBB.position.y += bInset;
            promptBorderBB.size.x -= 2 * bInset;
            promptBorderBB.size.y -= 2 * bInset;
            g2d.setColor(new Color(255, 255, 255, 255));
            g2d.setStroke(new BasicStroke(super.scaleToDisplay(7.5)));
            g2d.drawRect((int) promptBorderBB.position.x, (int) promptBorderBB.position.y, (int) promptBorderBB.size.x, (int) promptBorderBB.size.y);

            Vec2D promptCenter = promptBB.center();

            // Draw text
            double textSpace = 0.8;

            // Format lines
            double textSize = 55.0;

            // Set text font
            g2d.setFont(this.instance.lexicon.equipmentPro.deriveFont((float) scaleToDisplay(textSize)));
            ArrayList<String> lines = new ArrayList<>();
            String text = prompt.text();
            String[] words = text.contains(" ") ? text.split(" ") : new String[] { text };
            String currentLine = "";
            double textHeight = 0.0;
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                currentLine += currentLine.isEmpty() ? word : " " + word;
                Vec2D bounds = fontRenderer.bounds(g2d, currentLine);
                double lineWidth = bounds.x;
                if (lineWidth * 0.5 > promptBorderBB.size.x * textSpace * 0.5 || i == words.length - 1) {
                    textHeight += bounds.y;
                    lines.add(currentLine);
                    currentLine = "";
                }
            }

            double spaceBetweenLines = scaleToDisplay(4.0);

            // Draw author
            g2d.setFont(this.instance.lexicon.futilePro.deriveFont((float) scaleToDisplay(textSize * 1.0)));
            Vec2D authorBounds = fontRenderer.bounds(g2d, prompt.author());
            textHeight += authorBounds.y;
            fontRenderer.drawString(g2d, prompt.author(), (int) promptCenter.x, (int) (promptCenter.y - textHeight * 0.5 - spaceBetweenLines - super.scaleToDisplay(12.0)), FontRenderer.CENTER, FontRenderer.CENTER, false);

            // Draw lines
            g2d.setFont(this.instance.lexicon.equipmentPro.deriveFont((float) scaleToDisplay(textSize)));
            double lineOffsetY = authorBounds.y + 0.0 - (textHeight * 0.5) - (lines.size() * spaceBetweenLines * 0.5) + spaceBetweenLines * 0.5;
            int charCount = 0;
            for (String line : lines) {
                String visibleText = "";
                for (String c : line.split("")) {
                    if (charCount <= promptLettersRevealed) {
                        visibleText += c;
                        charCount++;
                    }
                }
                Vec2D bounds = fontRenderer.bounds(g2d, line.isEmpty() ? "X" : line);
                fontRenderer.drawString(g2d, visibleText, (int) (promptCenter.x - bounds.x * 0.5), (int) (promptCenter.y + lineOffsetY), FontRenderer.LEFT, FontRenderer.CENTER, false);
                lineOffsetY += bounds.y + spaceBetweenLines;
            }

            // Draw next button & handle auto next once text has revealed
            // (-5 chars for a later reveal of next button)
            if (this.promptLettersRevealed - 5 >= charCount) {
                // Add time to auto next timer
                int timeLeft = (int) Math.ceil((Constants.PROMPT_TEXT_AUTO_NEXT_FACTOR * prompt.waitTime()) - (this.promptAutoNextTimer / 1000.0));
                if (prompt.waitTime() > 0)
                    this.promptAutoNextTimer += dt;

                if (timeLeft <= 0 && this.promptAutoNextTimer > 0) {
                    world.prompt(null);
                    if (prompt.runnable() != null)
                        prompt.runnable().run();
                }

                if (prompt.waitTime() != TextPrompt.UNSKIPPABLE) {
                    // Draw next
                    String nextText = "Next" + (prompt.waitTime() > 0 ? " (" + Math.max(0, timeLeft) + ")" : "");
                    double nextButtonInset = super.scaleToDisplay(12.0);
                    g2d.setFont(this.instance.lexicon.equipmentPro.deriveFont((float) scaleToDisplay(46.0)));
                    BoundingBox nextBounds = fontRenderer.
                            drawString(g2d, nextText,
                                    (int) (promptBorderBB.position.x + promptBorderBB.size.x - nextButtonInset),
                                    (int) (promptBorderBB.position.y + promptBorderBB.size.y - nextButtonInset),
                                    FontRenderer.RIGHT, FontRenderer.BOTTOM, false);

                    // Draw key hint
                    double bindOffsetX = super.scaleToDisplay(8.0);
                    double bindSize = nextBounds.size.y * 1.1;

                    BindLocalizer.drawHint(g2d, this, instance.inputConfig.getBind("skipDialog"), (int) (nextBounds.position.x - bindOffsetX), (int) (nextBounds.position.y + 2.0), (int) bindSize, Align.RIGHT);
                }
            }

            // Reveal letters
            this.promptLettersRevealed += super.time(Constants.PROMPT_TEXT_REVEAL_SPEED, dt);

            // g2d.drawImage(this.instance.atlas.uiBorderText.getImage(), (int) promptBB.position.x, (int) promptBB.position.y, (int) promptBB.size.x, (int) promptBB.size.y, null);

            this.currentPrompt = prompt;
        } else {
            this.currentPrompt = null;
            this.promptLettersRevealed = this.promptAutoNextTimer = 0;
        }
    }

    private void drawWaypointArrow(Graphics2D g2d, World world) {

        // Draw waypoint arrow
        if (world.waypoint != null) {
            BoundingBox playerBB = player.getBoundingBox();
            Vec2D center = playerBB.center();
            double rotationToWaypoint = center.rotationTo(world.waypoint);
            double arrowDistance = world.waypoint.distanceSqrt(center);
            double playerDistance = Tile.TILE_SIZE * 1.5;
            double minDistance = Tile.TILE_SIZE * 2.0;
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

            GraphicsUtils.setAlpha(g2d, 0.95f);

            GraphicsUtils.rotate(g2d, rotationToWaypoint, super.scaleToDisplay(arrowPos.x), super.scaleToDisplay(arrowPos.y));
            g2d.drawImage(this.instance.atlas.uiArrow.getImage(), super.scaleToDisplay(arrowPos.x - arrowSize / 2),
                    super.scaleToDisplay(arrowPos.y - arrowSize / 2), super.scaleToDisplay(arrowSize), super.scaleToDisplay(arrowSize), null);
            GraphicsUtils.rotate(g2d, -rotationToWaypoint, super.scaleToDisplay(arrowPos.x), super.scaleToDisplay(arrowPos.y));

            GraphicsUtils.setAlpha(g2d, 1.0f);
        }
    }

    private void handlePlayerDeath(Graphics2D g2d, int width, int height,  double dt) {
        if (player.getHealth() <= 0.0 || this.timeSincePlayerDeath > 0) {
            // Lock binds
            this.instance.inputConfig.getBinds().values().forEach(KeyBind::lock);

            if (this.timeSincePlayerDeath == 0.0)
                checkpointLoaded = false;

            this.timeSincePlayerDeath += dt;

            double restartTime = 1000.0;

            if (this.timeSincePlayerDeath < restartTime / 2) {
                double d = timeSincePlayerDeath / (restartTime / 2);
                g2d.setColor(new Color(0f, 0f, 0f, (float) d));
            } else if (this.timeSincePlayerDeath > restartTime / 2) {
                double d = Math.min(1, (timeSincePlayerDeath - (restartTime / 2)) / (restartTime / 2));
                g2d.setColor(new Color(0f, 0f, 0f, 1.0f - (float) d));
            }

            g2d.fillRect(0, 0, width, height);

            if (this.timeSincePlayerDeath > restartTime / 2 && !checkpointLoaded) {
                this.loadCheckpoint();
            }
            if (this.timeSincePlayerDeath > restartTime) {
                this.timeSincePlayerDeath = 0.0;

                // Unlock binds
                this.instance.inputConfig.getBinds().values().forEach(KeyBind::unlock);
            }
        }
    }

    private void drawMiniMap(Graphics2D g2d) {
        World world = this.instance.world;
        int width = this.instance.display.getWidth();

        // Draw map
        if (world.compiledObjectImage != null) {
            BufferedImage mapImage = this.instance.world.compiledObjectImage;
            BoundingBox mapBounds = new BoundingBox(world.topLeft, world.bottomRight);
            Vec2D vecMapSize = mapBounds.abs();
            double mapCompileScale = ((mapImage.getWidth() / vecMapSize.x) + (mapImage.getHeight() / vecMapSize.y)) / 2.0;
            double mapDisplaySize = 200.0;
            double mapZoom = 4.0;
            double mapInset = 100.0;
            double mapDisplayScaleX = mapDisplaySize / mapImage.getWidth();
            double mapDisplayScaleY = mapDisplaySize / mapImage.getWidth();
            double centerX = player.getBoundingBox().center().x - mapBounds.position.x;
            double centerY = player.getBoundingBox().center().y - mapBounds.position.y;

            double mapX = centerX * mapCompileScale * mapDisplayScaleX * mapZoom;
            double mapY = centerY * mapCompileScale * mapDisplayScaleY * mapZoom;
            double mapWidth = mapImage.getWidth() * mapDisplayScaleX;
            double mapHeight = mapImage.getHeight() * mapDisplayScaleY;
            int mapCenterX = width - scaleToDisplay(mapInset + mapWidth / 2);
            int mapCenterY = scaleToDisplay(mapInset + mapHeight / 2);

            Shape ellipseClip = new Ellipse2D.Double(mapCenterX - scaleToDisplay(mapDisplaySize / 2), mapCenterY - scaleToDisplay(mapDisplaySize / 2), scaleToDisplay(mapDisplaySize), scaleToDisplay(mapDisplaySize));

            g2d.setClip(ellipseClip);
            g2d.setColor(Constants.MAP_BACKGROUND_COLOR);
            g2d.fill(ellipseClip);
            g2d.drawImage(mapImage, mapCenterX - scaleToDisplay(mapWidth / 2) + scaleToDisplay(-mapX + mapWidth / 2), mapCenterY - scaleToDisplay(mapHeight / 2) + scaleToDisplay(-mapY + mapHeight / 2), scaleToDisplay(mapWidth * mapZoom), scaleToDisplay(mapHeight * mapZoom), null);
            g2d.setColor(Color.GREEN);
            g2d.fillRect(mapCenterX - scaleToDisplay(5), mapCenterY - scaleToDisplay(5), scaleToDisplay(10), scaleToDisplay(10));
            g2d.setClip(null);

            g2d.setStroke(new BasicStroke(scaleToDisplay(8.0)));
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.draw(ellipseClip);

            g2d.setStroke(new BasicStroke(scaleToDisplay(1.0)));
            g2d.setColor(Color.WHITE);
            g2d.draw(ellipseClip);


            GraphicsUtils.setAlpha(g2d, 1f);
            g2d.setClip(null);
        }
    }

    private void drawHUD(Graphics2D g2d, int width, int height, World world, double dt) {
        // drawMiniMap(g2d);

        // Draw text prompt
        drawTextPrompt(g2d, width, height, world, dt);

        // Set font
        g2d.setFont(this.instance.lexicon.equipmentPro.deriveFont((float) scaleToDisplay(40.0)));

        double hudGap = 20.0;

        // Draw Player Health
        this.healthDisplayed += (this.player.getHealth() - this.healthDisplayed) * Constants.HUD_VALUE_ANIMATION_SPEED; // Animates health add/remove
        int hearts = (int) (player.getMaxHealth() / 2);
        double heartSize = 100.0;
        double heartGap = 7.5;
        double heartsX = 40.0;
        double heartsY = 40.0;
        double heartFullCutoff = 0.084;

        for (int i = 0; i < hearts; i++) {
            double ratio = Math.min(1, (this.healthDisplayed - (i * 2.0)) / 2.0);

            Rectangle heart = new Rectangle(super.scaleToDisplay(heartsX + (heartGap + heartSize) * i), super.scaleToDisplay(heartsY), super.scaleToDisplay(heartSize), super.scaleToDisplay(heartSize));

            g2d.setClip(null);
            g2d.drawImage(this.instance.atlas.uiHeartEmpty.getBufferedImage(), heart.x, heart.y, heart.width, heart.height, null);

            if (ratio <= 0.0)
                continue;

            Rectangle clip = new Rectangle(heart.x, heart.y, heart.width, heart.height);

            clip.x += heartSize * heartFullCutoff;
            clip.width -= heartSize * heartFullCutoff * 2;
            clip.width *= ratio;
            g2d.setClip(clip);
            g2d.drawImage(this.instance.atlas.uiHeartFull.getBufferedImage(), heart.x, heart.y, heart.width, heart.height, null);

        }
        g2d.setClip(null);

        // Draw Player Dashes
        SkillDash skillDash = this.instance.world.saveGame.getSkills().skillDash;
        this.dashesDisplayed += (this.player.getDashesLeft() - this.dashesDisplayed) * Constants.HUD_VALUE_ANIMATION_SPEED; // Animates dash add/remove
        int dashes = (int) skillDash.getMaximumDashCount();
        double dashSize = 50.0;
        double dashGap = 7.5;
        double dashX = heartsX;
        double dashY = 40.0 + heartSize + hudGap;
        double dashFullCutoff = 0.19;

        double dashArc = 15.0;
        double dashWidth = dashes * 100.0;
        double dashInset = 10.0;
        double dashBarWidth = 7.5;
        double dashAreaWidth = (dashWidth - dashInset * 2) / dashes;

        g2d.setColor(new Color(48, 44, 46));
        g2d.fillRoundRect(super.scaleToDisplay(dashX), super.scaleToDisplay(dashY), super.scaleToDisplay(dashWidth), super.scaleToDisplay(dashSize), super.scaleToDisplay(dashArc), super.scaleToDisplay(dashArc));

        Color dashColorBarBackground = new Color(79, 84, 107);

        g2d.setColor(dashColorBarBackground);
        g2d.fillRoundRect(super.scaleToDisplay(dashX + dashInset), super.scaleToDisplay(dashY + dashInset), super.scaleToDisplay(dashWidth - (dashInset * 2)), super.scaleToDisplay(dashSize - (dashInset * 2)), super.scaleToDisplay(dashArc), super.scaleToDisplay(dashArc));

        g2d.setColor(new Color(62, 179, 51));
        g2d.fillRoundRect(super.scaleToDisplay(dashX + dashInset), super.scaleToDisplay(dashY + dashInset), super.scaleToDisplay((dashWidth - (dashInset * 2)) * (this.dashesDisplayed / skillDash.getMaximumDashCount())), super.scaleToDisplay(dashSize - (dashInset * 2)), super.scaleToDisplay(dashArc * 0.2), super.scaleToDisplay(dashArc * 0.2));

        for (int i = 1; i < dashes; i++) {
            g2d.setColor(this.dashesDisplayed >= i ? new Color(73, 218, 62) : dashColorBarBackground.brighter());
            g2d.fillRect(super.scaleToDisplay(dashX + dashInset + (dashAreaWidth * i) - (dashBarWidth * 0.5)), super.scaleToDisplay(dashY + dashInset), super.scaleToDisplay(dashBarWidth), super.scaleToDisplay(dashSize - (dashInset * 2)));
        }

        double coinX = dashX;
        double coinY = dashY + dashSize + hudGap;
        double coinSize = 50.0;
        double coinGap = heartGap;
        double coinTextSize = 62.0;

        Image coinImage = this.instance.atlas.uiCoin instanceof TextureAnimated animated ? animated.getImage(0) : this.instance.atlas.uiCoin.getBufferedImage();
        g2d.drawImage(coinImage, super.scaleToDisplay(coinX), super.scaleToDisplay(coinY), super.scaleToDisplay(coinSize), super.scaleToDisplay(coinSize), null);

        g2d.setColor(new Color(234, 234, 234));
        g2d.setFont(this.instance.lexicon.equipmentPro.deriveFont((float) super.scaleToDisplay(coinTextSize)));
        fontRenderer.drawStringAccurately(g2d,  String.valueOf(0), super.scaleToDisplay(coinX + coinSize + coinGap), super.scaleToDisplay(coinY + coinSize * 0.5 + coinSize * 0.075), Align.LEFT, Align.CENTER, false);

    }

    private void loadCheckpoint() {
        resetPlayerStats();
        checkpointLoaded = true;
        instance.world.story.loadCheckpoint();
    }

    private void resetPlayerStats() {
        this.player.setDashesLeft(instance.world.saveGame.getSkills().skillDash.getMaximumDashCount());
        this.player.setMaxHealth(instance.world.saveGame.getSkills().skillHealth.getMaxHealth());
        this.player.setHealth(this.player.getMaxHealth());
    }

    private Vec2D calculateCameraTargetPos() {
        // Create camera target pos with center position of players bounding box
        Vec2D cameraTargetPos = this.player.getBoundingBox().center();
        // Add/Subtract mouse position, to follow crosshair a bit
        return cameraTargetPos.subtract(cameraTargetPos.clone().subtract(this.mouseWorldPosition).multiply(Constants.CAMERA_MOUSE_FOLLOW_FACTOR));
    }

    public void resetCameraPosition() {
        Vec2D target = calculateCameraTargetPos().subtract(new Vec2D(instance.display.getWidth(), instance.display.getHeight()).multiply(0.5));
        this.cameraPos.x = target.x;
        this.cameraPos.y = target.y;
    }

    public boolean hasPromptRevealFinished() {
        if (instance.world.getPrompt() == null)
            return true;
        int promptTextLength = instance.world.getPrompt().text().length();
        return currentPrompt == instance.world.getPrompt() && promptLettersRevealed > promptTextLength;
    }

}
