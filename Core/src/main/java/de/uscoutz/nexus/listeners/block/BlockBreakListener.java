package de.uscoutz.nexus.listeners.block;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockBreakListener implements Listener {

    private NexusPlugin plugin;

    public BlockBreakListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta itemMeta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        if(plugin.getToolManager().getBlockResistance().containsKey(event.getBlock().getType())) {
            int blockResistance = plugin.getToolManager().getBlockResistance().get(event.getBlock().getType());
            if(itemMeta != null && plugin.getToolManager().isTool(itemMeta)) {
                PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                int breakingPower = plugin.getToolManager().getBreakingPower(itemMeta);
                String key = plugin.getToolManager().getKey(itemMeta);
                if(breakingPower >= blockResistance) {
                    event.setCancelled(false);
                    return;
                } else {
                    player.sendMessage(plugin.getLocaleManager().translate("de_DE", "tool-break_too-high-resistance"));
                }
            }
        }
        event.setCancelled(true);
    }
}
