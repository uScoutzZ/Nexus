package de.uscoutz.nexus.wave;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.wave.commands.SpawnEntityCommand;
import de.uscoutz.nexus.wave.listener.profile.ProfileLoadListener;
import de.uscoutz.nexus.wave.profile.RaidManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusWavePlugin extends JavaPlugin {

    @Getter
    private static NexusWavePlugin instance;

    @Getter
    private NexusSchematicPlugin schematicPlugin;
    @Getter
    private NexusPlugin nexusPlugin;
    @Getter
    private RaidManager raidManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        nexusPlugin = NexusPlugin.getInstance();
        schematicPlugin = NexusSchematicPlugin.getInstance();
        raidManager = new RaidManager(this);

        Bukkit.getPluginManager().registerEvents(new ProfileLoadListener(this), this);
        getCommand("spawnentity").setExecutor(new SpawnEntityCommand(this));
        Bukkit.getConsoleSender().sendMessage("[NexusWave] Enabled");
    }
}
