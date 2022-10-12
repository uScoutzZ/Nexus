package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class PlayerBucketEmptyListener implements Listener {

    private NexusPlugin plugin;

    public PlayerBucketEmptyListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(true);
    }
}
