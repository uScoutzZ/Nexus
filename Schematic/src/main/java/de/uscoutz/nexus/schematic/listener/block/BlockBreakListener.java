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
        if(region != null) {
            if(profile.getSchematicsByRegion().containsKey(region)) {
                /*player.sendMessage(profile.getSchematicsByRegion().get(region).getSchematic().getSchematicType() + " "
                        + profile.getSchematicsByRegion().get(region).getSchematic().getLevel());
                player.sendMessage("id: " + profile.getSchematicsByRegion().get(region).getSchematicId());
                player.sendMessage("hits: " + profile.getSchematicsByRegion().get(region).getDamage());
                player.sendMessage("percent: " + profile.getSchematicsByRegion().get(region).getPercentDamage());*/
                event.setCancelled(true);
            } else {
                player.sendMessage("§cNo schematic for this region found");
            }
        }
    }
}
