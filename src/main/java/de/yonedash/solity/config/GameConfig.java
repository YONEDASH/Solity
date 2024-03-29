package de.yonedash.solity.config;

import de.yonedash.solity.Instance;
import de.yonedash.solity.localization.Language;

import java.io.File;

public class GameConfig extends INIConfig {

    public Language language;

    public boolean vSync, lowPowerMode, showFps;
    public double fpsLimit;

    public GameConfig(Instance instance) {
        super(new File(instance.gameRoot, "config.ini"));
    }

    @Override
    protected void init() {
        add("language", Language.systemDefault().name());
        add("volumeMaster", 0.5);
        add("volumeMusic", 1.0);
        add("volumeSound", 1.0);
        add("volumeTyping", 1.0);
        add("fpsLimit", 60);
        add("vsync", false);
        add("lowPowerMode", false);
        add("fullscreen", false);
        add("showFps", false);
    }

    @Override
    public void load() {
        super.load();

        this.language = Language.valueOf(getString("language"));
        this.fpsLimit = getDouble("fpsLimit");
        this.vSync = getBoolean("vsync");
        this.lowPowerMode = getBoolean("lowPowerMode");
        this.showFps = getBoolean("showFps");
    }

}
