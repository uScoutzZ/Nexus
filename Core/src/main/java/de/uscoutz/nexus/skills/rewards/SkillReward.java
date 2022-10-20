package de.uscoutz.nexus.skills.rewards;

import org.bukkit.entity.Player;

public interface SkillReward {

    String getDisplay();
    void setDisplay(String display);
    void addReward(Player player);
}
