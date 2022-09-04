package de.uscoutz.nexus.schematic.collector;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectorManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private Map<Block, Collector> collectors;
    @Getter
    private Map<SchematicType, Map<Condition, Map<Integer, List<ItemStack>>>> collectorNeededMap;
    @Getter
    private File schematicCollectorsFile;

    public CollectorManager(File schematicCollectorsFile, NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        collectors = new HashMap<>();
        collectorNeededMap = new HashMap<>();
        this.schematicCollectorsFile = schematicCollectorsFile;
        if(!schematicCollectorsFile.exists()) {
            try {
                schematicCollectorsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for(SchematicType schematicType : SchematicType.values()) {
            collectorNeededMap.put(schematicType, new HashMap<>());
        }
    }

    public void loadCollectors() {
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(schematicCollectorsFile);
        for(Condition condition : Condition.values()) {
            for(SchematicType schematicType : plugin.getSchematicManager().getSchematicsMap().keySet()) {
                collectorNeededMap.get(schematicType).put(condition, new HashMap<>());
                for(Schematic schematic : plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(condition).values()) {
                    String needed = fileConfiguration.getString(condition.toString() + "." + schematicType.toString().toLowerCase() + "." + schematic.getLevel());
                    collectorNeededMap.get(schematicType).get(condition).put(schematic.getLevel(), getNeededItemsFromString(needed));
                }
            }
        }
    }

    public List<ItemStack> getNeededItemsFromString(String needed) {
        List<ItemStack> neededItems = new ArrayList<>();

        if(needed != null && !needed.equals("")) {
            for(String stringMaterial : needed.split(", ")) {
                int amount = Integer.parseInt(stringMaterial.split(":")[1]);
                Material material = Material.getMaterial(stringMaterial.split(":")[0]);
                try {
                    neededItems.add(new ItemStack(material, amount));
                } catch (IllegalArgumentException exception) {
                    Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Material " + stringMaterial +" not found");
                }
            }
        }

        return neededItems;
    }
}
