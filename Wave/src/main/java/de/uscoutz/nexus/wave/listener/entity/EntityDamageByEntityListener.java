package de.uscoutz.nexus.wave.listener.entity;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.profile.ProfilePlayer;
import de.uscoutz.nexus.skills.Skill;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import org.bukkit.entity.Arrow;
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
        if(event.getEntity() instanceof LivingEntity damaged) {
            Entity damager = event.getDamager();

            if(event.getDamage() >= damaged.getHealth()) {
                Player player = null;
                if(damager instanceof Player) {
                    player = (Player) damager;
                } else if(damager instanceof Arrow arrow) {
                    player = (Player) arrow.getShooter();
                }

                if(player != null) {
                    Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(damaged.getWorld());
                    ProfilePlayer profilePlayer = profile.getMembers().get(player.getUniqueId());
                    profilePlayer.addSkillXP(Skill.COMBAT, 4);
                    RaidProfile raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());
                    if(raidProfile.getRaid() != null) {
                        profilePlayer.addKill();
                    }
                }
            }
        }
    }
}
