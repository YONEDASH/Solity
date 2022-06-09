package de.yonedash.smash.item;

import de.yonedash.smash.Instance;
import de.yonedash.smash.resource.TextureAtlas;

public class ItemRegistry {

    private final Instance instance;

    public ItemWeapon fork;

    public ItemRegistry(Instance instance) {
        this.instance = instance;
    }

    public void load() {
        TextureAtlas atlas = this.instance.atlas;

        this.fork = new ItemWeapon("fork", atlas.fork, 450.0, 10.0, true);
    }

}
