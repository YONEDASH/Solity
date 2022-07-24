package de.yonedash.smash.config;

import de.yonedash.smash.progression.skills.Skills;

import java.io.File;

public class SaveGame extends XMLConfig {

    private final Skills skills;

    public SaveGame(File file) {
        super(file);
        this.skills = new Skills(this);
    }

    @Override
    public void init() {
        this.skills.init();
    }

    public Skills getSkills() {
        return this.skills;
    }

}