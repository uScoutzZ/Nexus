package de.uscoutz.nexus.schematic.listener.block;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockSpreadListener implements Listener {

    private NexusSchematicPlugin plugin;

    public BlockSpreadListener(NexusSchematicPlugin nexusPlugin) {
        this.plugin = nexusPlugin;
    }

    @EventHandler
    public void onGrow(BlockSpreadEvent event) {
        event.setCancelled(true);
    }
}
