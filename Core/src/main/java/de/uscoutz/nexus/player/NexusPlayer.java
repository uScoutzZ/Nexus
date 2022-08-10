package de.uscoutz.nexus.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import de.uscoutz.nexus.utilities.InventorySerializer;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ICloudService;
import eu.thesimplecloud.plugin.startup.CloudPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class NexusPlayer {

    private NexusPlugin plugin;

    @Getter
    private Player player;
    @Getter
    private Map<Integer, Profile> profilesMap;
    @Getter
    private long firstLogin, generalPlaytime, joined;
    @Getter
    private int currentProfileSlot;

    public NexusPlayer(Player player, NexusPlugin plugin) {
        this.plugin = plugin;
        this.player = player;
        joined = System.currentTimeMillis();
        profilesMap = new HashMap<>();
        if(registered()) {
            player.sendMessage("§aRegistered, loading data");
            load();
        } else {
            player.sendMessage("§cNot registered, creating data");
            plugin.getDatabaseAdapter().setAsync("players", player.getUniqueId(), 0, System.currentTimeMillis(),
                    0, GameProfileSerializer.toString(((CraftPlayer) player).getProfile()));
            player.sendMessage("§aCreated data, loading data now");
            load();
            player.sendMessage("§aProfile is being created");
            Profile profile = new Profile(UUID.randomUUID(), plugin);
            profile.create(player.getUniqueId(), 0);
        }
        player.sendMessage("§aProfiles are being loaded");

        new BukkitRunnable() {
            @Override
            public void run() {
                loadProfiles();
            }
        }.runTaskLater(plugin, 2);
        plugin.getPlayerManager().getPlayersMap().put(player, this);
    }

    public void switchProfile(int profileSlot) {
        getCurrentProfile().getMembers().get(player.getUniqueId()).checkout(joined);
        player.getInventory().clear();
        currentProfileSlot = profileSlot;
        setActiveProfile(profileSlot);
    }

    public boolean setActiveProfile(int profileSlot) {
        Profile profile = profilesMap.get(profileSlot);
        ICloudService emptiestServer = plugin.getNexusServer().getEmptiestServer();
        if(profile.isPrepared()) {
            player.sendMessage("§aProfile is prepared");
            if(profile.loaded()) {
                player.sendMessage("§aProfile is already loaded, teleporting");
            } else {
                player.sendMessage("§aProfile not loaded, loading");
                if(plugin.getNexusServer().getProfilesServerMap().containsKey(profile.getProfileId())) {
                    player.sendMessage("§cProfile is loaded somewhere");
                    String server = plugin.getNexusServer().getProfilesServerMap().get(profile.getProfileId());
                    if(!server.equals(plugin.getNexusServer().getThisServiceName())) {
                        player.sendMessage("§cProfile is loaded on another server");
                        ICloudService iCloudService = plugin.getNexusServer().getServiceByName(server);
                        if(iCloudService != null && iCloudService.isOnline()) {
                            player.sendMessage("§eServer is online, sending");
                            CloudAPI.getInstance().getCloudPlayerManager().connectPlayer(CloudAPI.getInstance().getCloudPlayerManager().getCachedCloudPlayer(player.getUniqueId()),
                                    iCloudService);
                        } else {
                            player.sendMessage("§6Server is offline. Resetting session...");
                            plugin.getNexusServer().getProfilesServerMap().remove(profile.getProfileId());
                            return setActiveProfile(profileSlot);
                        }
                    } else {
                        player.sendMessage("§c§lProfile should be loaded on your current server. Session resetted");
                        plugin.getNexusServer().getProfilesServerMap().remove(profile.getProfileId());
                        return setActiveProfile(profileSlot);
                    }
                } else {
                    profile.load(player);
                    /*if(CloudPlugin.getInstance().getThisServiceName().equals(emptiestServer.getName())) {
                        profile.load(player);
                    } else {
                        player.sendMessage("§dOther server is more empty, sending");
                        CloudAPI.getInstance().getCloudPlayerManager().connectPlayer(CloudAPI.getInstance().getCloudPlayerManager().getCachedCloudPlayer(player.getUniqueId()), emptiestServer);
                    }*/
                }
                if(profile.loaded()) {
                    player.sendMessage("§aWorld was loaded, teleporting");
                } else {
                    player.sendMessage("§cWorld wasn't loaded for some reason");
                }
            }
        } else {
            player.sendMessage("§cThe profile is not prepared to be loaded");
        }

        if(profile.loaded()) {
            player.sendMessage("§aTeleporting");
            player.teleport(profile.getWorld().getSpawn());
            if(!profile.getMembers().get(player.getUniqueId()).getInventoryBase64().equals("empty")) {
                player.getInventory().setContents(InventorySerializer.fromBase64(profile.getMembers().get(
                        player.getUniqueId()).getInventoryBase64()).getContents());
                player.sendMessage("§eInventory loaded");
            }
            return true;
        } else {
            player.sendMessage("§4Profile is not loaded");
            return false;
        }
    }

    public void checkout() {
        plugin.getDatabaseAdapter().updateAsync("players", "player", player.getUniqueId(),
                new DatabaseUpdate("playtime", generalPlaytime + (System.currentTimeMillis()-joined)),
                new DatabaseUpdate("gameprofile", GameProfileSerializer.toString(((CraftPlayer) player).getProfile())),
                new DatabaseUpdate("currentProfile", currentProfileSlot));
        if(getCurrentProfile().loaded()) {
            getCurrentProfile().getMembers().get(player.getUniqueId()).checkout(joined);
        }
        plugin.getPlayerManager().getPlayersMap().remove(player);
    }

    public Profile getCurrentProfile() {
        return profilesMap.get(currentProfileSlot);
    }

    public boolean registered() {
        return plugin.getDatabaseAdapter().keyExistsAsync("players", "player", player.getUniqueId());
    }

    private void load() {
        ResultSet resultSet = plugin.getDatabaseAdapter().getAsync("players", "player", String.valueOf(player.getUniqueId()));
        try {
            if(resultSet.next()) {
                firstLogin = resultSet.getLong("firstLogin");
                generalPlaytime = resultSet.getLong("playtime");
                currentProfileSlot = resultSet.getInt("currentProfile");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadProfiles() {
        ResultSet resultSet = plugin.getDatabaseAdapter().get("playerProfiles", "player", String.valueOf(player.getUniqueId()));
        try {
            while(resultSet.next()) {
                UUID profileId = UUID.fromString(resultSet.getString("profileId"));
                int slot = resultSet.getInt("slot");
                Profile profile;
                if(plugin.getProfileManager().getProfilesMap().containsKey(profileId)) {
                    profile = plugin.getProfileManager().getProfilesMap().get(profileId);
                } else {
                    profile = new Profile(profileId, plugin);
                }
                profilesMap.put(slot, profile);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if(profilesMap.isEmpty()) {
            player.sendMessage("§cYou don't have any profiles");
        } else {
            player.sendMessage("§e" + profilesMap.size() + " Profile(s) found");
        }
    }
}
