package de.yonedash.solity.graphics;

import de.yonedash.solity.resource.Texture;

import java.util.HashMap;

// This class saves texture data for a certain entity class
public class TextureBundle {

    private final HashMap<Integer, Texture> data;

    public TextureBundle() {
        this.data = new HashMap<>();
    }

    public void store(int id, Texture texture) {
        this.data.put(id, texture);
    }

    public Texture pull(int id) {
        return this.data.get(id);
    }

}
