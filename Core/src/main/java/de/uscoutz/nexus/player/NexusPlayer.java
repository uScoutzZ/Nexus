package de.uscoutz.nexus.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;
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
    private long firstLogin, playtime;
    @Getter
    private int currentProfileSlot;


    public NexusPlayer(Player player, NexusPlugin plugin) {
        this.plugin = plugin;
        this.player = player;
        profilesMap = new HashMap<>();
        if(registered()) {
            player.sendMessage("§aRegistered, loading data");
            load();
        } else {
            player.sendMessage("§cNot registered, creating data");
            plugin.getDatabaseAdapter().setAsync("players", player.getUniqueId(), 0, System.currentTimeMillis(), 0);
            player.sendMessage("§aCreated data, loading data now");
            load();
            player.sendMessage("§aProfile is being created");
            Profile profile = new Profile(UUID.randomUUID(), plugin);
            profile.create(player.getUniqueId());
        }
        player.sendMessage("§aProfiles are being loaded");

        new BukkitRunnable() {
            @Override
            public void run() {
                loadProfiles();
            }
        }.runTaskLater(plugin, 2);
    }

    public void setActiveProfile(int profileSlot) {
        Profile profile = profilesMap.get(profileSlot);
        if(profile.isPrepared()) {
            player.sendMessage("§aProfile is prepared");
            if(profile.loaded()) {
                player.sendMessage("§aProfile is already loaded, teleporting");
            } else {
                player.sendMessage("§aProfile not loaded, loading");
                profile.load(player);
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
        }
    }

    public boolean registered() {
        return plugin.getDatabaseAdapter().keyExistsAsync("players", "player", player.getUniqueId());
    }

    private void load() {
        ResultSet resultSet = plugin.getDatabaseAdapter().getAsync("players", "player", String.valueOf(player.getUniqueId()));
        try {
            if(resultSet.next()) {
                firstLogin = resultSet.getLong("firstLogin");
                playtime = resultSet.getLong("playtime");
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
                    player.sendMessage("constructor");
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
            player.sendMessage("§eProfile(s) found");
            setActiveProfile(currentProfileSlot);
        }
    }
}
