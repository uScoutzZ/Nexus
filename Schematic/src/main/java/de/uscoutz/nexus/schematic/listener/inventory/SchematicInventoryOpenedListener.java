package de.uscoutz.nexus.schematic.listener.inventory;

import de.uscoutz.nexus.events.SchematicInventoryOpenedEvent;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SchematicInventoryOpenedListener implements Listener {

    private NexusSchematicPlugin plugin;

    public SchematicInventoryOpenedListener(NexusSchematicPlugin nexusSchematicPlugin) {
        plugin = nexusSchematicPlugin;
    }

    @EventHandler
    public void onSchematicInventoryOpened(SchematicInventoryOpenedEvent event) {
        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(event.getPlayer().getWorld());

        for(SchematicItem schematicItem : plugin.getSchematicItemManager().getSchematicItemMap().values()) {
            if(schematicItem.isObtainable() && (schematicItem.getTask() == null || profile.getQuests().containsKey(schematicItem.getTask()))) {
                ItemStack itemStack = plugin.getNexusPlugin().getInventoryManager().getShopItem(event.getPlayer(),
                        event.getSimpleInventory(), schematicItem.getItemStack(), schematicItem.getIngredients());
                //event.getSimpleInventory().addItem(itemStack);
            }
        }
    }
}
