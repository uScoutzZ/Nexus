package de.uscoutz.nexus.wave.listener.schematic;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.events.SchematicDamagedEvent;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import de.uscoutz.nexus.wave.raids.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SchematicDamagedListener implements Listener {

    private NexusWavePlugin plugin;

    public SchematicDamagedListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSchematicDamaged(SchematicDamagedEvent event) {
        Profile profile = event.getProfile();
        RaidProfile raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());
        if(raidProfile.getRaid() != null) {
            Raid raid = raidProfile.getRaid();
            if(!raid.getAttacked().contains(event.getSchematic())) {
                raid.getAttacked().add(event.getSchematic());
                profile.broadcast(true, "raid_schematic-attacked", "raid_" + event.getSchematic().getSchematic().getSchematicType().toString().toLowerCase() + "-attacked");
            }
        }
    }
}
