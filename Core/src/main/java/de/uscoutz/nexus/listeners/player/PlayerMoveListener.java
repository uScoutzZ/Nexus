package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.biomes.Biome;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.scoreboards.NexusScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private NexusPlugin plugin;

    public PlayerMoveListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
        Biome biome = plugin.getBiomeManager().getBiome(player.getLocation());

        if(nexusPlayer.getBiome() == null || !nexusPlayer.getBiome().equals(biome)) {
            nexusPlayer.setBiome(biome);
            if(nexusPlayer.getNexusScoreboard() != null) {
                nexusPlayer.getNexusScoreboard().update(NexusScoreboard.ScoreboardUpdateType.BIOME);
            }
        }
    }
}
