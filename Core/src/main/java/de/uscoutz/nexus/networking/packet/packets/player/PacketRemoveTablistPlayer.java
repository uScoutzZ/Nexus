package de.uscoutz.nexus.networking.packet.packets.player;

import com.mojang.authlib.GameProfile;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PacketRemoveTablistPlayer extends Packet {

    private UUID uuid;

    public PacketRemoveTablistPlayer(String password, UUID uuid) {
        super(password);
        this.uuid = uuid;
    }

    @Override
    public Object execute() {
        NexusPlugin plugin = NexusPlugin.getInstance();

        ServerPlayer serverPlayer = plugin.getNexusServer().getServerPlayerMap().get(uuid);

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            ServerGamePacketListenerImpl ps = ((CraftPlayer) onlinePlayer).getHandle().connection;

            ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer));
        });
        plugin.getNexusServer().getServerPlayerMap().remove(uuid);
        return null;
    }

    /*public static void createNPC(Location loc, String name) {
        // get NMS world
        WorldServer nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name); // create game profile
        // use class given just before
        FakePlayer ep = new FakePlayer(nmsWorld, profile, loc);
        // now quickly made player connection
        ep.playerConnection = new PlayerConnection(ep.server, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), ep);

        nmsWorld.addEntity(ep); // add entity to world
        ep.spawn(); // spawn for actual online players
        // now you can keep the FakePlayer instance for next player or just to check
    }*/

    public static void showAll(ServerPlayer entityPlayer) {
        ClientboundPlayerInfoPacket playerInfoAdd = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER);
        ClientboundAddPlayerPacket namedEntitySpawn = new ClientboundAddPlayerPacket(entityPlayer);
        ClientboundRotateHeadPacket headRotation = new ClientboundRotateHeadPacket(entityPlayer, (byte) ((0 * 256f) / 360f));
        //ClientboundPlayerInfoPacket playerInfoRemove = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER);
        for (Player player : Bukkit.getOnlinePlayers()) {
            ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
            connection.send(playerInfoAdd);
            connection.send(namedEntitySpawn);
            connection.send(headRotation);
            player.sendMessage("show player");
            //connection.send(playerInfoRemove);
        }
        /*entityPlayer.getEntityData().
                set(net.minecraft.world.entity.player.Player.DELTA_AFFECTED_BY_BLOCKS_BELOW, (byte) 0xFF);*/
    }
}
