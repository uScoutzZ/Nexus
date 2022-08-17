package de.uscoutz.nexus.schematic.listener.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerItemHeldListener implements Listener {

    private NexusSchematicPlugin plugin;

    public PlayerItemHeldListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());

        ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());
        if(itemStack != null && itemStack.getItemMeta() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(plugin.getSchematicItemManager().isSchematicItem(itemMeta)) {
                PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                String key = dataContainer.get(new NamespacedKey(NexusPlugin.getInstance().getName().toLowerCase(), "key"), PersistentDataType.STRING);
                SchematicItem schematicItem = plugin.getSchematicItemManager().getSchematicItemMap().get(key);
                schematicPlayer.startPreview(itemStack, schematicItem);
            }
        }
    }
}
