package de.uscoutz.nexus.skills.rewards;

import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemReward implements SkillReward{

    private String display;
    @Setter
    private ItemStack rewardItem;

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public void setDisplay(String display) {
        this.display = display;
    }

    @Override
    public void addReward(Player player) {
        player.getInventory().addItem(rewardItem);
    }
}
