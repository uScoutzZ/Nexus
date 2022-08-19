package de.uscoutz.nexus.schematic.listener.block;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class BlockGrowListener implements Listener {

    private NexusSchematicPlugin plugin;

    public BlockGrowListener(NexusSchematicPlugin nexusPlugin) {
        this.plugin = nexusPlugin;
    }

    @EventHandler
    public void onGrow(BlockGrowEvent event) {
        event.setCancelled(true);
    }
}
