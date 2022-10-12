package de.uscoutz.nexus.schematic.listener.block;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
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
        if(event.getChangedBlockData() instanceof Door) {
            Door door = (Door) event.getChangedBlockData();
            if(door.getHalf() == Bisected.Half.TOP) {
                Door down = (Door) event.getBlock().getLocation().clone().subtract(0, 1, 0).getBlock().getBlockData();
                event.setCancelled(down.isOpen() == door.isOpen());
            }
        } else {
            event.setCancelled(true);
        }
    }
}
