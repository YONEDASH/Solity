package de.yonedash.smash.progression.skills;

import de.yonedash.smash.progression.saves.SaveGame;

import java.util.ArrayList;
import java.util.List;

public final class Skills {

    private final ArrayList<Skill> skillArrayList;
    public final SkillHealth skillHealth;
    public final SkillDash skillDash;

    public Skills(SaveGame saveGame) {
        this.skillArrayList = new ArrayList<>();

        this.skillHealth = (SkillHealth) this.store(new SkillHealth(saveGame));
        this.skillDash = (SkillDash) this.store(new SkillDash(saveGame));
    }

    private Skill store(Skill skill) {
        this.skillArrayList.add(skill);
        return skill;
    }

    public void init() {
        this.skillArrayList.forEach(Skill::init);
    }

    public List<Skill> getList() {
        return skillArrayList;
    }
}
