package de.yonedash.solity.progression.saves;

import java.util.HashMap;
import java.util.Properties;

public class StateCapture {

    private final HashMap<String, String> data;

    public StateCapture(SaveGame saveGame) {
        this.data = new HashMap<>();
        Properties properties = saveGame.properties();
        properties.stringPropertyNames().forEach(key -> data.put(key, properties.getProperty(key)));
    }

    public void apply(SaveGame saveGame) {
        long playtime = saveGame.getLong("playTime");
        data.keySet().forEach(key -> saveGame.set(key, data.get(key)));
        saveGame.set("playTime", playtime);
    }

}
