package de.uscoutz.nexus.skills;

import lombok.Getter;

public enum Skill {

    MINING("Mining", 100, 750, 5000),
    WOODCUTTING("Woodcutting", 100, 300, 5000),
    FARMING("Farming", 100, 500, 5000);

    @Getter
    private final int[] neededXP;
    @Getter
    private String title;

    Skill(String title, int... neededXP) {
        this.title = title;
        this.neededXP = neededXP;
    }
}
