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

    public BuiltSchematic(NexusSchematicPlugin plugin, Schematic schematic, UUID schematicId) {
        this.plugin = plugin;
        this.schematic = schematic;
        this.schematicId = schematicId;
    }
}
