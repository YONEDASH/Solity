package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;

public class LevelCheckpoint extends LevelObject {

    public LevelCheckpoint(BoundingBox boundingBox, int z) {
        super(boundingBox, z);
    }

    public void trigger() {
        // todo
        // Respawn enemies

        // Heal health & stamina
    }

}
