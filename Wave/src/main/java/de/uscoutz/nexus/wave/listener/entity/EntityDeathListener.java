package de.uscoutz.nexus.wave.listener.entity;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.player.RaidPlayer;
import de.uscoutz.nexus.wave.profile.Raid;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

    private NexusWavePlugin plugin;

    public EntityDeathListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(event.getEntity().getWorld());
        RaidProfile raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());

        if(raidProfile.getRaid() != null) {
            Raid raid = raidProfile.getRaid();
            Entity entity = event.getEntity();
            if(raid.getMobs().contains(entity)) {

            }
        }
    }
}
