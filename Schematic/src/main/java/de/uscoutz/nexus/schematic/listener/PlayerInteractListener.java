package de.uscoutz.nexus.schematic.listener;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    private NexusSchematicPlugin plugin;

    public PlayerInteractListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());

        if(player.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE) {
            event.setCancelled(true);
            int i = 0;
            if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if(schematicPlayer.getLocations().containsKey(0)
                        && schematicPlayer.getLocations().get(0).getBlock().getLocation().equals(event.getClickedBlock().getLocation())) {
                    return;
                }
                schematicPlayer.getLocations().put(0, event.getClickedBlock().getLocation());
                i = 1;
            } else if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if(schematicPlayer.getLocations().containsKey(1)
                        && schematicPlayer.getLocations().get(1).getBlock().getLocation().equals(event.getClickedBlock().getLocation())) {
                    return;
                }
                schematicPlayer.getLocations().put(1, event.getClickedBlock().getLocation());
                i = 2;
            }
            if(i != 0) {
                player.sendMessage("§6Position " + i + " set to " + event.getClickedBlock().getX() + ", " +
                        event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ());
            }
        }
    }
}
