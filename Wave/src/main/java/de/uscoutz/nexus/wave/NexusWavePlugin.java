package de.uscoutz.nexus.wave;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.wave.commands.SpawnEntityCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusWavePlugin extends JavaPlugin {

    @Getter
    private NexusSchematicPlugin schematicPlugin;
    @Getter
    private static NexusWavePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        schematicPlugin = NexusSchematicPlugin.getInstance();
        getCommand("spawnentity").setExecutor(new SpawnEntityCommand(this));
        Bukkit.getConsoleSender().sendMessage("[NexusWave] Enabled");
    }
}
