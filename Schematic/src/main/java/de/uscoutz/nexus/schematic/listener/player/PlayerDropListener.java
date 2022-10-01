package de.uscoutz.nexus.schematic.listener.player;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.collector.Collector;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDropListener implements Listener {

    private NexusSchematicPlugin plugin;

    public PlayerDropListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        final boolean[] canPlayerPickup = {true};

        new BukkitRunnable() {
            @Override
            public void run() {
                if(event.getItemDrop().isOnGround()) {
                    Block dropBlock = event.getItemDrop().getLocation().clone().subtract(0, 1, 0).getBlock();
                    Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
                    SchematicProfile schematicProfile = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId());
                    Collector collector = schematicProfile.getCollectors().get(dropBlock);
                    if(collector != null) {
                        collector.collect(player, event.getItemDrop());
                        canPlayerPickup[0] = false;
                    } /*else {
                        player.getInventory().addItem(event.getItemDrop().getItemStack());
                        event.getItemDrop().remove();
                        player.sendMessage("§cCouldn't find a collector here");
                    }*/
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
        event.getItemDrop().setCanPlayerPickup(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                event.getItemDrop().setCanPlayerPickup(canPlayerPickup[0]);
            }
        }.runTaskLater(plugin, 50);
    }
}
