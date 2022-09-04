package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;

import java.util.UUID;

public class BuiltSchematic {

    private NexusSchematicPlugin plugin;

    @Getter
    private Schematic schematic;
    @Getter
    private UUID schematicId;
    private double damage;

    public BuiltSchematic(NexusSchematicPlugin plugin, Schematic schematic, double percentDamage, UUID schematicId) {
        this.plugin = plugin;
        this.schematic = schematic;
        this.schematicId = schematicId;
        damage = percentDamage*schematic.getDurability();
    }

    public void damage(double damage) {
        this.damage += damage;
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
        if(percentageDamage == 0) {
            return Condition.INTACT;
        } else if(percentageDamage >= 50) {
            return Condition.DESTROYED;
        } else {
            return Condition.DAMAGED;
        }
    }
}
