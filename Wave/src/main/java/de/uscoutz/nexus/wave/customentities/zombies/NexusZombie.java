package de.uscoutz.nexus.wave.customentities.zombies;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.customentities.NexusAttackType;
import de.uscoutz.nexus.wave.customentities.NexusMob;
import de.uscoutz.nexus.wave.customentities.goals.MoveToNexusGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class NexusZombie extends Zombie implements NexusMob {

    private NexusWavePlugin plugin;
    private double damage;
    private MoveToNexusGoal moveToNexusGoal;

    public NexusZombie(Location loc, NexusWavePlugin plugin, double damage) {
        super(EntityType.ZOMBIE, ((CraftWorld) loc.getWorld()).getHandle());
        this.plugin  = plugin;

        this.setPos(loc.getX(), loc.getY(), loc.getZ());

        this.setCanPickUpLoot(false); // Can Pick up Loot
        this.setAggressive(true); // Aggressive
        /*this.setCustomNameVisible(true); // Custom Name Visible
        this.setCustomName(Component.literal("testZombie")); // Custom Name*/
        this.setPersistenceRequired(true);
        this.damage = damage;

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!isAlive()) {
                    cancel();
                    return;
                }
                setAggressive(!isAggressive());
                if(isAggressive()) {
                    /*if(moveToNexusGoal.isReachedTarget()) {
                        attackSchematic();
                    }*/

                    BuiltSchematic builtSchematic = moveToNexusGoal.getBuiltSchematic();
                    if(builtSchematic != null) {
                        Region schematicRegion = plugin.getSchematicPlugin().getNexusPlugin().getRegionManager().getRegion(builtSchematic.getLocation());
                        if(schematicRegion.getBoundingBox().clone().expand(1, 1, 1, 1, 1, 1)
                                .contains(getBukkitEntity().getLocation().getX(), getBukkitEntity().getLocation().getY(), getBukkitEntity().getLocation().getZ())) {
                            if(BuiltSchematic.getCondition(builtSchematic.getPercentDamage()) != Condition.DESTROYED) {
                                attackSchematic(builtSchematic);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    @Override
    public void registerGoals() {
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1, false));
        moveToNexusGoal = new MoveToNexusGoal(this);
        this.goalSelector.addGoal(1, moveToNexusGoal);
    }

    @Override
    public void attackSchematic(BuiltSchematic builtSchematic) {
        NexusAttackType.NORMAL.getAttack().accept(this.getBukkitEntity(), builtSchematic, damage);
    }
}
