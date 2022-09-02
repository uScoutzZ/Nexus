package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;
import org.bukkit.Location;

import java.util.*;

public class SchematicManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private Map<SchematicType, Map<Condition, Map<Integer, Schematic>>> schematicsMap;
    @Getter
    private Map<UUID, SchematicProfile> schematicProfileMap;


    public SchematicManager(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        schematicsMap = new HashMap<>();
        for(SchematicType schematicType : SchematicType.values()) {
            schematicsMap.put(schematicType, new HashMap<>());
            for(Condition condition : Condition.values()) {
                schematicsMap.get(schematicType).put(condition, new HashMap<>());
            }
        }
        schematicProfileMap = new HashMap<>();
    }

    public void loadSchematics() {
        for(SchematicType schematicType : SchematicType.values()) {
            if(schematicType.getLocation1() != null) {
                for(int i = 0; i < 100; i++) {
                    if(!schematicType.getLocation1().clone().add(0, 0, schematicType.getZDistance()*i).getBlock().getType()
                            .equals(schematicType.getLocation1().getBlock().getType())) {
                        break;
                    } else {
                        for(Condition condition : Condition.values()) {
                            Schematic schematic = new Schematic(schematicType, i, condition, plugin);
                            long timeToFinish = schematic.getSchematicType().getFileConfiguration().getLong("timesToFinish." + i);
                            schematic.setTimeToFinish(timeToFinish);
                        }
                    }
                }
            }
        }
    }
}
