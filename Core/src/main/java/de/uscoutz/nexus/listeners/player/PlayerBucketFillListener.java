package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class PlayerBucketFillListener implements Listener {

    private NexusPlugin plugin;

    public PlayerBucketFillListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        BlockData blockData = event.getBlock().getBlockData().clone();

        int rangeMin = plugin.getConfig().getInt("respawn-range-min");
        int rangeMax = plugin.getConfig().getInt("respawn-range-max");
        long respawnAfter = new Random().nextLong(rangeMax-rangeMin)+rangeMin;
        new BukkitRunnable() {
            @Override
            public void run() {
                event.getBlock().setType(blockData.getMaterial());
                event.getBlock().setBlockData(blockData);
            }
        }.runTaskLater(plugin, respawnAfter*20);
    }
}
