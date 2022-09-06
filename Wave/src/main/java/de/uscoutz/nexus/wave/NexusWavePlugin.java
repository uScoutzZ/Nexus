package de.uscoutz.nexus.wave;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.collector.CollectorManager;
import de.uscoutz.nexus.wave.commands.SpawnEntityCommand;
import de.uscoutz.nexus.wave.listener.creature.CreatureSpawnListener;
import de.uscoutz.nexus.wave.listener.entity.EntityDeathListener;
import de.uscoutz.nexus.wave.listener.player.PlayerChangeWorldListener;
import de.uscoutz.nexus.wave.listener.player.PlayerJoinListener;
import de.uscoutz.nexus.wave.listener.profile.ProfileCheckoutListener;
import de.uscoutz.nexus.wave.listener.profile.ProfileLoadListener;
import de.uscoutz.nexus.wave.player.PlayerManager;
import de.uscoutz.nexus.wave.profile.RaidManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class NexusWavePlugin extends JavaPlugin {

    @Getter
    private static NexusWavePlugin instance;

    @Getter
    private NexusSchematicPlugin schematicPlugin;
    @Getter
    private NexusPlugin nexusPlugin;
    @Getter
    private RaidManager raidManager;
    @Getter
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        nexusPlugin = NexusPlugin.getInstance();
        schematicPlugin = NexusSchematicPlugin.getInstance();
        raidManager = new RaidManager(new File("/home/networksync/nexus/raids/"), this);
        raidManager.loadSpawnLocations(128);
        raidManager.loadRaidTypes();
        playerManager = new PlayerManager(this);

        Bukkit.getPluginManager().registerEvents(new ProfileLoadListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfileCheckoutListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CreatureSpawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChangeWorldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getCommand("spawnentity").setExecutor(new SpawnEntityCommand(this));
        Bukkit.getConsoleSender().sendMessage("[NexusWave] Enabled");
    }
}
