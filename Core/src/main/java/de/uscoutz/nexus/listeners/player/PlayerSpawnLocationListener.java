package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnLocationListener implements Listener {

    private NexusPlugin plugin;

    public PlayerSpawnLocationListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());

        if(nexusPlayer.getCurrentProfile() != null) {
            event.setSpawnLocation(nexusPlayer.getCurrentProfile().getWorld().getSpawn());
        }
    }
}
