package de.uscoutz.nexus.schematic.listener.profile;

import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.events.ProfileCheckoutEvent;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.DestroyAnimation;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ProfileCheckoutListener implements Listener {

    private NexusSchematicPlugin plugin;

    public ProfileCheckoutListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProfileCheckout(ProfileCheckoutEvent event) {
        Profile profile = event.getProfile();
        SchematicProfile sProfile = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId());

        for(BuiltSchematic builtSchematic : sProfile.getSchematicsByRegion().values()) {
            builtSchematic.saveDamage();
        }

        for(String items : sProfile.getBoughtItems().keySet()) {
            if(sProfile.getBoughtItems().get(items) != 0) {
                if(plugin.getNexusPlugin().getDatabaseAdapter().keyExistsTwo("boughtItems", "profileId", String.valueOf(profile.getProfileId()), "item", items)) {
                    plugin.getNexusPlugin().getDatabaseAdapter().updateTwo("boughtItems", "profileId",
                            String.valueOf(profile.getProfileId()), "item", items,
                            new DatabaseUpdate("amount", String.valueOf(sProfile.getBoughtItems().get(items))));
                } else {
                    plugin.getNexusPlugin().getDatabaseAdapter().set("boughtItems", String.valueOf(profile.getProfileId()), items, String.valueOf(sProfile.getBoughtItems().get(items)));
                }
            }
        }

        for(UUID schematicId : profile.getSchematicIds()) {
            Schematic.destroy(profile, schematicId, plugin, DestroyAnimation.SILENT,
                    plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getBuiltSchematics().get(
                            schematicId).getSchematic().getSchematicType());
        }

        for(Entity entity : profile.getWorld().getWorld().getEntities()) {
            if(entity.getType() != EntityType.ENDER_CRYSTAL && !profile.getWorld().getWorldEntities().contains(entity)) {
                entity.remove();
            }
        }

        plugin.getSchematicManager().getSchematicProfileMap().remove(profile.getProfileId());
    }
}
