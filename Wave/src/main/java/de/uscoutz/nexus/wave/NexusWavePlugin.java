package de.uscoutz.nexus.wave;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.wave.commands.CancelRaidCommand;
import de.uscoutz.nexus.wave.commands.TriggerRaidCommand;
import de.uscoutz.nexus.wave.listener.creature.CreatureSpawnListener;
import de.uscoutz.nexus.wave.listener.entity.EntityDamageByEntityListener;
import de.uscoutz.nexus.wave.listener.entity.EntityDamageListener;
import de.uscoutz.nexus.wave.listener.entity.EntityDeathListener;
import de.uscoutz.nexus.wave.listener.player.PlayerChangeWorldListener;
import de.uscoutz.nexus.wave.listener.player.PlayerDeathListener;
import de.uscoutz.nexus.wave.listener.player.PlayerJoinListener;
import de.uscoutz.nexus.wave.listener.player.PlayerRespawnListener;
import de.uscoutz.nexus.wave.listener.profile.ProfileCheckoutListener;
import de.uscoutz.nexus.wave.listener.profile.ProfileLoadListener;
import de.uscoutz.nexus.wave.listener.schematic.SchematicDamagedListener;
import de.uscoutz.nexus.wave.listener.schematic.SchematicUpdateListener;
import de.uscoutz.nexus.wave.player.PlayerManager;
import de.uscoutz.nexus.wave.raids.RaidManager;
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
        raidManager = new RaidManager(new File("/home/networksync/nexusdev/raids/"), this);
        raidManager.loadSpawnLocations(nexusPlugin.getConfig().getInt("base-radius"));
        raidManager.loadRaidTypes();
        playerManager = new PlayerManager(this);

        Bukkit.getPluginManager().registerEvents(new ProfileLoadListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfileCheckoutListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CreatureSpawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChangeWorldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SchematicUpdateListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SchematicDamagedListener(this), this);
        getCommand("cancelraid").setExecutor(new CancelRaidCommand(this));
        getCommand("triggerraid").setExecutor(new TriggerRaidCommand(this));
        Bukkit.getConsoleSender().sendMessage("[NexusWave] Enabled");
    }
}
