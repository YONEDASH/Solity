package de.yonedash.solity.entity;

import de.yonedash.solity.BoundingBox;

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
