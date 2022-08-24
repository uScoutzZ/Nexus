package de.uscoutz.nexus.schematic.listener.block;

import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private NexusSchematicPlugin plugin;

    public BlockBreakListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        SchematicProfile profile = plugin.getSchematicManager().getSchematicProfileMap().get(
                plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld()).getProfileId());

        Region region = plugin.getNexusPlugin().getRegionManager().getRegion(event.getBlock().getLocation());
        if(region == null) {
            player.sendMessage("no region " + plugin.getNexusPlugin().getRegionManager().getRegions().size());
        } else {
            player.sendMessage(profile.getSchematicsByRegion().get(region).getSchematic().getSchematicType() + "");
        }



        event.setCancelled(true);

    }
}
