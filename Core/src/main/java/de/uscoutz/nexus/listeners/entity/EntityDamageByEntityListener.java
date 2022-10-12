package de.uscoutz.nexus.listeners.entity;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {

    private final NexusPlugin plugin;

    public EntityDamageByEntityListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity hit = event.getEntity();

        if(hit instanceof ArmorStand) {
            event.setCancelled(true);
            return;
        }

        if(damager instanceof Player && hit instanceof Player) {
            event.setCancelled(true);
            return;
        }

        if(hit instanceof EnderCrystal || hit instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }
}
