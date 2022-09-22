package de.uscoutz.nexus.wave.listener.player;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private NexusWavePlugin plugin;

    public PlayerRespawnListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
        event.setRespawnLocation(profile.getWorld().getSpawn());
    }
}
