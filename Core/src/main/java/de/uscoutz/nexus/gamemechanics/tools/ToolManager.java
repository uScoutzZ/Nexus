package de.uscoutz.nexus.gamemechanics.tools;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.utilities.InventoryManager;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ToolManager {

    private NexusPlugin plugin;

    @Getter
    private Map<String, Tool> toolMap;
    @Getter
    private Map<Material, Integer> blockResistance;
    @Getter
    private Map<Material, Material> blockDrop;
    @Getter
    private File toolsFile, resistanceFile, blockdropsFile;
    @Getter
    private FileConfiguration toolsConfig, resistanceConfig, blockdropsConfig;

    public ToolManager(NexusPlugin plugin, File toolsFile, File resistanceFile, File blockdropsFile) {
        this.plugin = plugin;
        toolMap = new HashMap<>();
        blockResistance = new HashMap<>();
        blockDrop = new HashMap<>();
        this.toolsFile = toolsFile;
        this.resistanceFile = resistanceFile;
        if(!toolsFile.exists()) {
            try {
                toolsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(!resistanceFile.exists()) {
            try {
                resistanceFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(!blockdropsFile.exists()) {
            try {
                blockdropsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        toolsConfig = YamlConfiguration.loadConfiguration(toolsFile);
        resistanceConfig = YamlConfiguration.loadConfiguration(resistanceFile);
        blockdropsConfig = YamlConfiguration.loadConfiguration(blockdropsFile);
    }

    public void loadTools() {
        for(String key : toolsConfig.getKeys(false)) {
            Material material = Material.getMaterial(toolsConfig.getString(key + ".material"));

            Tool tool = new Tool(key, ItemBuilder.create(material), plugin);
            tool.breakingPower(toolsConfig.getInt(key + ".breakingPower"));
            if(toolsConfig.getString(key + ".locale") != null) {
                tool.name(toolsConfig.getString(key + ".locale"));
            }
            String ingredients = toolsConfig.getString(key+ ".ingredients");
            tool.setIngredients(InventoryManager.getNeededItemsFromString(ingredients));

            tool.build();
            toolMap.put(key, tool);
        }
    }

    public void loadBlockResistances() {
        for(String key : resistanceConfig.getKeys(false)) {
            Material material = Material.getMaterial(key);
            int resistance = resistanceConfig.getInt(key);
            blockResistance.put(material, resistance);
        }
    }

    public void loadBlockDrops() {
        for(String key : blockdropsConfig.getKeys(false)) {
            Material material = Material.getMaterial(key);
            Material drop = Material.getMaterial(blockdropsConfig.getString(key));
            blockDrop.put(material, drop);
        }
    }

    public String getKey(ItemMeta itemMeta) {
        return itemMeta.getPersistentDataContainer().get(new NamespacedKey(plugin.getName().toLowerCase(), "key"), PersistentDataType.STRING);
    }

    public int getBreakingPower(ItemMeta itemMeta) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin.getName().toLowerCase(), "breakingpower");
        return itemMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
    }

    public boolean isTool(ItemMeta itemMeta) {
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin.getName().toLowerCase(), "breakingpower");
        return dataContainer.has(namespacedKey);
    }
}
