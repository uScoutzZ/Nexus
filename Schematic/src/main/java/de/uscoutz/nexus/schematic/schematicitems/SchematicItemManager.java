package de.uscoutz.nexus.schematic.schematicitems;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.gamemechanics.Rarity;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.collector.CollectorManager;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.utilities.InventoryManager;
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
import java.util.LinkedHashMap;
import java.util.Map;

public class SchematicItemManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private LinkedHashMap<String, SchematicItem> schematicItemMap;
    private File itemsFile;
    private FileConfiguration itemsConfig;

    @Getter
    private Map<Schematic, SchematicItem> schematicItemBySchematic;

    public SchematicItemManager(NexusSchematicPlugin plugin, File itemsFile) {
        this.plugin = plugin;
        schematicItemMap = new LinkedHashMap<>();
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

            try {
                SchematicType schematicType = SchematicType.valueOf(itemsConfig.getString(key + ".schematicType"));
                int level = itemsConfig.getInt(key + ".level");
                int requiredLevel;

                boolean obtainable = itemsConfig.getBoolean(key + ".obtainable");
                Schematic schematic = plugin.getSchematicManager()
                        .getSchematicsMap().get(schematicType).get(Condition.INTACT).get(level);
                if(schematic == null) {
                    Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Couldn't find " + schematicType +" level " + level);
                } else {
                    SchematicItem schematicItem = new SchematicItem(key, ItemBuilder.create(material), plugin, schematic, obtainable, "none");
                    if(itemsConfig.getString(key + ".locale") != null) {
                        schematicItem.name(itemsConfig.getString(key + ".locale"));
                    }
                    if (itemsConfig.getString(key + ".rarity") != null) {
                        schematicItem.rarity(Rarity.valueOf(itemsConfig.getString(key + ".rarity")));
                    } else {
                        schematicItem.rarity(Rarity.COMMON);
                    }
                    if(obtainable) {
                        String ingredients = itemsConfig.getString(key+ ".ingredients");
                        schematicItem.setMoneyPrice(itemsConfig.getInt(key + ".money"));
                        schematicItem.setVotetokensPrice(itemsConfig.getInt(key + ".votetokens"));
                        if(ingredients != null) {
                            schematicItem.setIngredients(InventoryManager.getNeededItemsFromString(ingredients));
                        }
                        schematicItem.setIngredients(InventoryManager.getNeededItemsFromString(ingredients));
                        String taskName = itemsConfig.getString(key+ ".quest");
                        if(taskName != null) {
                            try {
                                schematicItem.setTask(Task.valueOf(taskName));
                            } catch (Exception ignored) {
                            }
                        }
                        if(itemsConfig.getString(key + ".maxObtainable") != null) {
                            schematicItem.setMaxObtainable(itemsConfig.getInt(key + ".maxObtainable"));
                        } else {
                            schematicItem.setMaxObtainable(100);
                        }
                        if(itemsConfig.getString(key + ".requiredLevel") != null) {
                            schematicItem.setRequiredLevel(itemsConfig.getInt(key + ".requiredLevel"));
                        } else {
                            schematicItem.setRequiredLevel(0);
                        }
                    }

                    schematicItem.build();
                    schematicItemMap.put(key, schematicItem);
                    schematicItemBySchematic.put(schematic, schematicItem);
                }
            } catch (Exception exception) {
                Bukkit.getLogger().warning("SchematicItem " + key + " has an invalid material!");
            }
        }
    }

    public boolean isSchematicItem(ItemMeta itemMeta) {
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin.getNexusPlugin().getName().toLowerCase(), "schematictype");
        return dataContainer.has(namespacedKey);
    }
}
