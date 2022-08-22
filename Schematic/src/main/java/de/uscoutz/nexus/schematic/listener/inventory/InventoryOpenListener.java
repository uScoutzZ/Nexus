package de.uscoutz.nexus.schematic.listener.inventory;

import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryOpenListener implements Listener {

    private NexusSchematicPlugin plugin;

    public InventoryOpenListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        /*if(inventory instanceof FurnaceInventory) {
            FurnaceInventory furnaceInventory = (FurnaceInventory) inventory;
            furnaceInventory.addItem(ItemBuilder.create(Material.ACACIA_PLANKS).build());
            furnaceInventory.addItem(ItemBuilder.create(Material.ACACIA_LOG).build());
            furnaceInventory.addItem(ItemBuilder.create(Material.DIAMOND_AXE).build());

            for(ItemStack itemStack : furnaceInventory.getContents()) {
                player.sendMessage(itemStack.getType() + "");
            }
        }*/
    }
}
