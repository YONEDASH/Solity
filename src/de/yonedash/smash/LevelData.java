package de.yonedash.smash;

import de.yonedash.smash.entity.LevelObject;

import java.util.ArrayList;

// This class is responsible for holding the level data (tiles..)
public record LevelData(long seed, ArrayList<LevelObject> levelObjects) {

}



