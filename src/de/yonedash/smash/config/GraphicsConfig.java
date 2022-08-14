package de.yonedash.smash.config;

import de.yonedash.smash.Instance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GraphicsConfig extends INIConfig {

    public GraphicsConfig(Instance instance) {
        super(new File(instance.gameRoot, "graphics.ini"));
    }

    @Override
    protected void init() {
        keyOrder.clear();
        Preset defaultPreset = Preset.MEDIUM;
        add("preset", defaultPreset.name());
        add("chunkRefreshTimeFactor", defaultPreset.values[0]);
        add("fogQuality", defaultPreset.values[1]);
        add("particleEmitDelayFactor", defaultPreset.values[2]);
    }

    private final ArrayList<String> keyOrder = new ArrayList<>();

    @Override
    public void add(String key, Object defaultValue) {
        super.add(key, defaultValue);
        keyOrder.add(key);
    }

    public double chunkRefreshTimeFactor, fogQuality, particleEmitDelayFactor;

    @Override
    public void load() {
        super.load();

        chunkRefreshTimeFactor = getDouble("chunkRefreshTimeFactor");
        fogQuality = getDouble("fogQuality");
        particleEmitDelayFactor = getDouble("particleEmitDelayFactor");
    }

    public enum Preset {

        LOW(20.0, 0.02, 5.0),
        MEDIUM(10.0, 0.1, 2.5),
        HIGH(5.0, 0.15, 0.9),
        ULTRA(1.0, 0.25, 0.8);

        private final Object[] values;

        Preset(Object... values) {
            this.values = values;
        }

        public void apply(GraphicsConfig graphicsConfig) {
            List<String> keys = graphicsConfig.keyOrder;

            graphicsConfig.set("preset", this.name());

            int nStart = 1;

            // Set preset values
            for (int i = nStart; i < keys.size(); i++)
                graphicsConfig.set(keys.get(i), values[i - nStart]);
        }

    }

}
