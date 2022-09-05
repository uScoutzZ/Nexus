package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;
import org.bukkit.Location;

import java.util.*;

public class SchematicProfile {

    private NexusSchematicPlugin plugin;
    private Profile profile;

    @Getter
    private Map<UUID, BuiltSchematic> builtSchematics;
    @Getter
    private Map<SchematicType, List<Region>> schematics;
    @Getter
    private Map<Region, BuiltSchematic> schematicsByRegion;

    public SchematicProfile(Profile profile, NexusSchematicPlugin plugin) {
        this.profile = profile;
        this.plugin = plugin;
        builtSchematics = new HashMap<>();
        schematicsByRegion = new HashMap<>();
        schematics = new HashMap<>();
        for(SchematicType schematicType : SchematicType.values()) {
            schematics.put(schematicType, new ArrayList<>());
        }
    }
}
