package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class AsyncPrePlayerLoginListener implements Listener {

    private NexusPlugin plugin;

    public AsyncPrePlayerLoginListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerUUID = event.getUniqueId();
        NexusPlayer nexusPlayer = new NexusPlayer(playerUUID, plugin);
        boolean registered = nexusPlayer.registered();

        Bukkit.getConsoleSender().sendMessage("[Nexus] " + registered);

        if(registered && nexusPlayer.getProfilesMap().size() != 0) {
            assert nexusPlayer.getCurrentProfile() != null;
            Bukkit.getConsoleSender().sendMessage("[Nexus] " + nexusPlayer.getCurrentProfile());
            if(!nexusPlayer.setActiveProfile(nexusPlayer.getCurrentProfileSlot(), true)) {
                //event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                //event.kickMessage(Component.text("§cYour join could not be processed. Probably all servers are full. Try again later."));
            }
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    nexusPlayer.setActiveProfile(nexusPlayer.getCurrentProfileSlot(), true);
                }
            }.runTaskLater(plugin, 10);
        }
    }
}
