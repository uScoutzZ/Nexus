package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.collector.Collector;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDropListener implements Listener {

    private NexusPlugin plugin;

    public PlayerDropListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        event.getItemDrop().setCanPlayerPickup(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(event.getItemDrop().isOnGround()) {
                    Block dropBlock = event.getItemDrop().getLocation().clone().subtract(0, 1, 0).getBlock();
                    Collector collector = plugin.getCollectorManager().getCollectors().get(dropBlock);
                    if(dropBlock.getType() == Material.EMERALD_BLOCK && collector != null) {
                        collector.collect(player, event.getItemDrop());
                    } else {
                        player.getInventory().addItem(event.getItemDrop().getItemStack());
                        event.getItemDrop().remove();
                        player.sendMessage("§cCouldn't find a collector here");
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
