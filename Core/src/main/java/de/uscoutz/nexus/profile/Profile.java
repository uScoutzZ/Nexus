package de.uscoutz.nexus.profile;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopKicked;
import de.uscoutz.nexus.networking.packet.packets.profiles.PacketPlayerReloadProfiles;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.worlds.NexusWorld;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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

    @Getter
    private int[] timeToCheckout;
    @Getter
    private BukkitTask checkoutTask;

    public Profile(UUID profileId, NexusPlugin plugin) {
        this.plugin = plugin;
        this.profileId = profileId;
        this.members = new HashMap<>();
        if(exists()) {
            prepare();
        }
    }

    public void scheduleCheckout() {
        timeToCheckout = new int[]{plugin.getConfig().getInt("profile-checkout-after")};
        checkoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(timeToCheckout[0] == 0) {
                    checkout();
                    cancel();
                }
                timeToCheckout[0]--;
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    public void cancelCheckout() {
        if(checkoutTask != null) {
            checkoutTask.cancel();
        }
    }

    public List<NexusPlayer> getActivePlayers() {
        List<NexusPlayer> players = new ArrayList<>();
        for(Player activePlayer : world.getWorld().getPlayers()) {
            players.add(plugin.getPlayerManager().getPlayersMap().get(activePlayer.getUniqueId()));
        }

        return players;
    }

    public void delete() {
        if(loaded()) {
            for(Player all : world.getWorld().getPlayers()) {
                all.sendMessage(plugin.getLocaleManager().translate("de_DE", "profile-deleted-teleport"));
                plugin.getPlayerManager().getPlayersMap().get(all.getUniqueId()).switchProfile(0);
            }
        }

        for(UUID member : members.keySet()) {
            ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(member).getBlockingOrNull();
            if(cloudPlayer != null) {
                if(cloudPlayer.isOnline() && cloudPlayer.getConnectedServer().getGroupName().equals(plugin.getConfig().getString("cloudtype"))) {
                    new PacketCoopKicked("123", member, profileId).send(cloudPlayer.getConnectedServer());
                }
            }
        }

        plugin.getDatabaseAdapter().delete("playerProfiles", "profileId", profileId);
        plugin.getDatabaseAdapter().deleteTwo("profiles", "owner", owner, "profileId", profileId);
        new BukkitRunnable() {
            @Override
            public void run() {
                checkout();
            }
        }.runTaskLater(plugin, 8);
    }

    public void checkout() {
        if(loaded()) {
            for(Player all : world.getWorld().getPlayers()) {
                all.kick(Component.text(plugin.getLocaleManager().translate("de_DE", "profile-unloaded")));
            }
            plugin.getWorldManager().getEmptyWorlds().add(world.getWorld());
            plugin.getNexusServer().getProfilesServerMap().remove(profileId);
        }

        plugin.getDatabaseAdapter().update("profiles", "profileId", profileId,
                new DatabaseUpdate("nexusLevel", nexusLevel),
                new DatabaseUpdate("lastActivity", System.currentTimeMillis()));

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
        new BukkitRunnable() {
            @Override
            public void run() {
                loadMembers();
            }
        }.runTaskLater(plugin, 3);
    }

    public void kickPlayer(UUID player) {
        members.remove(player);
        plugin.getDatabaseAdapter().deleteTwoAsync("playerProfiles", "player", player, "profileId", String.valueOf(profileId));
        new BukkitRunnable() {
            @Override
            public void run() {
                loadMembers();
            }
        }.runTaskLater(plugin, 3);
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
