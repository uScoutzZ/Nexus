package de.uscoutz.nexus.schematic;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.schematic.autominer.AutoMinerManager;
import de.uscoutz.nexus.schematic.commands.*;
import de.uscoutz.nexus.schematic.gateways.GatewayManager;
import de.uscoutz.nexus.schematic.listener.block.*;
import de.uscoutz.nexus.schematic.listener.entity.EntityChangeBlockListener;
import de.uscoutz.nexus.schematic.listener.entity.EntityPortalEnterListener;
import de.uscoutz.nexus.schematic.listener.inventory.InventoryOpenListener;
import de.uscoutz.nexus.schematic.listener.schematic.SchematicInventoryOpenedListener;
import de.uscoutz.nexus.schematic.listener.player.*;
import de.uscoutz.nexus.schematic.listener.profile.ProfileCheckoutListener;
import de.uscoutz.nexus.schematic.listener.profile.ProfileLoadListener;
import de.uscoutz.nexus.schematic.listener.schematic.SchematicItemBoughtListener;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItemManager;
import de.uscoutz.nexus.schematic.collector.CollectorManager;
import de.uscoutz.nexus.schematic.files.FileManager;
import de.uscoutz.nexus.schematic.player.SchematicPlayerManager;
import de.uscoutz.nexus.schematic.schematics.SchematicManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class NexusSchematicPlugin extends JavaPlugin {

    @Getter
    private static NexusSchematicPlugin instance;
    @Getter
    private NexusPlugin nexusPlugin;

    @Getter
    private SchematicPlayerManager playerManager;
    @Getter
    private FileManager fileManager;
    @Getter
    private SchematicManager schematicManager;
    @Getter
    private CollectorManager collectorManager;
    @Getter
    private SchematicItemManager schematicItemManager;
    @Getter
    private GatewayManager gatewayManager;
    @Getter
    private AutoMinerManager autoMinerManager;

    @Getter
    private final String NO_PERMISSION = "§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.";

    @Override
    public void onEnable() {
        instance = this;
        nexusPlugin = NexusPlugin.getInstance();
        playerManager = new SchematicPlayerManager(this);
        fileManager = new FileManager(this);
        fileManager.loadSchematicFiles();
        schematicManager = new SchematicManager(this);
        schematicManager.loadSchematics();
        collectorManager = new CollectorManager(new File("/home/networksync/nexus/schematiccollectors.yml"), this);
        collectorManager.loadCollectors();
        schematicItemManager = new SchematicItemManager(this,
                new File("/home/networksync/nexus/schematicitems.yml"));
        schematicItemManager.loadItems();
        gatewayManager = new GatewayManager(new File("/home/networksync/nexus/gateways.yml"), this);
        gatewayManager.loadGateways();
        autoMinerManager = new AutoMinerManager(this);

        getCommand("schematicwand").setExecutor(new SchematicWand(this));
        getCommand("createschematic").setExecutor(new CreateSchematicCommand(this));
        getCommand("loadschematic").setExecutor(new LoadSchematicCommand(this));
        getCommand("getupgradeitems").setExecutor(new GetUpgradeItemsCommand(this));
        getCommand("getschematicitem").setExecutor(new GetSchematicItemCommand(this));
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDropListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerItemHeldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfileLoadListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfileCheckoutListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityChangeBlockListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockPhysicsListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockSpreadListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockGrowListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryOpenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SchematicInventoryOpenedListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityPortalEnterListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SchematicItemBoughtListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractAtEntityListener(this), this);
        Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Enabled");
    }
}
