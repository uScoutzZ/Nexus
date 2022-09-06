package de.uscoutz.nexus.schematic.schematicitems;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SchematicItemManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private Map<String, SchematicItem> schematicItemMap;
    private File itemsFile;
    private FileConfiguration itemsConfig;

    @Getter
    private Map<Schematic, SchematicItem> schematicItemBySchematic;

    public SchematicItemManager(NexusSchematicPlugin plugin, File itemsFile) {
        this.plugin = plugin;
        schematicItemMap = new HashMap<>();
        schematicItemBySchematic = new HashMap<>();
        this.itemsFile = itemsFile;
        if(!itemsFile.exists()) {
            try {
                itemsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    public void loadItems() {
        for(String key : itemsConfig.getKeys(false)) {
            Material material = Material.getMaterial(itemsConfig.getString(key + ".material"));
            SchematicType schematicType = SchematicType.valueOf(itemsConfig.getString(key + ".schematicType"));
            int level = itemsConfig.getInt(key + ".level");
            Schematic schematic = plugin.getSchematicManager()
                    .getSchematicsMap().get(schematicType).get(Condition.INTACT).get(level);
            if(schematic == null) {
                Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Couldn't find " + schematicType +" level " + level);
            } else {
                SchematicItem schematicItem = new SchematicItem(key, ItemBuilder.create(material), plugin, schematic);
                if(itemsConfig.getString(key + ".locale") != null) {
                    schematicItem.name(itemsConfig.getString(key + ".locale"));
                }

                schematicItem.build();
                schematicItemMap.put(key, schematicItem);
                schematicItemBySchematic.put(schematic, schematicItem);
            }
        }
    }

    public boolean isSchematicItem(ItemMeta itemMeta) {
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin.getNexusPlugin().getName().toLowerCase(), "schematictype");
        return dataContainer.has(namespacedKey);
    }
}
