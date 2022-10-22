package de.uscoutz.nexus.networking;

import com.mojang.authlib.GameProfile;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.profiles.PacketRequestProfilesCount;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ICloudService;
import eu.thesimplecloud.plugin.startup.CloudPlugin;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.sql.ResultSet;
import java.util.*;

public class NexusServer {

    private NexusPlugin plugin;

    @Getter
    private RMap<UUID, String> profilesServerMap;
    @Getter
    private RMap<String, Integer> profileCountByServer;
    @Getter
    private RedissonClient redissonClient;
    /*@Getter
    private Map<String, Integer> profileCountByServer;*/
    @Getter
    private Map<UUID, Integer> profileToLoad;
    @Getter
    private Map<UUID, ServerPlayer> serverPlayerMap;
    @Getter
    private RList<UUID> onlinePlayers;

    public NexusServer(NexusPlugin plugin) {
        this.plugin = plugin;

        serverPlayerMap = new HashMap<>();
        profileToLoad = new HashMap<>();
        createRedisConnection();
        Bukkit.getConsoleSender().sendMessage("[Nexus] Onlineplayers: " + onlinePlayers.size());
        for(UUID onlinePlayer : onlinePlayers) {
            Bukkit.getConsoleSender().sendMessage("[Nexus] Onlineplayer: " + onlinePlayer);
            MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
            ServerLevel nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
            GameProfile gameProfile;
            ResultSet gameProfileResultSet = NexusPlugin.getInstance().getDatabaseAdapter().get("players", "player", String.valueOf(onlinePlayer));
            try {
                if (gameProfileResultSet.next()) {
                    gameProfile = GameProfileSerializer.fromString(gameProfileResultSet.getString("gameprofile"));
                    ServerPlayer serverPlayer = new ServerPlayer(nmsServer, nmsWorld, gameProfile, null);
                    serverPlayerMap.put(onlinePlayer, serverPlayer);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public void createRedisConnection() {
        Config config = new Config();
        config.setNettyThreads(256);
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redissonClient = Redisson.create(config);
        this.redissonClient = redissonClient;
        profilesServerMap = redissonClient.getMap("nexus.profilesServer");
        profileCountByServer = redissonClient.getMap("nexus.profileCount");
        onlinePlayers = redissonClient.getList("nexus.onlinePlayers");
        profileCountByServer.put(getThisServiceName(), 0);
    }

    public ICloudService getServiceByName(String name) {
        return CloudAPI.getInstance().getCloudServiceManager().getCloudServiceByName(name);
    }

    public String getThisServiceName() {
        return CloudPlugin.getInstance().getThisServiceName();
    }

    public List<ICloudService> getNexusServers() {
        return CloudAPI.getInstance().getCloudServiceGroupManager().getServiceGroupByName(
                plugin.getConfig().getString("cloudtype")).getAllServices();
    }

    public void updatePlayersOnServer() {
        for(ICloudService iCloudService : getNexusServers()) {
            new PacketRequestProfilesCount("123", getThisServiceName()).send(iCloudService);
        }
    }

    public int getProfileCount() {
        int count = 0;
        for(Profile profile : plugin.getProfileManager().getProfilesMap().values()) {
            if(profile.loaded()) {
                count++;
            }
        }

        return count;
    }

    public ICloudService getEmptiestServer() {
        //updatePlayersOnServer();

        String emptiestServer = getThisServiceName();
        int profileCount = getProfileCount();
        for(String server : profileCountByServer.keySet()) {
            if(profileCountByServer.get(server) < profileCount) {
                if(server.split("-")[0].equals(getThisServiceName().split("-")[0])) {
                    emptiestServer = server;
                    profileCount = profileCountByServer.get(server);
                }
            }
        }

        if(emptiestServer.equals(getThisServiceName()) && plugin.getWorldManager().getEmptyWorlds().size() == 0) {
            return null;
        }

        return getServiceByName(emptiestServer);
    }
}
