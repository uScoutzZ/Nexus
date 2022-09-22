package de.uscoutz.nexus.listeners.block;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.worlds.NexusWorld;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;

public class BlockBreakListener implements Listener {

    private NexusPlugin plugin;

    public BlockBreakListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if(player.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(false);
            return;
        }

        ItemMeta itemMeta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        int blockResistance, breakingPower = 0;
        if(!plugin.getToolManager().getBlockResistance().containsKey(event.getBlock().getType())) {
            event.setCancelled(true);
            return;
        } else {
            blockResistance = plugin.getToolManager().getBlockResistance().get(event.getBlock().getType());
            if(itemMeta != null && plugin.getToolManager().isTool(itemMeta)) {
                breakingPower = plugin.getToolManager().getBreakingPower(itemMeta);
            }
        }
        if(breakingPower >= blockResistance) {
            event.setCancelled(false);
            int rangeMin = plugin.getConfig().getInt("respawn-range-min");
            int rangeMax = plugin.getConfig().getInt("respawn-range-max");
            long respawnAfter = new Random().nextLong(rangeMax-rangeMin)+rangeMin;
            if(event.getBlock().getType().toString().contains("_LOG") || event.getBlock().getType().toString().contains("_WOOD")) {
                NexusWorld nexusWorld = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld()).getWorld();
                boolean inList = false;
                for(Location location : nexusWorld.getBrokenBlocks().keySet()) {
                    if(location.distance(event.getBlock().getLocation()) < 15) {
                        inList = true;
                        nexusWorld.getBrokenBlocks().get(location).put(event.getBlock().getLocation(), event.getBlock().getBlockData());
                        break;
                    }
                }
                if(!inList) {
                    nexusWorld.getBrokenBlocks().put(event.getBlock().getLocation(), new HashMap<>());
                    nexusWorld.getBrokenBlocks().get(event.getBlock().getLocation()).put(event.getBlock().getLocation(), event.getBlock().getBlockData().clone());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for(Location location : nexusWorld.getBrokenBlocks().get(event.getBlock().getLocation()).keySet()) {
                                Block block = event.getBlock().getWorld().getBlockAt(location);
                                BlockData blockData = nexusWorld.getBrokenBlocks().get(event.getBlock().getLocation()).get(location);
                                block.setType(blockData.getMaterial());
                                block.setBlockData(blockData);
                            }

                            nexusWorld.getBrokenBlocks().remove(event.getBlock().getLocation());
                        }
                    }.runTaskLater(plugin, respawnAfter*20);
                }
            } else {
                BlockData blockData = event.getBlock().getBlockData().clone();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getBlock().setType(blockData.getMaterial());
                        event.getBlock().setBlockData(blockData);
                    }
                }.runTaskLater(plugin, respawnAfter*20);
            }
        } else {
            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "tool-break_too-high-resistance"));
            event.setCancelled(true);
        }
    }
}
