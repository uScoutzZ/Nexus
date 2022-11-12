package de.uscoutz.nexus.schematic.listener.schematic;

import de.uscoutz.nexus.events.SchematicItemBoughtEvent;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SchematicItemBoughtListener implements Listener {

    private NexusSchematicPlugin plugin;

    public SchematicItemBoughtListener(NexusSchematicPlugin nexusSchematicPlugin) {
        plugin = nexusSchematicPlugin;
    }

    @EventHandler
    public void onSchematicItemBought(SchematicItemBoughtEvent event) {
        SchematicProfile schematicProfile = plugin.getSchematicManager().getSchematicProfileMap().get(event.getProfile().getProfileId());
        int amount = schematicProfile.getBoughtItems().getOrDefault(event.getKey(), 0);
        schematicProfile.getBoughtItems().put(event.getKey(), amount+1);
    }
}
