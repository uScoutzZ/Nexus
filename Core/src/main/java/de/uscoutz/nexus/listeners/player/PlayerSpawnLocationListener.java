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

        if(plugin.getNexusServer().getSpectators().containsKey(player.getUniqueId())) {
            event.setSpawnLocation(Bukkit.getPlayer(plugin.getNexusServer().getSpectators().remove(player.getUniqueId())).getLocation());
            return;
        }

        if(nexusPlayer.getCurrentProfile() != null && nexusPlayer.getCurrentProfile().getWorld() != null) {
            nexusPlayer.getCurrentProfile().getWorld().setSpawn(plugin.getLocationManager().getLocation("base-spawn",
                    nexusPlayer.getCurrentProfile().getWorld().getWorld()));
            event.setSpawnLocation(nexusPlayer.getCurrentProfile().getWorld().getSpawn());
        }
    }
}
