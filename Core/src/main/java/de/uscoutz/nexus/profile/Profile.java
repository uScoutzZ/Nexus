package de.uscoutz.nexus.profile;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.worlds.NexusWorld;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

    public void checkout() {
        if(loaded()) {
            for(Player all : world.getWorld().getPlayers()) {
                all.kick(Component.text(plugin.getLocaleManager().translate("de_DE", "profile-unloaded")));
            }
            plugin.getWorldManager().getEmptyWorlds().add(world.getWorld());
            plugin.getNexusServer().getProfilesServerMap().remove(profileId);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getProfileManager().getProfilesMap().remove(profileId);
            }
        }.runTaskLater(plugin, 3);
    }

    public void loadMembers() {
        ResultSet resultSet = plugin.getDatabaseAdapter().getAsync("playerProfiles", "profileId", String.valueOf(profileId));

        try {
            while(resultSet.next()) {
                UUID player = UUID.fromString(resultSet.getString("player"));
                long joinedProfile = resultSet.getLong("joinedProfile"),
                        profilePlaytime = resultSet.getLong("playtime");
                String inventory = resultSet.getString("inventory");
                if(!members.containsKey(player)) {
                    new ProfilePlayer(this, player, profilePlaytime, joinedProfile, inventory, plugin);
                }
            }
            plugin.getProfileManager().getProfilesMap().put(profileId, this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        if(!loading) {
            loading = true;
            if(!plugin.getWorldManager().getEmptyWorlds().isEmpty()) {
                world = new NexusWorld(this, plugin);
                plugin.getNexusServer().getProfilesServerMap().put(profileId, plugin.getNexusServer().getThisServiceName());
            } else {
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

    public void create(UUID owner, int profileSlot) {
        plugin.getDatabaseAdapter().set("profiles", profileId, owner, 0, System.currentTimeMillis(), System.currentTimeMillis());
        plugin.getDatabaseAdapter().set("playerProfiles", owner, profileId, profileSlot,
                System.currentTimeMillis(), 0, "empty");
        this.owner = owner;
    }

    public void addPlayer(int profileSlot, UUID player) {
        plugin.getDatabaseAdapter().set("playerProfiles", player, profileId, profileSlot,
                System.currentTimeMillis(), 0, "empty");
        loadMembers();
    }

    public void kickPlayer(UUID player) {
        members.remove(player);
        plugin.getDatabaseAdapter().deleteTwoAsync("playerProfiles", "player", player, "profileId", String.valueOf(profileId));
        loadMembers();
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
