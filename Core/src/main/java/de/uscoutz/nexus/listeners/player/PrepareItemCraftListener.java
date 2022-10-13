package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.Damageable;

public class PrepareItemCraftListener implements Listener {

    private NexusPlugin nexusPlugin;

    public PrepareItemCraftListener(NexusPlugin nexusPlugin) {
        this.nexusPlugin = nexusPlugin;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if(recipe != null && Enchantment.DURABILITY.canEnchantItem(recipe.getResult())) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }
}
