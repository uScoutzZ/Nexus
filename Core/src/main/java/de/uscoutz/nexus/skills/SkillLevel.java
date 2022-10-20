package de.uscoutz.nexus.skills;

import de.uscoutz.nexus.skills.rewards.SkillReward;
import lombok.Getter;

public class SkillLevel {

    @Getter
    private int neededXP;
    @Getter
    private SkillReward[] rewards;

    public SkillLevel(int neededXP, SkillReward... skillRewards) {
        this.neededXP = neededXP;
        this.rewards = skillRewards;
    }
}
