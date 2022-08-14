package de.yonedash.smash;

import de.yonedash.smash.graphics.EntityFog;
import de.yonedash.smash.progression.saves.SaveGame;
import de.yonedash.smash.entity.DisplayEntity;
import de.yonedash.smash.entity.Entity;
import de.yonedash.smash.entity.LevelObject;
import de.yonedash.smash.progression.saves.SaveGameTemporary;
import de.yonedash.smash.progression.story.Story;
import de.yonedash.smash.scene.Scene;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

// This class represents the game world
public class World implements ProgressReport {

    public final SaveGame saveGame;
    public Vec2D waypoint;
    public Story story;
    public OpenSimplexNoise simplexNoise;
    public double fogOffset, weatherOffset, weatherProgress;
    public double particleOffset;
    public final ArrayList<Chunk> chunks;
    public final CopyOnWriteArraySet<Chunk> chunksLoaded;
    public final CopyOnWriteArraySet<Entity> entitiesLoaded;
    public Vec2D topLeft, bottomRight;

    public BufferedImage compiledObjectImage;

    private double randomOffset;

    private TextPrompt prompt;

    public World(SaveGame saveGame) {
        this.saveGame = saveGame;

        this.chunks = new ArrayList<>();

        this.chunksLoaded = new CopyOnWriteArraySet<>();
        this.entitiesLoaded = new CopyOnWriteArraySet<>();
    }

    // Returns random value between -1 and 1
    public double random(Scene scene, double dt) {
        return this.simplexNoise.eval(randomOffset, randomOffset += scene.time(1, dt));
    }

    // Show prompt on screen
    public void prompt(TextPrompt prompt) {
        this.prompt = prompt;
    }

    public TextPrompt getPrompt() {
        return prompt;
    }

    private int progressTotal, progress;

    public void load(Instance instance, LevelData level) {
        System.out.println("OBJECTS: " + level.levelObjects().size());

        // Init simplex noise
        this.simplexNoise = new OpenSimplexNoise(level.seed());

        // Init save game
        this.saveGame.load();

        // Set last access
        this.saveGame.set("lastAccess", System.currentTimeMillis());

        // Set last played
        if (!(saveGame instanceof SaveGameTemporary)) {
            instance.launchData.set("saveGameLastPlayed", this.saveGame.getName());
            try {
                instance.launchData.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Reset waypoint
        this.waypoint = null;

        // In order to prevent bugs, clear all lists/sets
        this.chunks.clear();
        this.chunksLoaded.clear();
        this.entitiesLoaded.clear();

        // Load tiles into chunks

        // Create new arraylist with levels tiles
        CopyOnWriteArrayList<LevelObject> levelObjects = new CopyOnWriteArrayList<>(level.levelObjects());

        // Set progress total for reporting
        this.progressTotal = levelObjects.size();

        // Cancel if list is empty
        if (levelObjects.size() == 0)
            return;

        // Sort tiles by their position sum
        levelObjects.sort(Comparator.comparingDouble(o -> o.getBoundingBox().position.x + o.getBoundingBox().position.y));

        // Get tile with lowest x and y value
        Vec2D posSmallestSum = this.topLeft = levelObjects.get(0).getBoundingBox().position.clone();
        // Get tile with highest x and y value
        Vec2D posHighestSum = this.bottomRight = levelObjects.get(levelObjects.size() - 1).getBoundingBox().position.clone();

        // Double check positions to prevent any errors
        for (LevelObject levelObject : levelObjects) {
            Vec2D pos = levelObject.getBoundingBox().position;
            if (pos.x < posSmallestSum.x)
                posSmallestSum.x = pos.x;
            if (pos.y < posSmallestSum.y)
                posSmallestSum.y = pos.y;
            if (pos.x > posHighestSum.x)
                posHighestSum.x = pos.x;
            if (pos.y > posHighestSum.y)
                posHighestSum.y = pos.y;
        }

        for (int x = ((int) (posSmallestSum.x / Chunk.CHUNK_SIZE)); x < ((int) (posHighestSum.x / Chunk.CHUNK_SIZE)); x++) {
            for (int y = ((int) (posSmallestSum.y / Chunk.CHUNK_SIZE)); y < ((int) (posHighestSum.y / Chunk.CHUNK_SIZE)); y++) {
                ArrayList<LevelObject> tilesInChunk = new ArrayList<>();

                for (LevelObject levelObject : levelObjects) {
                    Vec2D pos = levelObject.getBoundingBox().position;

                    // Check if tile is within chunk
                    if (pos.x >= x * Chunk.CHUNK_SIZE && pos.x < x * Chunk.CHUNK_SIZE + Chunk.CHUNK_SIZE
                        && pos.y >= y * Chunk.CHUNK_SIZE && pos.y < y * Chunk.CHUNK_SIZE + Chunk.CHUNK_SIZE) {
                        // If so, add to chunk
                        tilesInChunk.add(levelObject);
                        // and remove from available tiles
                        levelObjects.remove(levelObject);

                        // Report progress
                        this.progress++;
                    }
                }

                // If chunk has no tiles, don't bother to add to increase performance
                if (tilesInChunk.size() == 0)
                    continue;

                // Create a new chunk with tiles
                Chunk chunk = new Chunk(x, y, tilesInChunk.toArray(new LevelObject[0]));

                // Create fog image
                chunk.getEntities().stream().filter(entity -> entity instanceof EntityFog).forEach(entity -> ((EntityFog) entity).createFog(instance.graphicsConfig));

                // Add to chunk list
                this.chunks.add(chunk);
            }
        }

        // Compile world objects to image (for map)
        this.compileWorldObjectsToImage(instance);
    }

    public BufferedImage compileWorldObjectsToImage(Instance instance) {
        BoundingBox bounds = new BoundingBox(topLeft, bottomRight);
        Vec2D size = bounds.abs();
        double scale = Constants.MAP_RESOLUTION_SCALE;
        BufferedImage bufferedImage = new BufferedImage((int) (size.x * scale), (int) (size.y * scale), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        Scene dummyScene = new Scene(instance) {
            @Override
            public void update(Graphics2D g2d, double dt) {

            }

            @Override
            public double calculateDisplayScaleFactor() {
                return scale;
            }
        };

        g2d.translate(-bounds.position.x * scale, -bounds.position.y * scale);

        // Draw objects
        ArrayList<LevelObject> zSortedLevelObjects = new ArrayList<>();
        this.chunks.forEach(chunk -> zSortedLevelObjects.addAll(Arrays.stream(chunk.getLevelObjects()).toList()));

        // Sort list by z value
        zSortedLevelObjects.sort(Comparator.comparingInt(DisplayEntity::getZ));
        zSortedLevelObjects.forEach(levelObject -> levelObject.draw(dummyScene, g2d, 0));

        g2d.dispose();
        return this.compiledObjectImage = bufferedImage;
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public int getProgressTotal() {
        return this.progressTotal;
    }
}
