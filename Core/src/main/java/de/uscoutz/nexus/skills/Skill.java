package de.uscoutz.nexus.skills;

import de.uscoutz.nexus.skills.rewards.ItemReward;
import de.uscoutz.nexus.skills.rewards.MoneyReward;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Skill {

    MINING("Mining",
            new SkillLevel(100, new MoneyReward(100), new ItemReward(new ItemStack(Material.COAL, 8))),
            new SkillLevel(300, new ItemReward(new ItemStack(Material.IRON_INGOT, 3)))),
    WOODCUTTING("Woodcutting",
            new SkillLevel(100, new MoneyReward(100), new ItemReward(new ItemStack(Material.DARK_OAK_LOG, 8))),
            new SkillLevel(300, new ItemReward(new ItemStack(Material.ACACIA_LOG, 8)))),
    FARMING("Farming",
            new SkillLevel(100, new MoneyReward(100)),
            new SkillLevel(300, new MoneyReward(300)));

    @Getter
    private final SkillLevel[] skillLevels;
    @Getter
    private String title;

    Skill(String title, SkillLevel... skillLevels) {
        this.title = title;
        this.skillLevels = skillLevels;
    }
}
