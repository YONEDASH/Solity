package de.yonedash.solity.progression.skills;

import de.yonedash.solity.progression.saves.SaveGame;

public class SkillDash extends Skill {

    public SkillDash(SaveGame saveGame) {
        super(saveGame, "Dash");
    }

    public float getMaximumDashCount() {
        int level = getLevel();
        return 1f + (float) Math.floor(level * 1.0f);
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
        return 1f + level * 2.2f;
    }


}
