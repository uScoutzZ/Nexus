package de.uscoutz.nexus.broadcasts;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BroadcastManager {

    private NexusPlugin plugin;

    private List<BroadcastMessage> broadcastMessages, messagesToSend;
    private int interval;

    public BroadcastManager(NexusPlugin plugin, File file) {
        this.plugin = plugin;
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        broadcastMessages = new ArrayList<>();
        interval = fileConfiguration.getInt("interval");
        for(String key : fileConfiguration.getString("keys").split("; ")) {
            broadcastMessages.add(new BroadcastMessage(key));
        }
        messagesToSend = new ArrayList<>(broadcastMessages);
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player all : Bukkit.getOnlinePlayers()) {
                    all.sendMessage(" \n" + plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(
                            all.getUniqueId()).getLanguage(), messagesToSend.get(0).getLocaleKey()) + "\n ");
                }
                messagesToSend.remove(0);
                if(messagesToSend.size() == 0) {
                    messagesToSend = new ArrayList<>(broadcastMessages);
                }
            }
        }.runTaskTimer(plugin, interval*20, interval*20);
    }
}
