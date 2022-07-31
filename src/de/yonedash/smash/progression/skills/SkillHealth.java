package de.yonedash.smash.progression.skills;

import de.yonedash.smash.progression.saves.SaveGame;

public class SkillHealth extends Skill {

    public SkillHealth(SaveGame saveGame) {
        super(saveGame, "Health");
    }

    public double getMaxHealth() {
        int level = getLevel();
        return 2 * (3.0 + level) - 2.0;
    }

}
