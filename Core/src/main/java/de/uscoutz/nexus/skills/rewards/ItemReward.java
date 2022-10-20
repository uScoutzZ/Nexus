package de.uscoutz.nexus.skills.rewards;

import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemReward implements SkillReward{

    private String display;
    private ItemStack rewardItem;

    public ItemReward(ItemStack rewardItem) {
        this.rewardItem = rewardItem;
        String name = LegacyComponentSerializer.legacyAmpersand().serialize(Component.translatable(rewardItem.translationKey()));
        if(rewardItem.getItemMeta() != null && rewardItem.getItemMeta().displayName() != null) {
            name = LegacyComponentSerializer.legacyAmpersand().serialize(rewardItem.getItemMeta().displayName());
        }
        display = "§7" + rewardItem.getAmount() + "x " + name;
    }

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
