package de.uscoutz.nexus.listeners.block;

import de.uscoutz.nexus.NexusPlugin;
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
        if(itemMeta != null) {
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(plugin.getName(), "breakingPower");
            if(dataContainer.has(namespacedKey)) {
                int breakingPower = dataContainer.get(namespacedKey, PersistentDataType.INTEGER);
                String key = dataContainer.get(new NamespacedKey(plugin.getName(), "key"), PersistentDataType.STRING);
                player.sendMessage("Key: " + key);
                player.sendMessage("breakingPower: " + breakingPower);
            }
        }
    }
}
