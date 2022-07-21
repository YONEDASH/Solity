package de.yonedash.smash.progression.skills;

import de.yonedash.smash.config.SaveGame;

import java.util.ArrayList;
import java.util.List;

public final class Skills {

    private final ArrayList<Skill> skillArrayList;
    public final SkillDash skillDash;

    public Skills(SaveGame saveGame) {
        this.skillArrayList = new ArrayList<>();

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
