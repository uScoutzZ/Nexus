package de.uscoutz.nexus.listeners.food;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChangeListener implements Listener {

    private NexusPlugin plugin;

    public FoodLevelChangeListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
