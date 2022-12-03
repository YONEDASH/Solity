package de.yonedash.solity.item;

import de.yonedash.solity.resource.Texture;

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
