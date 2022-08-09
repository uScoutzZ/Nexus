package de.uscoutz.nexus;

import de.uscoutz.nexus.database.DatabaseAdapter;
import de.uscoutz.nexus.listeners.PlayerJoinListener;
import de.uscoutz.nexus.player.PlayerManager;
import de.uscoutz.nexus.profile.ProfileManager;
import de.uscoutz.nexus.worlds.WorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;

public class NexusPlugin extends JavaPlugin {

    @Getter
    private ProfileManager profileManager;
    @Getter
    private PlayerManager playerManager;
    @Getter
    private WorldManager worldManager;
    @Getter
    private DatabaseAdapter databaseAdapter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        playerManager = new PlayerManager(this);
        profileManager = new ProfileManager(this);
        worldManager = new WorldManager(this);
        databaseAdapter = new DatabaseAdapter(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), this);

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        Bukkit.getConsoleSender().sendMessage("[Nexus] Enabled");
    }
}
