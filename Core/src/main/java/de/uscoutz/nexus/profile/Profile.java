package de.uscoutz.nexus.profile;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.worlds.NexusWorld;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Profile {

    private NexusPlugin plugin;

    @Getter
    private UUID profileId, owner;
    @Getter
    private Map<UUID, ProfilePlayer> members;
    @Getter
    private int nexusLevel;
    @Getter
    private long start, lastActivity;
    @Getter
    private NexusWorld world;
    @Getter
    private boolean loading;

    public Profile(UUID profileId, NexusPlugin plugin) {
        this.plugin = plugin;
        this.profileId = profileId;
        this.members = new HashMap<>();
        if(exists()) {
            prepare();
        }
    }

    public void loadMembers() {
        ResultSet resultSet = plugin.getDatabaseAdapter().getAsync("playerProfiles", "profileId", String.valueOf(profileId));

        try {
            while(resultSet.next()) {
                UUID player = UUID.fromString(resultSet.getString("player"));
                long joinedProfile = resultSet.getLong("joinedProfile"),
                        profilePlaytime = resultSet.getLong("playtime");
                String inventory = resultSet.getString("inventory");
                new ProfilePlayer(this, player, profilePlaytime, joinedProfile, inventory, plugin);
            }
            plugin.getProfileManager().getProfilesMap().put(profileId, this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(Player requestor) {
        if(loading) {
            requestor.sendMessage("§cThis profile is already being loaded");
        } else {
            loading = true;
            if(!plugin.getWorldManager().getEmptyWorlds().isEmpty()) {
                requestor.sendMessage("§aLoading world");
                world = new NexusWorld(this, plugin);
            } else {
                requestor.sendMessage("§cCouldn't load profile because there aren't any empty maps");
                loading = false;
            }
        }
    }

    public void prepare() {
        ResultSet resultSet = plugin.getDatabaseAdapter().getAsync("profiles", "profileId", String.valueOf(profileId));

        try {
            while(resultSet.next()) {
                owner = UUID.fromString(resultSet.getString("owner"));
                nexusLevel = resultSet.getInt("nexusLevel");
                start = resultSet.getLong("start");
                lastActivity = resultSet.getLong("lastActivity");
            }
            plugin.getProfileManager().getProfilesMap().put(profileId, this);
            loadMembers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void create(UUID owner, int currentSlot) {
        plugin.getDatabaseAdapter().set("profiles", profileId, owner, 0, System.currentTimeMillis(), System.currentTimeMillis());
        plugin.getDatabaseAdapter().set("playerProfiles", owner, profileId, currentSlot,
                System.currentTimeMillis(), 0, "empty");
        this.owner = owner;
        Bukkit.getPlayer(owner).sendMessage("§aProfile data was created");
    }

    public boolean loaded() {
        return world != null;
    }

    public boolean exists() {
        return plugin.getDatabaseAdapter().keyExistsAsync("profiles", "profileId", profileId);
    }

    public boolean isPrepared() {
        return plugin.getProfileManager().getProfilesMap().containsKey(profileId);
    }
}
