package de.uscoutz.nexus.wave.listener.player;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.player.RaidPlayer;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private NexusWavePlugin plugin;

    public PlayerJoinListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        RaidPlayer raidPlayer = new RaidPlayer(player, plugin);
        Profile newProfile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
        RaidProfile newRaidProfile = plugin.getRaidManager().getRaidProfileMap().get(newProfile.getProfileId());

        if(newRaidProfile.getRaid() != null) {
            raidPlayer.joinRaid(newRaidProfile.getRaid());
        }
    }
}
