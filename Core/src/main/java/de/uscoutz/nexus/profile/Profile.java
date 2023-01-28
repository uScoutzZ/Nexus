package de.uscoutz.nexus.profile;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.events.ProfileCheckoutEvent;
import de.uscoutz.nexus.events.ProfileLoadEvent;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopKicked;
import de.uscoutz.nexus.networking.packet.packets.profiles.PacketReloadProfileMembers;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.quests.Quest;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.utilities.InventoryManager;
import de.uscoutz.nexus.worlds.NexusWorld;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import eu.thesimplecloud.api.service.ICloudService;
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
    private int nexusLevel, concurrentlyBuilding, wonRaids, lostRaids, highestTower = -1;
    @Getter @Setter
    private long start, lastActivity, souls;
    @Getter
    private NexusWorld world;
    @Getter
    private boolean loading;
    @Getter
    private List<UUID> schematicIds;
    @Getter
    private List<Region> regions;
    @Getter
    private Map<Task, Quest> quests;

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
        quests = new HashMap<>();
        schematicIds = new ArrayList<>();
        regions = new ArrayList<>();
        if(exists()) {
            prepare();
        }
    }

    public void broadcast(String key, Object... args) {
        for(NexusPlayer nexusPlayer : getActivePlayers()) {
            nexusPlayer.getPlayer().sendMessage(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), key, args));
        }
    }

    public void broadcast(boolean isKeyArgument, String key, String... keyArgument) {
        for(NexusPlayer nexusPlayer : getActivePlayers()) {
            String[] args = new String[keyArgument.length];
            for(int i = 0; i < keyArgument.length; i++) {
                args[i] = plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), keyArgument[i]);
            }
            nexusPlayer.getPlayer().sendMessage(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), key, args));
        }
    }

    public void scheduleCheckout() {
        timeToCheckout = new int[]{plugin.getConfig().getInt("profile-checkout-after")};
        Bukkit.getConsoleSender().sendMessage("[Nexus] Scheduling checkout for profile " + profileId + " in " + timeToCheckout[0] + " seconds.");
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
                if(all.getUniqueId().equals(owner)) {
                    all.sendMessage(plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(
                            all.getUniqueId()).getLanguage(), "profile-deleted-teleport"));
                }
                //plugin.getPlayerManager().getPlayersMap().get(all.getUniqueId()).switchProfile(0);
            }
        }

        plugin.getDatabaseAdapter().delete("playerProfiles", "profileId", profileId);
        plugin.getDatabaseAdapter().delete("schematics", "profileId", profileId);
        plugin.getDatabaseAdapter().delete("quests", "profileId", profileId);
        plugin.getDatabaseAdapter().delete("raids", "profileId", profileId);
        plugin.getDatabaseAdapter().delete("storages", "profileId", profileId);
        plugin.getDatabaseAdapter().deleteTwo("profiles", "owner", owner, "profileId", profileId);
        new BukkitRunnable() {
            @Override
            public void run() {
                checkout();
            }
        }.runTaskLater(plugin, 20);

        for(UUID member : members.keySet()) {
            ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(member).getBlockingOrNull();
            if(cloudPlayer != null) {
                if(cloudPlayer.isOnline() && cloudPlayer.getConnectedServer().getGroupName().equals(plugin.getConfig().getString("cloudtype"))) {
                    new PacketCoopKicked("123", member, profileId, !member.equals(owner)).send(cloudPlayer.getConnectedServer());
                }
            }
        }


    }

    public void checkout() {
        if(loaded()) {
            for(Player all : world.getWorld().getPlayers()) {
                all.sendMessage(plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(all.getUniqueId()).getLanguage(), "profile-unloaded"));
                all.kick(Component.text(""));
            }
            Bukkit.getPluginManager().callEvent(new ProfileCheckoutEvent(Profile.this));
            plugin.getWorldManager().getEmptyWorlds().add(world.getWorld());
            plugin.getWorldManager().getWorldProfileMap().remove(world.getWorld());
            plugin.getNexusServer().getProfilesServerMap().remove(profileId);
            plugin.getNexusServer().getProfileCountByServer().replace(plugin.getNexusServer().getThisServiceName(),
                    plugin.getNexusServer().getProfileCountByServer().get(plugin.getNexusServer().getThisServiceName())-1);
            plugin.getDatabaseAdapter().update("profiles", "profileId", profileId,
                    new DatabaseUpdate("nexusLevel", nexusLevel),
                    new DatabaseUpdate("lastActivity", System.currentTimeMillis()),
                    new DatabaseUpdate("souls", souls));
            plugin.getDatabaseAdapter().update("profileStats", "profileId", profileId,
                    new DatabaseUpdate("wonRaids", wonRaids),
                    new DatabaseUpdate("lostRaids", lostRaids));
            saveStorages();
            world = null;
            loading = false;
            Bukkit.getConsoleSender().sendMessage("[Nexus] Profile " + profileId + " unloaded");
        }
    }

    public void saveStorages() {
        for(String storageId : storageBlocks.keySet()) {
            String contents = InventoryManager.toBase64(storageBlocks.get(storageId).getInventory());
            plugin.getDatabaseAdapter().updateTwo("storages", "profileId", profileId,
                    "storageId", storageId,
                    new DatabaseUpdate("inventory", contents));
            storages.replace(storageId, contents);
        }
        storageBlocks.clear();
    }

    public void loadMembers() {
        ResultSet resultSet = plugin.getDatabaseAdapter().getAsync("playerProfiles", "profileId", String.valueOf(profileId));
        List<UUID> tempMembers = new ArrayList<>(members.keySet());
        try {
            while(resultSet.next()) {
                UUID player = UUID.fromString(resultSet.getString("player"));
                long joinedProfile = resultSet.getLong("joinedProfile"),
                        profilePlaytime = resultSet.getLong("playtime");
                String inventory = resultSet.getString("inventory");
                long money = resultSet.getLong("money");
                if(!members.containsKey(player)) {
                    new ProfilePlayer(this, player, profilePlaytime, joinedProfile, inventory, money, plugin);
                } else {
                    ProfilePlayer profilePlayer = members.get(player);
                    profilePlayer.setInventoryBase64(inventory);
                    profilePlayer.setPlaytime(profilePlaytime);
                    tempMembers.remove(player);
                }
            }
            for(UUID member : tempMembers) {
                members.remove(member);
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
                plugin.getNexusServer().getProfileCountByServer().replace(plugin.getNexusServer().getThisServiceName(),
                        plugin.getNexusServer().getProfileCountByServer().get(plugin.getNexusServer().getThisServiceName()) + 1);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getPluginManager().callEvent(new ProfileLoadEvent(Profile.this));
                    }
                }.runTask(plugin);

                ResultSet questsResultSet = plugin.getDatabaseAdapter().getAsync("quests", "profileId", String.valueOf(profileId));

                try {
                    while(questsResultSet.next()) {
                        Task task = Task.valueOf(questsResultSet.getString("task"));
                        long progress = questsResultSet.getLong("progress"),
                        begun = questsResultSet.getLong("begun"),
                        finished = questsResultSet.getLong("finished");
                        quests.put(task, new Quest(profileId, task, progress, begun, finished, plugin));
                    }
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            } else {
                loading = false;
            }
        }
    }

    public void prepare() {
        Bukkit.getConsoleSender().sendMessage("[Nexus] Preparing");
        ResultSet profileResultSet = plugin.getDatabaseAdapter().getAsync("profiles", "profileId", String.valueOf(profileId));

        try {
            while(profileResultSet.next()) {
                owner = UUID.fromString(profileResultSet.getString("owner"));
                nexusLevel = profileResultSet.getInt("nexusLevel");
                start = profileResultSet.getLong("start");
                lastActivity = profileResultSet.getLong("lastActivity");
                souls = profileResultSet.getInt("souls");
            }
            plugin.getProfileManager().getProfilesMap().put(profileId, this);
            loadMembers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ResultSet statsResultSet = plugin.getDatabaseAdapter().getAsync("profileStats", "profileId", String.valueOf(profileId));

        try {
            if(statsResultSet.next()) {
                lostRaids = statsResultSet.getInt("lostRaids");
                wonRaids = statsResultSet.getInt("wonRaids");
            } else {
                lostRaids = 0;
                wonRaids = 0;
                plugin.getDatabaseAdapter().setAsync("profileStats", profileId, 0, 0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void create(UUID owner, int profileSlot) {
        plugin.getDatabaseAdapter().set("profiles", profileId, owner, 0, System.currentTimeMillis(), System.currentTimeMillis(), 0);
        plugin.getDatabaseAdapter().set("profileStats", profileId, 0, 0);
        plugin.getDatabaseAdapter().set("playerProfiles", owner, profileId, profileSlot,
                System.currentTimeMillis(), 0, "empty", 0);
        plugin.getDatabaseAdapter().set("playerStats", owner, profileId, 0, 0);

        Location realNexusLocation = plugin.getLocationManager().getLocation("nexus", Bukkit.getWorlds().get(0)).subtract(0, 1, 0);
        String nexusLocation = realNexusLocation.getBlockX() + ", " + realNexusLocation.getBlockY() + ", " + realNexusLocation.getBlockZ();
        plugin.getDatabaseAdapter().set("schematics", profileId, UUID.randomUUID(), "NEXUS", 0, 0, nexusLocation, System.currentTimeMillis(), 0);

        Location realWorkshopLocation = plugin.getLocationManager().getLocation("workshop", Bukkit.getWorlds().get(0)).subtract(0, 1, 0);
        String workshopLocation = realWorkshopLocation.getBlockX() + ", " + realWorkshopLocation.getBlockY() + ", " + realWorkshopLocation.getBlockZ();
        plugin.getDatabaseAdapter().set("schematics", profileId, UUID.randomUUID(), "WORKSHOP", 0, 180, workshopLocation, System.currentTimeMillis(), 50);
        quests.put(Task.TALK_TO_GEORGE, new Quest(profileId, Task.TALK_TO_GEORGE, plugin).assign());
        this.owner = owner;
    }

    public void addPlayer(int profileSlot, UUID player) {
        plugin.getDatabaseAdapter().set("playerProfiles", player, profileId, profileSlot,
                System.currentTimeMillis(), 0, "empty", 0);
        plugin.getDatabaseAdapter().set("playerStats", player, profileId, 0, 0);
        for(ICloudService service : NexusPlugin.getInstance().getNexusServer().getNexusServers()) {
            new PacketReloadProfileMembers("123", profileId).send(service);
        }
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

    public Map<Task, Quest> getUnfinishedQuests() {
        Map<Task, Quest> unfinishedQuests = new HashMap<>();

        for(Quest quest : quests.values()) {
            if(!quest.isFinished()) {
                unfinishedQuests.put(quest.getTask(), quest);
            }
        }

        return unfinishedQuests;
    }

    public Quest getMainQuest() {
        List<Quest> importantQuests = new ArrayList<>();
        for(Quest quest : getUnfinishedQuests().values()) {
            if(quest.getTask().isChronological()) {
                importantQuests.add(quest);
            }
        }

        if(importantQuests.size() == 0) {
            importantQuests.addAll(getUnfinishedQuests().values());
            if(importantQuests.size() == 0) {
                return null;
            }
        }


        Quest oldestQuest = null;
        for(Quest quest : importantQuests) {
            if(oldestQuest == null || quest.getBegun() <= oldestQuest.getBegun()) {
                oldestQuest = quest;
            }
        }

        return oldestQuest;
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

    public void setNexusLevel(int nexusLevel) {
        this.nexusLevel = nexusLevel;
        if(nexusLevel == 5) {
            if(!quests.containsKey(Task.BUILD_PORTAL)) {
                Quest quest = new Quest(profileId, Task.BUILD_PORTAL, plugin);
                quest.assign();
                quests.put(Task.BUILD_PORTAL, quest);
            }
        }
    }
}
