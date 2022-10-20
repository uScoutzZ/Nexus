package de.uscoutz.nexus.skills;

import de.uscoutz.nexus.skills.rewards.SkillReward;

public class SkillLevel {

    private long neededXP;
    private SkillReward[] rewards;

    public SkillLevel(int neededXP, SkillReward... skillRewards) {
        this.neededXP = neededXP;
        this.rewards = skillRewards;
    }
}
