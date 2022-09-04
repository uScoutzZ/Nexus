package de.uscoutz.nexus.wave.listener.profile;

import de.uscoutz.nexus.events.ProfileCheckoutEvent;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ProfileCheckoutListener implements Listener {

    private NexusWavePlugin plugin;

    public ProfileCheckoutListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProfileCheckout(ProfileCheckoutEvent event) {
        Profile profile = event.getProfile();
        RaidProfile raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());
        if(raidProfile.getTask() != null) {
            raidProfile.getTask().cancel();
        }

        if(raidProfile.getRaid() != null) {
            raidProfile.getRaid().end();
        }

        plugin.getRaidManager().getRaidProfileMap().remove(profile.getProfileId());
    }
}
