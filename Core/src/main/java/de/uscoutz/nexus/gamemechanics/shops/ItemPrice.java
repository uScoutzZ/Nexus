package de.uscoutz.nexus.gamemechanics.shops;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.utilities.InventoryManager;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemPrice implements NexusPrice{

    private String display;
    @Getter
    private List<String> displayList;
    private List<ItemStack> neededItems;

    public ItemPrice(List<ItemStack> neededItems, NexusPlugin plugin) {
        this.neededItems = neededItems;
        displayList = new ArrayList<>();
        display = "§7";
        for(ItemStack item : neededItems) {
            displayList.add("§e" + item.getAmount() + "x §7" + LegacyComponentSerializer.legacyAmpersand().serialize(Component.translatable(item.translationKey())));
            display += item.getAmount() + "x " + LegacyComponentSerializer.legacyAmpersand().serialize(Component.translatable(item.translationKey())) + "\n §7";
        }
    }

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public void remove(Player player) {
        InventoryManager.removeNeededItems(player, neededItems);
    }

    @Override
    public boolean containsNeeded(Player player) {
        boolean playerHasItems = true;
        for(ItemStack neededStack : neededItems) {
            if(!player.getInventory().containsAtLeast(neededStack, neededStack.getAmount())) {
                playerHasItems = false;
            }
        }
        return playerHasItems;
    }
}
