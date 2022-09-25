package de.uscoutz.nexus.wave.listener.entity;

import de.uscoutz.nexus.wave.NexusWavePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private NexusWavePlugin plugin;

    public EntityDamageListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            if(event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                    && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                event.setCancelled(true);
            }
        }
    }
}
