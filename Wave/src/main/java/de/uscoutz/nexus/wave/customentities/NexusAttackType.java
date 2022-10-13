package de.uscoutz.nexus.wave.customentities;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import lombok.Getter;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.function.BiConsumer;

public enum NexusAttackType {

    NORMAL((entity, builtSchematic, damage) -> {
        World world = entity.getWorld();
        /*UUID profileId = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(world).getProfileId();
        SchematicProfile profile = plugin.getSchematicPlugin().getSchematicManager().getSchematicProfileMap().get(profileId);
        Region region = plugin.getSchematicPlugin().getNexusPlugin().getRegionManager().getRegion(this.getBukkitEntity().getLocation());
        BuiltSchematic builtSchematic = profile.getSchematicsByRegion().get(region);
        */
        builtSchematic.damage(damage);
        Block block = ((LivingEntity)entity).getTargetBlock(3);
        if(block != null) {
            world.spawnParticle(Particle.BLOCK_CRACK, block.getLocation(), 35, 1, 1, 1, block.getBlockData());
        }
    }),
    SHOOT((entity, builtSchematic, damage) -> {
        NexusWavePlugin plugin = NexusWavePlugin.getInstance();
        World world = entity.getWorld();
        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(world);
        /*
        SchematicProfile profile = plugin.getSchematicPlugin().getSchematicManager().getSchematicProfileMap().get(profileId);
        Region region = plugin.getSchematicPlugin().getNexusPlugin().getRegionManager().getRegion(this.getBukkitEntity().getLocation());
        BuiltSchematic builtSchematic = profile.getSchematicsByRegion().get(region);
        */
        builtSchematic.damage(damage);
        Location location1 = entity.getLocation().clone();
        location1.add(0, 1, 0);
        Location middleLocation = profile.getWorld().getMiddle();
        if(builtSchematic.getSchematic().getSchematicType() != SchematicType.NEXUS) {
            middleLocation = plugin.getNexusPlugin().getRegionManager().getRegion(builtSchematic.getLocation()).getBoundingBox().getCenter().toLocation(world);
            middleLocation.setY(-50);
        }
        Vector vector = middleLocation.toVector().subtract(location1.toVector());
        world.spawnArrow(location1, vector, (float) 3, (float) 0);
        /*Block block = ((LivingEntity)this.getBukkitEntity()).getTargetBlock(3);
        if(block != null) {
            world.spawnParticle(Particle.BLOCK_CRACK, block.getLocation(), 35, 1, 1, 1, block.getBlockData());
        }*/
    });

    @Getter
    private TriConsumer<Entity, BuiltSchematic, Double> attack;

    NexusAttackType(TriConsumer<Entity, BuiltSchematic, Double> attack) {
        this.attack = attack;
    }
}
