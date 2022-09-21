package de.uscoutz.nexus;

import de.uscoutz.nexus.biomes.BiomeManager;
import de.uscoutz.nexus.commands.*;
import de.uscoutz.nexus.database.DatabaseAdapter;
import de.uscoutz.nexus.gamemechanics.tools.ToolManager;
import de.uscoutz.nexus.inventory.InventoryListener;
import de.uscoutz.nexus.listeners.block.BlockBreakListener;
import de.uscoutz.nexus.listeners.entity.EntityDamageByEntityListener;
import de.uscoutz.nexus.listeners.inventory.InvenvtoryOpenListener;
import de.uscoutz.nexus.listeners.player.*;
import de.uscoutz.nexus.locations.LocationManager;
import de.uscoutz.nexus.networking.NetworkServer;
import de.uscoutz.nexus.networking.NexusServer;
import de.uscoutz.nexus.player.PlayerManager;
import de.uscoutz.nexus.profile.ProfileManager;
import de.uscoutz.nexus.regions.RegionManager;
import de.uscoutz.nexus.utilities.InventoryManager;
import de.uscoutz.nexus.utilities.LocaleManager;
import de.uscoutz.nexus.worlds.WorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
        nexusServer.updatePlayersOnServer();
        localeManager = new LocaleManager(this);
        localeManager.assignFiles(new File("/home/networksync/nexus/languages"));
        locationManager = new LocationManager(this, new File("/home/networksync/nexus/locations.yml"));
        toolManager = new ToolManager(this, new File("/home/networksync/nexus/tools.yml"),
                new File("/home/networksync/nexus/blockresistance.yml"));
        toolManager.loadTools();
        toolManager.loadBlockResistances();
        regionManager = new RegionManager(this);
        biomeManager = new BiomeManager(this, new File("/home/networksync/nexus/biomes.yml"));
        biomeManager.loadBiomes();
        inventoryManager = new InventoryManager(this);

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
        getCommand("profile").setExecutor(new ProfileCommand(this));
        getCommand("coop").setExecutor(new CoopCommand(this));
        getCommand("stop").setExecutor(new StopCommand(this));
        getCommand("deletedata").setExecutor(new DeleteDataCommand(this));
        getCommand("checkplayer").setExecutor(new CheckPlayerCommand(this));
        getCommand("setlocation").setExecutor(new SetLocationCommand(this));
        getCommand("profileunload").setExecutor(new ProfileUnloadCommand(this));

        Bukkit.getConsoleSender().sendMessage("[NexusCore] Enabled");
    }

    @Override
    public void onDisable() {

    }
}
