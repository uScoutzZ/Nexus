package de.uscoutz.nexus.listeners.entity;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupItemListener implements Listener {

    private NexusPlugin plugin;

    public EntityPickupItemListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if(plugin.getToolManager().getBlockDrop().containsKey(event.getItem().getItemStack().getType())) {
            event.getItem().getItemStack().setType(plugin.getToolManager().getBlockDrop().get(event.getItem().getItemStack().getType()));
        }
    }
}
