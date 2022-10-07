package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerFlowerPotManipulateListener implements Listener {

    private NexusPlugin plugin;

    public PlayerFlowerPotManipulateListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFlowerPotManipulate(PlayerFlowerPotManipulateEvent event) {
        event.setCancelled(true);
    }
}
