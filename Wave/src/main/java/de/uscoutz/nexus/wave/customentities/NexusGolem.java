package de.uscoutz.nexus.wave.customentities;

import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.customentities.goals.MoveToNexusGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;


public class NexusGolem extends IronGolem implements NexusMob{

    private NexusWavePlugin plugin;
    private double damage;
    private MoveToNexusGoal moveToNexusGoal;

    public NexusGolem(Location loc, NexusWavePlugin plugin, double damage) {
        super(EntityType.IRON_GOLEM, ((CraftWorld) loc.getWorld()).getHandle());

        this.setPos(loc.getX(), loc.getY(), loc.getZ());

        this.setCanPickUpLoot(false); // Can Pick up Loot
        this.setAggressive(true); // Aggressive
        this.setCustomNameVisible(true); // Custom Name Visible
        this.setCustomName(Component.literal("testGolem")); // Custom Name
        this.damage = damage;
        moveToNexusGoal = new MoveToNexusGoal(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!isAlive()) {
                    cancel();
                    return;
                }
                NexusGolem.this.getLevel().broadcastEntityEvent(NexusGolem.this, (byte)4);
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void registerGoals() {
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1, false));
        this.goalSelector.addGoal(1, new MoveToNexusGoal(this));
    }

    @Override
    public void attackSchematic(BuiltSchematic builtSchematic) {

    }
}
