package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.player.PacketAddTablistPlayer;
import de.uscoutz.nexus.networking.packet.packets.player.PacketRemoveTablistPlayer;
import eu.thesimplecloud.api.service.ICloudService;
import net.animalshomeland.bukkit.piglin.Piglin;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerQuitListener implements Listener {

    private NexusPlugin plugin;

    public PlayerQuitListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getNexusServer().getOnlinePlayers().remove(player.getUniqueId());

        plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).checkout();
        for(ICloudService iCloudService : plugin.getNexusServer().getNexusServers()) {
            if(!iCloudService.getName().equals(plugin.getNexusServer().getThisServiceName())) {
                new PacketRemoveTablistPlayer("123", player.getUniqueId()).send(iCloudService);
            }
        }
    }
}
