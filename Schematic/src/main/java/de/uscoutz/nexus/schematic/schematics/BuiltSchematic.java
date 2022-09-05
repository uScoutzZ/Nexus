package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class BuiltSchematic {

    private NexusSchematicPlugin plugin;

    @Getter @Setter
    private Schematic schematic;
    @Getter
    private final UUID schematicId;
    @Setter
    private double damage;
    private Profile profile;
    private int rotation;
    private Location location;

    public BuiltSchematic(NexusSchematicPlugin plugin, Schematic schematic, double percentDamage, UUID schematicId, Profile profile, int rotation, Location location) {
        this.plugin = plugin;
        this.schematic = schematic;
        this.schematicId = schematicId;
        this.profile = profile;
        this.location = location;
        this.rotation = rotation;
        damage = percentDamage*schematic.getDurability();
    }

    public void damage(double damage) {
        Condition oldCondition = getCondition(getPercentDamage());
        this.damage += damage;
        Condition newCondition = getCondition(getPercentDamage());

        if(oldCondition != newCondition) {
            if(newCondition == Condition.DESTROYED) {
                plugin.getNexusPlugin().getDatabaseAdapter().deleteTwo("collectors", "schematicId", schematicId, "intact", "0");
            }
            Schematic.destroy(profile, schematicId, plugin, false);
            Schematic repaired = plugin.getSchematicManager().getSchematicsMap().get(schematic.getSchematicType()).get(newCondition).get(schematic.getLevel());
            repaired.build(location, rotation, schematicId, this.damage, true);
        }
    }

    public void saveDamage() {
        plugin.getNexusPlugin().getDatabaseAdapter().update("schematics", "schematicId", schematicId, new DatabaseUpdate("damage", damage/schematic.getDurability()));
    }

    public double getDamage() {
        return damage;
    }

    public double getPercentDamage() {
        return damage/schematic.getDurability();
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
