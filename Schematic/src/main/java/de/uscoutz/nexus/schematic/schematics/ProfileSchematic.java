package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;

import java.util.UUID;

public class ProfileSchematic {

    private NexusSchematicPlugin plugin;

    @Getter
    private Schematic schematic;
    @Getter
    private UUID schematicId;
    @Getter
    private long placed;

    public ProfileSchematic(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }
}
