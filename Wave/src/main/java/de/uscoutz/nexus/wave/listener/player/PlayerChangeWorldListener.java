package de.uscoutz.nexus.wave.listener.player;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.player.RaidPlayer;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerChangeWorldListener implements Listener {

    private NexusWavePlugin plugin;

    public PlayerChangeWorldListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        RaidPlayer raidPlayer = plugin.getPlayerManager().getRaidPlayerMap().get(player.getUniqueId());
        if(plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().containsKey(event.getFrom())) {
            Profile oldProfile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(event.getFrom());
            RaidProfile oldRaidProfile = plugin.getRaidManager().getRaidProfileMap().get(oldProfile.getProfileId());
            if(oldRaidProfile.getRaid() != null) {
                raidPlayer.leaveRaid(oldRaidProfile.getRaid());
            }
            if(plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().containsKey(player.getWorld())) {
                Profile newProfile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
                RaidProfile newRaidProfile = plugin.getRaidManager().getRaidProfileMap().get(newProfile.getProfileId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(newRaidProfile.getRaid() != null) {
                            raidPlayer.joinRaid(newRaidProfile.getRaid());
                        }
                    }
                }.runTaskLater(plugin, 20);
            }
        }
    }
}
