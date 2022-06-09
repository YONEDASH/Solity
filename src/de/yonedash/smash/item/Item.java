package de.yonedash.smash.item;

import de.yonedash.smash.resource.Texture;

public class Item {

    private final String name;
    private final Texture texture;

    public Item(String name, Texture texture) {
        this.name = name;
        this.texture = texture;
    }

    public String getName() {
        return this.name;
    }

    public Texture getTexture() {
        return this.texture;
    }

}
