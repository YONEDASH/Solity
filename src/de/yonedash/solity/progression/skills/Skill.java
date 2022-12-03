package de.yonedash.solity.progression.skills;

import de.yonedash.solity.progression.saves.SaveGame;

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

    public void setLevel(int level) {
        this.saveGame.set("Skill/" + this.name + "/Level", level);
    }

}
