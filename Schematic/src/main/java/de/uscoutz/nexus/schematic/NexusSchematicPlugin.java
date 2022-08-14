package de.uscoutz.nexus.schematic;

import de.uscoutz.nexus.schematic.commands.CreateSchematicCommand;
import de.uscoutz.nexus.schematic.commands.LoadSchematicCommand;
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

    @Getter
    private final String NO_PERMISSION = "§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.";

    @Override
    public void onEnable() {
        instance = this;
        playerManager = new SchematicPlayerManager(this);
        fileManager = new FileManager(this);
        fileManager.loadSchematicFiles();
        schematicManager = new SchematicManager(this);
        schematicManager.loadSchematics();
        getCommand("schematicwand").setExecutor(new SchematicWand(this));
        getCommand("createschematic").setExecutor(new CreateSchematicCommand(this));
        getCommand("loadschematic").setExecutor(new LoadSchematicCommand(this));
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Enabled");
    }
}
