package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import eu.thesimplecloud.api.service.ICloudService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerLoginListener implements Listener {

    private NexusPlugin plugin;

    public PlayerLoginListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
        nexusPlayer.setPlayer(player);
    }
}
