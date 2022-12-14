package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.collector.Collector;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    @Getter
    private Map<String, Integer> boughtItems;
    @Getter
    private Map<Block, Collector> collectors;

    public SchematicProfile(Profile profile, NexusSchematicPlugin plugin) {
        this.profile = profile;
        profile.setConcurrentlyBuilding(0);
        this.plugin = plugin;
        collectors = new HashMap<>();
        builtSchematics = new HashMap<>();
        schematicsByRegion = new HashMap<>();
        schematics = new HashMap<>();
        boughtItems = new HashMap<>();
        for(SchematicType schematicType : SchematicType.values()) {
            schematics.put(schematicType, new ArrayList<>());
        }
        for(String item : plugin.getSchematicItemManager().getSchematicItemMap().keySet()) {
            boughtItems.put(item, 0);
        }

        ResultSet items = plugin.getNexusPlugin().getDatabaseAdapter().getAsync("boughtItems", "profileId", String.valueOf(profile.getProfileId()));
        try {
            while(items.next()) {
                boughtItems.put(items.getString("item"), items.getInt("amount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMaxConcurrentlyBuilding() {
        int maxConcurrentBuildings = plugin.getNexusPlugin().getConfig().getInt("concurrently-building");
        if(schematics.containsKey(SchematicType.HOME) && schematics.get(SchematicType.HOME).size() != 0) {
            Region homeRegion = schematics.get(SchematicType.HOME).get(0);
            if(schematicsByRegion.containsKey(homeRegion)) {
                maxConcurrentBuildings += schematicsByRegion.get(homeRegion).getSchematic().getLevel();
            }
        }
        return maxConcurrentBuildings;
    }
}
