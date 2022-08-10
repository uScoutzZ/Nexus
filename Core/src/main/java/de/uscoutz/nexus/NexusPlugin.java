package de.uscoutz.nexus;

import de.uscoutz.nexus.commands.CoopCommand;
import de.uscoutz.nexus.commands.ProfileCommand;
import de.uscoutz.nexus.commands.StopCommand;
import de.uscoutz.nexus.database.DatabaseAdapter;
import de.uscoutz.nexus.inventory.InventoryListener;
import de.uscoutz.nexus.listeners.player.PlayerJoinListener;
import de.uscoutz.nexus.listeners.player.PlayerLoginListener;
import de.uscoutz.nexus.listeners.player.PlayerQuitListener;
import de.uscoutz.nexus.listeners.player.PlayerSpawnLocationListener;
import de.uscoutz.nexus.localization.Message;
import de.uscoutz.nexus.networking.NetworkServer;
import de.uscoutz.nexus.networking.NexusServer;
import de.uscoutz.nexus.player.PlayerManager;
import de.uscoutz.nexus.profile.ProfileManager;
import de.uscoutz.nexus.worlds.WorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
    private Message message;

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
        message = new Message(this);

        networkServer = new NetworkServer(Bukkit.getPort() + 70, this);
        networkServer.start();

        Bukkit.getPluginManager().registerEvents(new PlayerSpawnLocationListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        getCommand("profile").setExecutor(new ProfileCommand(this));
        getCommand("coop").setExecutor(new CoopCommand(this));
        getCommand("stop").setExecutor(new StopCommand(this));

        Bukkit.getConsoleSender().sendMessage("[NexusCore] Enabled");
    }

    @Override
    public void onDisable() {

    }
}
