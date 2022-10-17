package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.player.PacketAddTablistPlayer;
import de.uscoutz.nexus.networking.packet.packets.server.PacketServerRequestPlayers;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.quests.Quest;
import de.uscoutz.nexus.quests.Task;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ICloudService;
import net.animalshomeland.bukkit.piglin.Piglin;
import net.animalshomeland.bukkit.piglin.group.Group;
import net.animalshomeland.bukkit.piglin.player.PermissionPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private NexusPlugin plugin;

    public PlayerJoinListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
        plugin.getNexusServer().getOnlinePlayers().add(player.getUniqueId());

        if(player.getGameMode() == GameMode.CREATIVE) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(true);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for(ICloudService iCloudService : plugin.getNexusServer().getNexusServers()) {
                    if(!iCloudService.getName().equals(plugin.getNexusServer().getThisServiceName())) {
                        Piglin.getInstance().getPlayerData().loadPlayerData(player.getUniqueId(), playerData -> {
                            Piglin.getInstance().getGroupManager().getGroup(playerData.getRang().toLowerCase()).ifPresentOrElse(group -> {
                                new PacketAddTablistPlayer("123", player.getUniqueId(), player.getName(), player.getPlayerListName(), group.getJoinPower().toString(), group.getPrefix(), group.getColor(), group.getJoinPower().toString()).send(iCloudService);
                            }, () -> {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.kick(Component.text("§cDein Rang konnte nicht geladen werden. Bitte melde dich bei einem Admin."));
                                    }
                                }.runTask(plugin);
                            });
                        });
                    }
                }
                for(ICloudService iCloudService : plugin.getNexusServer().getNexusServers()) {
                    if (!iCloudService.getName().equals(plugin.getNexusServer().getThisServiceName())) {
                        new PacketServerRequestPlayers("123", CloudAPI.getInstance().getThisSidesName()).send(iCloudService);
                    }
                }
            }
        }.runTaskLater(plugin, 20);


        /*

        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }


        for(UUID onlinePlayer : plugin.getNexusServer().getOnlinePlayers()) {
            if(!onlinePlayer.equals(player.getUniqueId())) {
                Bukkit.getConsoleSender().sendMessage("[Nexus] Onlineplayer: " + onlinePlayer);
                ServerPlayer serverPlayer = plugin.getNexusServer().getServerPlayerMap().get(onlinePlayer);
                String name = serverPlayer.getName().getString();
                *//*ComponentUtils.
                String name = net.minecraft.network.chat.Component.Serializer.*//*

                Scoreboard finalScoreboard = scoreboard;
                Piglin.getInstance().getPlayerData().loadPlayerData(onlinePlayer, playerData -> {
                    Piglin.getInstance().getGroupManager().getGroup(playerData.getRang().toLowerCase()).ifPresent(group -> {
                        int joinPower = group.getJoinPower();
                        Bukkit.getConsoleSender().sendMessage("[Nexus] Joinpower: " + joinPower + " Name: " + name);

                        Team sbTeam = finalScoreboard.getTeam(group.getJoinPower() + name);

                        if (sbTeam == null) {
                            sbTeam = finalScoreboard.registerNewTeam(joinPower + name);
                        }
                        sbTeam.addEntry(name);

                        ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;

                        ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer));
                        ps.send(new ClientboundAddPlayerPacket(serverPlayer));
                    });
                });
            }
        }*/
    }
}
