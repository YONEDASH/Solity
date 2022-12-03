package de.yonedash.solity.progression.saves;

import de.yonedash.solity.Difficulty;
import de.yonedash.solity.config.XMLConfig;
import de.yonedash.solity.progression.skills.Skills;

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
        add("difficulty", Difficulty.NORMAL.name());
        add("coins", 0);
        add("playTime", 0);
        add("lastAccess", 0);
    }

    public Skills getSkills() {
        return this.skills;
    }

    public int getCheckpointStep() {
        return getInt("checkpoint/step");
    }

    public double getDifficulty() {
        return Difficulty.valueOf(getString("difficulty")).value;
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

    public String getName() {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    // todo remove
    public Properties DEBUG_REMOVE_MEEEEEEEE() {
        return properties;
    }

}
