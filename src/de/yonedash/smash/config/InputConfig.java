package de.yonedash.smash.config;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class InputConfig extends XMLConfig {

    private final HashMap<String, KeyBind> binds = new HashMap<>();

    public InputConfig() {
        super(new File("GAMEFOLDER", "input.xml"));
    }

    @Override
    protected void loadFile() throws IOException {
        super.loadFile();

        // Load entries into binds map
        this.properties.stringPropertyNames()
                .forEach(key -> this.binds.put(key, new KeyBind(getString(key))));
    }

    @Override
    public void init() {
        addBind("skipDialog", new KeyBind(KeyBind.Device.KEYBOARD, KeyEvent.VK_SPACE));
        addBind("moveUp", new KeyBind(KeyBind.Device.KEYBOARD, KeyEvent.VK_W));
        addBind("moveLeft", new KeyBind(KeyBind.Device.KEYBOARD, KeyEvent.VK_A));
        addBind("moveDown", new KeyBind(KeyBind.Device.KEYBOARD, KeyEvent.VK_S));
        addBind("moveRight", new KeyBind(KeyBind.Device.KEYBOARD, KeyEvent.VK_D));
        addBind("shoot", new KeyBind(KeyBind.Device.MOUSE, 1));
    }

    @Override
    public void set(String key, Object value) {
        if (value instanceof KeyBind keyBind) {
            super.set(key, keyBind);

            // Update bind map
            this.binds.remove(key);
            this.binds.put(key, keyBind);
        } else {
            throw new IllegalArgumentException("InputConfig value has to be a KeyBind");
        }
    }

    public KeyBind getBind(String key) {
        return this.binds.get(key);
    }

    private void addBind(String key, KeyBind bind) {
        if (!this.binds.containsKey(key)) {
            this.binds.put(key, bind);
            super.add(key, (Object) bind);
        }
    }

}
