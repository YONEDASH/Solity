package de.yonedash.smash.progression.skills;

import de.yonedash.smash.config.SaveGame;

public abstract class Skill {

    protected final SaveGame saveGame;
    protected final String name;

    public Skill(SaveGame saveGame, String name) {
        this.saveGame = saveGame;
        this.name = name;
    }

    public void init() {
        this.saveGame.add("Skill/" + this.name + "/Level",  1);
    }

    public int getLevel() {
        return this.saveGame.getInt("Skill/" + this.name + "/Level");
    }

}