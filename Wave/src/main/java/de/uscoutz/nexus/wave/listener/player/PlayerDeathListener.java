package de.uscoutz.nexus.wave.listener.player;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.profile.ProfilePlayer;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private NexusWavePlugin plugin;

    public PlayerDeathListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        event.deathMessage(null);

        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
        ProfilePlayer profilePlayer = profile.getMembers().get(player.getUniqueId());
        profilePlayer.addDeath();
    }
}
