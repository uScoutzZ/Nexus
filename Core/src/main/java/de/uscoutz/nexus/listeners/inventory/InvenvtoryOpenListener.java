package de.uscoutz.nexus.listeners.inventory;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class InvenvtoryOpenListener implements Listener {

    private NexusPlugin plugin;

    public InvenvtoryOpenListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(event.getInventory().getType() == InventoryType.MERCHANT) {
            event.setCancelled(true);
        }
    }
}
