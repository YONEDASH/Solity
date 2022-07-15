package de.yonedash.smash;

import de.yonedash.smash.entity.Entity;
import de.yonedash.smash.entity.LevelObject;
import de.yonedash.smash.progression.Story;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

// This class represents the game world
public class World implements ProgressReport {

    public Vec2D waypoint;
    public Story story;
    public OpenSimplexNoise simplexNoise;
    public double fogOffset, weatherOffset, weatherProgress;
    public double particleOffset;
    public final ArrayList<Chunk> chunks;
    public final CopyOnWriteArraySet<Chunk> chunksLoaded;
    public final CopyOnWriteArraySet<Entity> entitiesLoaded;
    public Vec2D topLeft, bottomRight;


    private double randomOffset;

    private TextPrompt prompt;

    public World() {
        this.chunks = new ArrayList<>();

        this.chunksLoaded = new CopyOnWriteArraySet<>();
        this.entitiesLoaded = new CopyOnWriteArraySet<>();
    }

    // Returns random value between -1 and 1
    public double random() {
        return this.simplexNoise.eval(randomOffset, ++randomOffset);
    }

    // Show prompt on screen
    public void prompt(TextPrompt prompt) {
        this.prompt = prompt;
    }

    public TextPrompt getPrompt() {
        return prompt;
    }

    private int progressTotal, progress;

    public void loadLevel(LevelData level) {
        // Init simplex noise
        this.simplexNoise = new OpenSimplexNoise(level.seed());

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
                // Add to chunk list
                this.chunks.add(chunk);
            }
        }
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
