package de.uscoutz.nexus.schematic.schematics;

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
    private Condition condition;

    public BuiltSchematic(NexusSchematicPlugin plugin, Schematic schematic, int damage, UUID schematicId) {
        this.plugin = plugin;
        this.schematic = schematic;
        this.schematicId = schematicId;
        if(damage == 0) {
            condition = Condition.INTACT;
        } else if(damage >= 50) {
            condition = Condition.DAMAGED;
        } else {
            condition = Condition.DESTROYED;
        }
    }
}
