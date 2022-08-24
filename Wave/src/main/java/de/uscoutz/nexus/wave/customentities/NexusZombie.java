package de.uscoutz.nexus.wave.customentities;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.customentities.goals.MoveToNexusGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

public class NexusZombie extends Zombie {

    private NexusWavePlugin plugin;

    public NexusZombie(Location loc, NexusWavePlugin plugin) {
        super(EntityType.ZOMBIE, ((CraftWorld) loc.getWorld()).getHandle());
        this.plugin  = plugin;

        this.setPos(loc.getX(), loc.getY(), loc.getZ());

        this.setCanPickUpLoot(false); // Can Pick up Loot
        this.setAggressive(true); // Aggressive
        this.setCustomNameVisible(true); // Custom Name Visible
        this.setCustomName(Component.literal("testZombie")); // Custom Name
        this.setPersistenceRequired(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                setAggressive(!isAggressive());
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(1, new MoveToNexusGoal(this));
    }

    private void setNewGoal(World world) {
        Profile profile = NexusSchematicPlugin.getInstance().getNexusPlugin().getWorldManager().getWorldProfileMap().get(world);
        SchematicProfile schematicProfile = NexusSchematicPlugin.getInstance().getSchematicManager().getSchematicProfileMap()
                .get(profile.getProfileId());

        //Location moveTo = schematicProfile.getSchematics().get(SchematicType.NEXUS).get(0).getBoundingBox().getCenter().toLocation(world);


    }
}
