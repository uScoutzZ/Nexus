package de.uscoutz.nexus.wave.customentities;

import de.uscoutz.nexus.wave.NexusWavePlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;


public class NexusGolem extends IronGolem {

    public NexusGolem(Location loc, NexusWavePlugin plugin) {
        super(EntityType.IRON_GOLEM, ((CraftWorld) loc.getWorld()).getHandle());

        this.setPos(loc.getX(), loc.getY(), loc.getZ());

        this.setCanPickUpLoot(false); // Can Pick up Loot
        this.setAggressive(true); // Aggressive
        this.setCustomNameVisible(true); // Custom Name Visible
        this.setCustomName(Component.literal("testZombie")); // Custom Name
        new BukkitRunnable() {
            @Override
            public void run() {
                NexusGolem.this.getLevel().broadcastEntityEvent(NexusGolem.this, (byte)4);
            }
        }.runTaskTimer(plugin, 20, 20);
    }
}
