package de.yonedash.smash.config;

import de.yonedash.smash.localization.Language;

import java.io.File;

public class GameConfig extends XMLConfig {

    public Language language;

    public GameConfig() {
        super(new File("GAMEFOLDER", "config.xml"));
    }

    @Override
    protected void init() {
        add("language", Language.systemDefault().name());
    }

    @Override
    public void load() {
        super.load();

        this.language = Language.valueOf(getString("language"));
    }

}
