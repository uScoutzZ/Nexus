package de.uscoutz.nexus.schematic.listener.entity;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangeBlockListener implements Listener {

    private NexusSchematicPlugin plugin;

    public EntityChangeBlockListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if(event.getEntity() instanceof FallingBlock) {
            event.setCancelled(true);
        }
    }
}
