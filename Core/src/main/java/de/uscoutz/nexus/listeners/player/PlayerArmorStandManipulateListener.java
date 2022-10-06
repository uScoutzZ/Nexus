package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class PlayerArmorStandManipulateListener implements Listener {

    private NexusPlugin plugin;

    public PlayerArmorStandManipulateListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }
}
