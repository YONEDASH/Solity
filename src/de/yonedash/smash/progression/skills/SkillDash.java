package de.yonedash.smash.progression.skills;

import de.yonedash.smash.config.SaveGame;

public class SkillDash extends Skill {

    public SkillDash(SaveGame saveGame) {
        super(saveGame, "Dash");
    }

    public float getMaximumDashCount() {
        int level = getLevel();
        return 2f + (float) Math.floor(level * 0.3f);
    }

    public double getSustainFactor() {
        int level = getLevel();
        return 2f + level * 0.05f;
    }

    public double getMoveSpeedFactor() {
        int level = getLevel();
        return 2f + level * 0.15f;
    }

    public float getChargeFactor() {
        int level = getLevel();
        return 1f + level * 0.02f;
    }


}
