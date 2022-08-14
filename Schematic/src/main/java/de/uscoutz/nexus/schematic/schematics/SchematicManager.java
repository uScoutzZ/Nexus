package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class SchematicManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private Map<SchematicType, Map<Integer, Schematic>> schematicsMap;

    public SchematicManager(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        schematicsMap = new HashMap<>();
        for(SchematicType schematicType : SchematicType.values()) {
            schematicsMap.put(schematicType, new HashMap<>());
        }
        loadSchematics();
    }

    public void loadSchematics() {
        for(SchematicType schematicType : SchematicType.values()) {
            Location micLocation = new Location(schematicType.getLocation1().getWorld(),
                    Math.min(schematicType.getLocation1().getBlockX(), schematicType.getLocation2().getBlockX()),
                    Math.min(schematicType.getLocation1().getBlockY(), schematicType.getLocation2().getBlockY()),
                    Math.min(schematicType.getLocation1().getBlockZ(), schematicType.getLocation2().getBlockZ()));
            for(int i = 0; micLocation.clone().add(0, 0, schematicType.getZDistance()).getBlock().getType() == micLocation.getBlock().getType(); i++) {
                new Schematic(schematicType, i, plugin);
            }
        }
    }
}
