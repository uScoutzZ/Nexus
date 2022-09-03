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
    @Getter
    private double damage, percentDamage;

    public BuiltSchematic(NexusSchematicPlugin plugin, Schematic schematic, double percentDamage, UUID schematicId) {
        this.plugin = plugin;
        this.schematic = schematic;
        this.schematicId = schematicId;
        this.percentDamage = percentDamage;
        damage = percentDamage*schematic.getDurability();
    }

    public void damage(double damage) {
        this.damage += damage;
    }

    public Condition getCondition() {
        if(damage == 0) {
            return Condition.INTACT;
        } else if(damage >= 50) {
            return Condition.DESTROYED;
        } else {
            return Condition.DAMAGED;
        }
    }

    public void saveDamage() {
        plugin.getNexusPlugin().getDatabaseAdapter().update("schematics", "schematicId", schematicId, new DatabaseUpdate("damage", damage/schematic.getDurability()));
    }
}
