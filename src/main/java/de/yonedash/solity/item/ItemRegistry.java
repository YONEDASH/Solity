package de.yonedash.solity.item;

import de.yonedash.solity.Instance;
import de.yonedash.solity.graphics.TextureAtlas;

public class ItemRegistry {

    private final Instance instance;

    public ItemWeapon fork;

    public ItemRegistry(Instance instance) {
        this.instance = instance;
    }

    public void load() {
        TextureAtlas atlas = this.instance.atlas;

        this.fork = new ItemWeapon("fork", atlas.shuriken, 450.0, 10.0, true);
    }

}
