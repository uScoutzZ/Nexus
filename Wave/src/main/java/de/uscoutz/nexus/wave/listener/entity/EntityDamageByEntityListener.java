package de.uscoutz.nexus.wave.listener.entity;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.profile.ProfilePlayer;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {

    private NexusWavePlugin plugin;

    public EntityDamageByEntityListener(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        LivingEntity damaged = (LivingEntity) event.getEntity();
        Entity damager = event.getDamager();

        if(event.getDamage() >= damaged.getHealth()) {
            if(damager instanceof Player player) {
                Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(damaged.getWorld());
                ProfilePlayer profilePlayer = profile.getMembers().get(player.getUniqueId());
                profilePlayer.addKill();
            }
        }
    }
}
