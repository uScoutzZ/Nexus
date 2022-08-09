package de.uscoutz.nexus.schematic;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusSchematicPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Enabled");
    }
}
