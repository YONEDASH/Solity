package de.yonedash.smash;

import de.yonedash.smash.entity.LevelObject;

import java.util.ArrayList;

// This class is responsible for holding the level data (tiles..)
public class LevelData {

    private final ArrayList<LevelObject> levelObjects;

    public LevelData(ArrayList<LevelObject> levelObjects) {
        this.levelObjects = levelObjects;
    }

    public ArrayList<LevelObject> getLevelObjects() {
        return levelObjects;
    }
}
