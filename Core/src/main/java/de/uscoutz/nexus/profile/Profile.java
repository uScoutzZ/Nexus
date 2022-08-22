package de.uscoutz.nexus.profile;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.events.ProfileCheckoutEvent;
import de.uscoutz.nexus.events.ProfileLoadEvent;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopKicked;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.utilities.InventorySerializer;
import de.uscoutz.nexus.worlds.NexusWorld;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Container;
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
    private Map<String, String> storages;
    @Getter
    private Map<String, Container> storageBlocks;
    @Getter @Setter
    private int nexusLevel, concurrentlyBuilding;
    @Getter
    private long start, lastActivity;
    @Getter
    private NexusWorld world;
    @Getter
    private boolean loading;
    @Getter
    private List<UUID> schematicIds;
    @Getter
    private List<Region> regions;

    @Getter
    private int[] timeToCheckout;
    @Getter
    private BukkitTask checkoutTask;

    public Profile(UUID profileId, NexusPlugin plugin) {
        this.plugin = plugin;
        this.profileId = profileId;
        members = new HashMap<>();
        storages = new HashMap<>();
        storageBlocks = new HashMap<>();
        schematicIds = new ArrayList<>();
        regions = new ArrayList<>();
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
        plugin.getDatabaseAdapter().delete("schematics", "profileId", profileId);
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
            Bukkit.getPluginManager().callEvent(new ProfileCheckoutEvent(Profile.this));
            plugin.getWorldManager().getEmptyWorlds().add(world.getWorld());
            plugin.getWorldManager().getWorldProfileMap().remove(world.getWorld());
            plugin.getNexusServer().getProfilesServerMap().remove(profileId);
            plugin.getDatabaseAdapter().update("profiles", "profileId", profileId,
                    new DatabaseUpdate("nexusLevel", nexusLevel),
                    new DatabaseUpdate("lastActivity", System.currentTimeMillis()));
            saveStorages();
            world = null;
            loading = false;
        }

       /* new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getProfileManager().getProfilesMap().remove(profileId);
            }
        }.runTaskLater(plugin, 3);*/
    }

    public void saveStorages() {
        for(String storageId : storageBlocks.keySet()) {
            String contents = InventorySerializer.toBase64(storageBlocks.get(storageId).getInventory());
            plugin.getDatabaseAdapter().updateTwo("storages", "profileId", profileId,
                    "storageId", storageId,
                    new DatabaseUpdate("inventory", contents));
            storages.replace(storageId, contents);
        }
        storageBlocks.clear();
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
                } else {
                    ProfilePlayer profilePlayer = members.get(player);
                    profilePlayer.setInventoryBase64(inventory);
                    profilePlayer.setPlaytime(profilePlaytime);
                }
            }
            plugin.getProfileManager().getProfilesMap().put(profileId, this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        Bukkit.getConsoleSender().sendMessage("[Nexus] Now loading profile");
        if(!loading) {
            loading = true;
            loadMembers();
            if(!plugin.getWorldManager().getEmptyWorlds().isEmpty()) {
                ResultSet resultSet = plugin.getDatabaseAdapter().getAsync("storages", "profileId", String.valueOf(profileId));

                try {
                    while(resultSet.next()) {
                        String storageId = resultSet.getString("storageId");
                        String inventory = resultSet.getString("inventory");
                        storages.put(storageId, inventory);
                    }
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }

                world = new NexusWorld(this, plugin);
                Bukkit.getConsoleSender().sendMessage("[Nexus] Set world");
                plugin.getNexusServer().getProfilesServerMap().put(profileId, plugin.getNexusServer().getThisServiceName());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getPluginManager().callEvent(new ProfileLoadEvent(Profile.this));
                    }
                }.runTask(plugin);
            } else {
                loading = false;
            }
        }
    }

    public void prepare() {
        Bukkit.getConsoleSender().sendMessage("[Nexus] Preparing");
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

        Location location = plugin.getLocationManager().getLocation("nexus", Bukkit.getWorlds().get(0)).subtract(0, 1, 0);
        String nexusLocation = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
        plugin.getDatabaseAdapter().set("schematics", profileId, UUID.randomUUID(), "NEXUS", 0, 0, nexusLocation, System.currentTimeMillis());
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
