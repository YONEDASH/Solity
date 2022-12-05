package de.yonedash.solity;

import de.yonedash.solity.entity.LevelObject;

import java.util.ArrayList;

// This class is responsible for holding the level data (tiles..)
public record LevelData(long seed, ArrayList<LevelObject> levelObjects) {

}



