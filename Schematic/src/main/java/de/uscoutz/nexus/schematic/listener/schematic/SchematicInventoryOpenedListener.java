package de.uscoutz.nexus.schematic.listener.schematic;

import de.uscoutz.nexus.events.SchematicInventoryOpenedEvent;
import de.uscoutz.nexus.gamemechanics.shops.ItemPrice;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.Bukkit;
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
        SchematicProfile schematicProfile = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId());

        for(SchematicItem schematicItem : plugin.getSchematicItemManager().getSchematicItemMap().values()) {
            if(schematicItem.isObtainable()) {
                if((schematicItem.getTask() == null || profile.getQuests().containsKey(schematicItem.getTask()))
                        && schematicItem.getRequiredLevel() <= profile.getNexusLevel()) {
                    if(schematicItem.getMaxObtainable() > schematicProfile.getBoughtItems().get(schematicItem.getKey())) {
                        ItemStack itemStack = plugin.getNexusPlugin().getInventoryManager().getShopItem(event.getPlayer(),
                                event.getSimpleInventory(), schematicItem.getItemStack(),
                                schematicItem.getPrices());
                    } else {
                        ItemStack itemStack = plugin.getNexusPlugin().getInventoryManager().getShopItem(event.getPlayer(),
                                event.getSimpleInventory(), schematicItem.getItemStack(),
                                plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "tool_maximum-reached",
                                        schematicProfile.getBoughtItems().get(schematicItem.getKey()), schematicItem.getMaxObtainable()),
                                schematicItem.getPrices());
                    }
                    //event.getSimpleInventory().addItem(itemStack);
                } else {
                    ItemStack itemStack = plugin.getNexusPlugin().getInventoryManager().getShopItem(event.getPlayer(),
                            event.getSimpleInventory(), schematicItem.getItemStack(),
                            plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "tool_not-unlocked"),
                            schematicItem.getPrices());
                }
            }
        }
    }
}
