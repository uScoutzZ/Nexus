package de.uscoutz.nexus.networking;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.profiles.PacketRequestProfilesCount;
import de.uscoutz.nexus.profile.Profile;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ICloudService;
import eu.thesimplecloud.plugin.startup.CloudPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.*;

public class NexusServer {

    private NexusPlugin plugin;

    @Getter
    private RMap<UUID, String> profilesServerMap;
    @Getter
    private RedissonClient redissonClient;
    @Getter
    private Map<String, Integer> profileCountByServer;
    @Getter
    private Map<UUID, Integer> profileToLoad;

    public NexusServer(NexusPlugin plugin) {
        this.plugin = plugin;
        profileCountByServer = new HashMap<>();
        profileToLoad = new HashMap<>();
        createRedisConnection();
    }

    public void createRedisConnection() {
        Config config = new Config();
        config.setNettyThreads(256);
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redissonClient = Redisson.create(config);
        this.redissonClient = redissonClient;
        profilesServerMap = redissonClient.getMap("nexus.profilesServer");
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
        for(Profile profile : NexusPlugin.getInstance().getProfileManager().getProfilesMap().values()) {
            if(profile.loaded()) {
                count++;
            }
        }

        return count;
    }

    public ICloudService getEmptiestServer() {
        updatePlayersOnServer();

        String emptiestServer = getThisServiceName();
        int profileCount = getProfileCount();
        for(String server : profileCountByServer.keySet()) {
            Bukkit.broadcastMessage("§2" + server + " | " + profileCountByServer.get(server));
            if(profileCountByServer.get(server) < profileCount) {
                Bukkit.broadcastMessage("§3ja guter count für " + server);
                emptiestServer = server;
                profileCount = profileCountByServer.get(server);
            }
        }

        return getServiceByName(emptiestServer);
    }
}
