package de.uscoutz.nexus.wave.listener.profile;

import de.uscoutz.nexus.events.ProfileLoadEvent;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProfileLoadListener implements Listener {

    private NexusWavePlugin plugin;

    public ProfileLoadListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProfileLoad(ProfileLoadEvent event) {
        Profile profile = event.getProfile();
        RaidProfile raidProfile = new RaidProfile(profile, plugin);
        raidProfile.load();
        long cooldown = plugin.getConfig().getLong("cooldown");

        if(raidProfile.getLastRaid()+cooldown < System.currentTimeMillis()) {
            raidProfile.scheduleRaid();
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    raidProfile.scheduleRaid();
                }
            }.runTaskLater(plugin, (raidProfile.getLastRaid()+cooldown)-System.currentTimeMillis());
        }
    }
}
