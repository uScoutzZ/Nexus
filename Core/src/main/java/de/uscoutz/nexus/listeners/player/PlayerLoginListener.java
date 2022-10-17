package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.server.PacketServerRequestPlayers;
import de.uscoutz.nexus.player.NexusPlayer;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ICloudService;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

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
        UUID uuid = player.getUniqueId();

        if(plugin.getNexusServer().getServerPlayerMap().containsKey(uuid)) {
            ServerPlayer serverPlayer = plugin.getNexusServer().getServerPlayerMap().get(uuid);

            NexusPlugin.getInstance().getServer().getOnlinePlayers()
                    .forEach(onlinePlayer -> {
                        ServerGamePacketListenerImpl ps = ((CraftPlayer) onlinePlayer).getHandle().connection;

                        ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer));
                        //ps.send(new ClientboundAddPlayerPacket(serverPlayer));
                    });
            plugin.getNexusServer().getServerPlayerMap().remove(uuid);
        }
    }
}
