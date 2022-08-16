package de.uscoutz.nexus.schematic.listener;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceListener implements Listener {

    private NexusSchematicPlugin plugin;

    public BlockPlaceListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());

        event.getPlayer().getInventory().getItemInMainHand();
        ItemStack itemStack = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if(itemStack != null && itemStack.getItemMeta() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(plugin.getSchematicItemManager().isSchematicItem(itemMeta)) {
                event.setCancelled(true);
                PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                String key = dataContainer.get(new NamespacedKey(NexusPlugin.getInstance().getName().toLowerCase(), "key"), PersistentDataType.STRING);
                SchematicItem schematicItem = plugin.getSchematicItemManager().getSchematicItemMap().get(key);

                schematicItem.getSchematic().build(event.getBlock().getLocation().subtract(0, 1, 0), schematicPlayer.getRotationFromFacing(player.getFacing()));
            }
        }
    }
}
