package de.uscoutz.nexus.skills;

import de.uscoutz.nexus.skills.rewards.ItemReward;
import de.uscoutz.nexus.skills.rewards.MoneyReward;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Skill {

    MINING("Mining",
            new SkillLevel(25, new MoneyReward(25), new ItemReward(new ItemStack(Material.COBBLESTONE, 10))),
            new SkillLevel(100, new MoneyReward(100), new ItemReward(new ItemStack(Material.STONE, 8))),
            new SkillLevel(300, new MoneyReward(150), new ItemReward(new ItemStack(Material.STONE, 16))),
            new SkillLevel(500, new MoneyReward(300), new ItemReward(new ItemStack(Material.COAL, 12))),
            new SkillLevel(1000, new MoneyReward(500), new ItemReward(new ItemStack(Material.IRON_INGOT, 8))),
            new SkillLevel(1500, new MoneyReward(750), new ItemReward(new ItemStack(Material.ANDESITE, 32)), new ItemReward(new ItemStack(Material.DIORITE, 32))),
            new SkillLevel(3000, new MoneyReward(1000), new ItemReward(new ItemStack(Material.END_STONE, 32))),
            new SkillLevel(4000, new MoneyReward(1500), new ItemReward(new ItemStack(Material.BLACKSTONE, 64))),
            new SkillLevel(5000, new MoneyReward(1750), new ItemReward(new ItemStack(Material.REDSTONE, 63))),
            new SkillLevel(7500, new MoneyReward(2000), new ItemReward(new ItemStack(Material.NETHERITE_INGOT, 8)))),
    WOODCUTTING("Woodcutting",
            new SkillLevel(25, new MoneyReward(25), new ItemReward(new ItemStack(Material.DARK_OAK_LOG, 4))),
            new SkillLevel(100, new MoneyReward(100), new ItemReward(new ItemStack(Material.DARK_OAK_LOG, 8))),
            new SkillLevel(300, new MoneyReward(150), new ItemReward(new ItemStack(Material.ACACIA_LOG, 4))),
            new SkillLevel(500, new MoneyReward(300), new ItemReward(new ItemStack(Material.ACACIA_LOG, 8))),
            new SkillLevel(1000, new MoneyReward(500), new ItemReward(new ItemStack(Material.SPRUCE_LOG, 8))),
            new SkillLevel(1500, new MoneyReward(750), new ItemReward(new ItemStack(Material.SPRUCE_LOG, 12))),
            new SkillLevel(3000, new MoneyReward(1000), new ItemReward(new ItemStack(Material.BIRCH_LOG, 12))),
            new SkillLevel(4000, new MoneyReward(1500), new ItemReward(new ItemStack(Material.BIRCH_LOG, 16))),
            new SkillLevel(5000, new MoneyReward(1750), new ItemReward(new ItemStack(Material.DARK_OAK_LOG, 32)), new ItemReward(new ItemStack(Material.ACACIA_LOG, 16)), new ItemReward(new ItemStack(Material.SPRUCE_LOG, 12)), new ItemReward(new ItemStack(Material.BIRCH_LOG, 8))),
            new SkillLevel(7500, new MoneyReward(2000), new ItemReward(new ItemStack(Material.DARK_OAK_LOG, 64)), new ItemReward(new ItemStack(Material.ACACIA_LOG, 32)), new ItemReward(new ItemStack(Material.SPRUCE_LOG, 24)), new ItemReward(new ItemStack(Material.BIRCH_LOG, 16)))),
    FARMING("Farming",
            new SkillLevel(25, new MoneyReward(50)),
            new SkillLevel(100, new MoneyReward(150)),
            new SkillLevel(300, new MoneyReward(300)),
            new SkillLevel(500, new MoneyReward(500)),
            new SkillLevel(1000, new MoneyReward(750)),
            new SkillLevel(1500, new MoneyReward(1000)),
            new SkillLevel(3000, new MoneyReward(1250)),
            new SkillLevel(4000, new MoneyReward(1500)),
            new SkillLevel(5000, new MoneyReward(1750)),
            new SkillLevel(7500, new MoneyReward(2000))),
    COMBAT("Combat",
            new SkillLevel(25, new MoneyReward(50)),
            new SkillLevel(100, new MoneyReward(150)),
            new SkillLevel(300, new MoneyReward(300)),
            new SkillLevel(500, new MoneyReward(500)),
            new SkillLevel(1000, new MoneyReward(750)),
            new SkillLevel(1500, new MoneyReward(1000)),
            new SkillLevel(3000, new MoneyReward(1250)),
            new SkillLevel(4000, new MoneyReward(1500)),
            new SkillLevel(5000, new MoneyReward(1750)),
            new SkillLevel(7500, new MoneyReward(2000)));

    @Getter
    private final SkillLevel[] skillLevels;
    @Getter
    private String title;

    Skill(String title, SkillLevel... skillLevels) {
        this.title = title;
        this.skillLevels = skillLevels;
    }
}
