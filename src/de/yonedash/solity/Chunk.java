package de.yonedash.solity;

import de.yonedash.solity.entity.Entity;
import de.yonedash.solity.graphics.EntityFog;
import de.yonedash.solity.entity.LevelObject;
import de.yonedash.solity.graphics.EntityLighting;

import java.util.ArrayList;

// This class represents a chunk of the game world
// The game world is split up into chunks for the
// game to have better performance, since every chunk
// is only being loaded once it is supposed to be seen
// on the screen
public class Chunk {

    // This constant integer determines the chunk size in tile size
    public static final int CHUNK_SIZE = 8 * LevelObject.TILE_SIZE;

    private final BoundingBox boundingBox;
    private final LevelObject[] levelObjects;
    private final ArrayList<Entity> entities;

    private final EntityFog entityFog;
    private final EntityLighting entityLighting;

    public Chunk(int x, int y, LevelObject[] levelObjects) {
        this.boundingBox = new BoundingBox(new Vec2D(x * CHUNK_SIZE, y * CHUNK_SIZE), new Vec2D(CHUNK_SIZE, CHUNK_SIZE));
        this.levelObjects = levelObjects;
        this.entities = new ArrayList<>();
        this.entities.add(this.entityFog = new EntityFog(this));
        this.entities.add(this.entityLighting = new EntityLighting(this));
    }

//
//    public Texture[] compile() {
//        var texs[];
//        for (int layer = 0; layer < layers; layer++) {
//            Texture tex;
//            tex.draw(tiles of layer);
//            texs[layer] = tex;
//        }
//        return texs;
//    }

    public int getX() {
        return (int) this.boundingBox.position.x / CHUNK_SIZE;
    }

    public int getY() {
        return (int) this.boundingBox.position.y / CHUNK_SIZE;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public LevelObject[] getLevelObjects() {
        return levelObjects;
    }

    public ArrayList<Entity> getEntities() {
        return this.entities;
    }

    public EntityFog getEntityFog() {
        return entityFog;
    }

    public EntityLighting getEntityLighting() {
        return entityLighting;
    }
}
