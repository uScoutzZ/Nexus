package de.uscoutz.nexus.schematic.listener.block;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockPhysicsListener implements Listener {

    private NexusSchematicPlugin plugin;

    public BlockPhysicsListener(NexusSchematicPlugin nexusPlugin) {
        this.plugin = nexusPlugin;
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }
}
