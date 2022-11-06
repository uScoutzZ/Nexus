package de.uscoutz.nexus.wave.listener.schematic;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.events.SchematicUpdateEvent;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SchematicUpdateListener implements Listener {

    private NexusWavePlugin plugin;

    public SchematicUpdateListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSchematicUpdate(SchematicUpdateEvent event) {
        BuiltSchematic schematic = event.getSchematic();
        Profile profile = event.getProfile();
        RaidProfile raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());
        if(schematic.getSchematic().getSchematicType() == SchematicType.NEXUS
                && schematic.getCondition() == Condition.DESTROYED) {
            if(raidProfile.getRaid() != null) {
                raidProfile.getRaid().end(true, false);
            }
        }
    }
}
