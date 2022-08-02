package de.yonedash.smash.progression.saves;

import de.yonedash.smash.config.XMLConfig;
import de.yonedash.smash.progression.skills.Skills;

import java.io.File;
import java.util.Properties;

public class SaveGame extends XMLConfig {

    private final Skills skills;

    public SaveGame(File file) {
        super(file);
        this.skills = new Skills(this);
    }

    @Override
    public void init() {
        this.skills.init();
        add("checkpoint/step", 0);
        add("difficulty", 0);
        add("coins", 0);
        add("playTime", 0);
    }

    public Skills getSkills() {
        return this.skills;
    }

    public int getCheckpointStep() {
        return getInt("checkpoint/step");
    }

    public double getDifficulty() {
        return getDouble("difficulty");
    }

    public void addCoins(int amount) {
        set("coins", getCoins() + amount);
    }

    public int getCoins() {
        return getInt("coins");
    }

    protected Properties properties() {
        return this.properties;
    }

    public StateCapture capture() {
        return new StateCapture(this);
    }

    // todo remove
    public Properties DEBUG_REMOVE_MEEEEEEEE() {
        return properties;
    }

}
