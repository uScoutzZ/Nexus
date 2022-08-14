package de.uscoutz.nexus.schematic;

import de.uscoutz.nexus.schematic.commands.CreateSchematicCommand;
import de.uscoutz.nexus.schematic.commands.SchematicWand;
import de.uscoutz.nexus.schematic.files.FileManager;
import de.uscoutz.nexus.schematic.listener.PlayerInteractListener;
import de.uscoutz.nexus.schematic.listener.PlayerJoinListener;
import de.uscoutz.nexus.schematic.player.SchematicPlayerManager;
import de.uscoutz.nexus.schematic.schematics.SchematicManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusSchematicPlugin extends JavaPlugin {

    @Getter
    private static NexusSchematicPlugin instance;

    @Getter
    private SchematicPlayerManager playerManager;
    @Getter
    private FileManager fileManager;
    @Getter
    private SchematicManager schematicManager;

    @Override
    public void onEnable() {
        instance = this;
        playerManager = new SchematicPlayerManager(this);
        fileManager = new FileManager(this);
        schematicManager = new SchematicManager(this);
        fileManager.loadSchematicFiles();
        getCommand("schematicwand").setExecutor(new SchematicWand(this));
        getCommand("createschematic").setExecutor(new CreateSchematicCommand(this));
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Enabled");
    }
}
