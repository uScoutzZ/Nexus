package de.uscoutz.nexus.schematic.listener.profile;

import de.uscoutz.nexus.events.ProfileCheckoutEvent;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ProfileCheckoutListener implements Listener {

    private NexusSchematicPlugin plugin;

    public ProfileCheckoutListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProfileCheckout(ProfileCheckoutEvent event) {
        Profile profile = event.getProfile();

        for(UUID schematicId : profile.getSchematicIds()) {
            Schematic.destroy(profile, schematicId, plugin, false);
        }

        for(Entity entity : profile.getWorld().getWorld().getEntities()) {
            if(entity.getType() != EntityType.ENDER_CRYSTAL) {
                entity.remove();
            }
        }

        plugin.getSchematicManager().getSchematicProfileMap().remove(profile.getProfileId());
    }
}
