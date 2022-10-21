package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.autominer.AutoMiner;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BuiltSchematic {

    private NexusSchematicPlugin plugin;

    @Getter @Setter
    private Schematic schematic;
    @Getter
    private final UUID schematicId;
    @Setter
    private double hits;
    private Profile profile;
    private int rotation;
    @Getter
    private Location location;
    @Getter @Setter
    private List<Location> blocks;
    @Getter
    private List<Entity> entities;
    @Getter @Setter
    private boolean isBuilt;

    public BuiltSchematic(NexusSchematicPlugin plugin, Schematic schematic, double percentDamage, UUID schematicId, Profile profile, int rotation, Location location, List<Location> blocks, List<Entity> entities, boolean isBuilt) {
        this.plugin = plugin;
        this.schematic = schematic;
        this.schematicId = schematicId;
        this.profile = profile;
        this.location = location;
        this.rotation = rotation;
        hits = (percentDamage/100)*schematic.getDurability();
        this.blocks = blocks;
        this.entities = entities;
        this.isBuilt = isBuilt;
        plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getBuiltSchematics().put(schematicId, this);

        if(schematic.getSchematicType() == SchematicType.AUTOMINER) {
            if(!plugin.getAutoMinerManager().getAutoMinersPerProfile().containsKey(profile.getProfileId())) {
                plugin.getAutoMinerManager().getAutoMinersPerProfile().put(profile.getProfileId(), new HashMap<>());
            }
            plugin.getAutoMinerManager().getAutoMinersPerProfile().get(profile.getProfileId()).put(schematicId, new AutoMiner(this, plugin));
        }
    }

    public void damage(double damage) {
        Condition oldCondition = getCondition(getPercentDamage());
        this.hits += damage;
        Condition newCondition = getCondition(getPercentDamage());

        if(oldCondition != newCondition) {
            if(newCondition == Condition.DESTROYED) {
                plugin.getNexusPlugin().getDatabaseAdapter().deleteTwo("collectors", "schematicId", schematicId, "intact", "0");
            }
            Schematic.destroy(profile, schematicId, plugin, DestroyAnimation.SILENT, schematic.getSchematicType());
            Schematic repaired = plugin.getSchematicManager().getSchematicsMap().get(schematic.getSchematicType()).get(newCondition).get(schematic.getLevel());
            repaired.build(location, rotation, schematicId, getPercentDamage(), true);
        }
    }

    public void saveDamage() {
        plugin.getNexusPlugin().getDatabaseAdapter().update("schematics", "schematicId", schematicId, new DatabaseUpdate("damage", getPercentDamage()));
    }

    public double getHits() {
        return hits;
    }

    public double getPercentDamage() {
        return (hits /schematic.getDurability())*100;
    }

    public Condition getCondition() {
        return getCondition(getPercentDamage());
    }

    public static Condition getCondition(double percentageDamage) {
        if(percentageDamage < 1) {
            return Condition.INTACT;
        } else if(percentageDamage >= 50) {
            return Condition.DESTROYED;
        } else {
            return Condition.DAMAGED;
        }
    }
}
