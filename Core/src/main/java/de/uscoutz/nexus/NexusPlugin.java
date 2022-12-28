package de.uscoutz.nexus;

import com.mojang.authlib.GameProfile;
import de.uscoutz.nexus.biomes.BiomeManager;
import de.uscoutz.nexus.broadcasts.BroadcastManager;
import de.uscoutz.nexus.commands.*;
import de.uscoutz.nexus.database.DatabaseAdapter;
import de.uscoutz.nexus.gamemechanics.tools.ToolManager;
import de.uscoutz.nexus.inventory.InventoryListener;
import de.uscoutz.nexus.listeners.block.BlockBreakListener;
import de.uscoutz.nexus.listeners.entity.EntityDamageByEntityListener;
import de.uscoutz.nexus.listeners.entity.EntityPickupItemListener;
import de.uscoutz.nexus.listeners.food.FoodLevelChangeListener;
import de.uscoutz.nexus.listeners.inventory.InvenvtoryOpenListener;
import de.uscoutz.nexus.listeners.player.*;
import de.uscoutz.nexus.locations.LocationManager;
import de.uscoutz.nexus.networking.NetworkServer;
import de.uscoutz.nexus.networking.NexusServer;
import de.uscoutz.nexus.player.PlayerManager;
import de.uscoutz.nexus.profile.ProfileManager;
import de.uscoutz.nexus.regions.RegionManager;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import de.uscoutz.nexus.utilities.InventoryManager;
import de.uscoutz.nexus.utilities.LocaleManager;
import de.uscoutz.nexus.worlds.WorldManager;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.Executors;

public class NexusPlugin extends JavaPlugin {

    @Getter
    private static NexusPlugin instance;

    @Getter
    private ProfileManager profileManager;
    @Getter
    private PlayerManager playerManager;
    @Getter
    private WorldManager worldManager;
    @Getter
    private DatabaseAdapter databaseAdapter;
    @Getter
    private NexusServer nexusServer;
    @Getter
    private LocaleManager localeManager;
    @Getter
    private LocationManager locationManager;
    @Getter
    private ToolManager toolManager;
    @Getter
    private RegionManager regionManager;
    @Getter
    private BiomeManager biomeManager;
    @Getter
    private InventoryManager inventoryManager;
    @Getter
    private BroadcastManager broadcastManager;

    private NetworkServer networkServer;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        playerManager = new PlayerManager(this);
        profileManager = new ProfileManager(this);
        worldManager = new WorldManager(this);
        databaseAdapter = new DatabaseAdapter(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), this);
        nexusServer = new NexusServer(this);
        //nexusServer.updatePlayersOnServer();
        localeManager = new LocaleManager(this);
        localeManager.assignFiles(new File("/home/networksync/nexus/languages"));
        locationManager = new LocationManager(this, new File("/home/networksync/nexus/locations.yml"));
        biomeManager = new BiomeManager(this, new File("/home/networksync/nexus/biomes.yml"));
        biomeManager.loadBiomes();
        toolManager = new ToolManager(this, new File("/home/networksync/nexus/tools.yml"),
                new File("/home/networksync/nexus/blockresistance.yml"), new File("/home/networksync/nexus/blockdrops.yml"));
        toolManager.loadTools();
        toolManager.loadBlockResistances();
        toolManager.loadBlockDrops();
        regionManager = new RegionManager(this);
        inventoryManager = new InventoryManager(this);
        broadcastManager = new BroadcastManager(this, new File("/home/networksync/nexus/broadcasts.yml"));
        broadcastManager.start();

        networkServer = new NetworkServer(Bukkit.getPort() + 70, this);
        networkServer.start();


        Bukkit.getPluginManager().registerEvents(new AsyncPrePlayerLoginListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSpawnLocationListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractAtEntityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InvenvtoryOpenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FoodLevelChangeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityPickupItemListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerArmorStandManipulateListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerFlowerPotManipulateListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerBucketFillListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerBucketEmptyListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PrepareItemCraftListener(this), this);
        getCommand("profile").setExecutor(new ProfileCommand(this));
        getCommand("coop").setExecutor(new CoopCommand(this));
        getCommand("stop").setExecutor(new StopCommand(this));
        getCommand("deletedata").setExecutor(new DeleteDataCommand(this));
        getCommand("checkplayer").setExecutor(new CheckPlayerCommand(this));
        getCommand("setlocation").setExecutor(new SetLocationCommand(this));
        getCommand("profileunload").setExecutor(new ProfileUnloadCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("addmoney").setExecutor(new AddMoneyCommand(this));
        getCommand("joinprofile").setExecutor(new JoinProfileCommand(this));
        getCommand("loadprofile").setExecutor(new LoadProfileCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("adminmode").setExecutor(new AdminModeCommand(this));

        Bukkit.getConsoleSender().sendMessage("[NexusCore] Enabled");

        new BukkitRunnable() {
            @Override
            public void run() {
                databaseAdapter.getAsync("players", "player", "1a6b3b81-8dea-40b4-9929-cea02016955a");
            }
        }.runTaskTimer(this, 20*60*90, 20*60*90);
    }

    @Override
    public void onDisable() {
        nexusServer.getProfileCountByServer().remove(nexusServer.getThisServiceName());
        /*for(UUID uuid : nexusServer.getProfilesServerMap().keySet()) {
            if(nexusServer.getProfilesServerMap().get(uuid).equals(nexusServer.getThisServiceName())) {
                nexusServer.getProfilesServerMap().remove(uuid);
            }
        }*/
    }
}
