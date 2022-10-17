package de.uscoutz.nexus.networking.packet.packets.player;

import com.mojang.authlib.GameProfile;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import net.animalshomeland.bukkit.piglin.Piglin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PacketAddTablistPlayer extends Packet {

    private UUID uuid;
    private String displayName, scoreboardTeam, playerName, prefix, color, joinPower;

    public PacketAddTablistPlayer(String password, UUID uuid, String playerName, String displayName, String scoreboardTeam, String prefix, String color, String joinPower) {
        super(password);
        this.uuid = uuid;
        this.displayName = displayName;
        this.scoreboardTeam = scoreboardTeam;
        this.playerName = playerName;
        this.prefix = prefix;
        this.color = color;
        this.joinPower = joinPower;
    }

    @Override
    public Object execute() {
        Bukkit.getConsoleSender().sendMessage("[Nexus] Adding player " + playerName);
        NexusPlugin plugin = NexusPlugin.getInstance();
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        GameProfile gameProfile = null;
        ResultSet gameProfileResultSet = NexusPlugin.getInstance().getDatabaseAdapter().get("players", "player", String.valueOf(uuid));
        try {
            if(gameProfileResultSet.next()) {
                gameProfile = GameProfileSerializer.fromString(gameProfileResultSet.getString("gameprofile"));
                ServerPlayer serverPlayer = new ServerPlayer(nmsServer, nmsWorld, gameProfile, null);
                serverPlayer.setPos(0, 0, 0);
                serverPlayer.listName = Component.literal(displayName);

                for(Player players : Bukkit.getOnlinePlayers()) {
                    ServerGamePacketListenerImpl ps = ((CraftPlayer) players).getHandle().connection;

                    ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer));
                    ps.send(new ClientboundAddPlayerPacket(serverPlayer));

                    if(prefix != null && joinPower != null) {
                        String pn = playerName;

                        Scoreboard scoreboard = players.getScoreboard();

                        Team team = scoreboard.getTeam(joinPower + pn) != null ? scoreboard.getTeam(joinPower + pn) : scoreboard.registerNewTeam(joinPower + pn);
                        if(!team.getEntries().contains(playerName)) {
                            team.addEntry(playerName);
                        }

                        players.setScoreboard(scoreboard);
                    }
                }

                Piglin.getInstance().setTag();
                plugin.getNexusServer().getServerPlayerMap().put(uuid, serverPlayer);
                Bukkit.getConsoleSender().sendMessage("[Nexus] Added player " + playerName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return null;
    }
}
