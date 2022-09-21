package de.uscoutz.nexus.schematic.listener.inventory;

import de.uscoutz.nexus.events.SchematicInventoryOpenedEvent;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SchematicInventoryOpenedListener implements Listener {

    private NexusSchematicPlugin plugin;

    public SchematicInventoryOpenedListener(NexusSchematicPlugin nexusSchematicPlugin) {
        plugin = nexusSchematicPlugin;
    }

    @EventHandler
    public void onSchematicInventoryOpened(SchematicInventoryOpenedEvent event) {
        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(event.getPlayer().getWorld());

        for(SchematicItem schematicItem : plugin.getSchematicItemManager().getSchematicItemMap().values()) {
            event.getSimpleInventory().addItem(schematicItem.getItemStack());
        }
    }
}
