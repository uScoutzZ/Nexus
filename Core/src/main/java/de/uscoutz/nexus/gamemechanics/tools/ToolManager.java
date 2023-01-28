package de.uscoutz.nexus.gamemechanics.tools;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.biomes.Biome;
import de.uscoutz.nexus.gamemechanics.Rarity;
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
import java.util.LinkedHashMap;
import java.util.Map;

public class ToolManager {

    private NexusPlugin plugin;

    @Getter
    private LinkedHashMap<String, Tool> toolMap;
    @Getter
    private Map<Material, Material> blockDrop;
    @Getter
    private File toolsDirectory, resistanceFile, blockdropsFile;
    @Getter
    private FileConfiguration resistanceConfig, blockdropsConfig;

    public ToolManager(NexusPlugin plugin, File toolsDirectory, File resistanceFile, File blockdropsFile) {
        this.plugin = plugin;
        toolMap = new LinkedHashMap<>();
        blockDrop = new HashMap<>();
        this.toolsDirectory = toolsDirectory;
        this.resistanceFile = resistanceFile;
        if(!toolsDirectory.exists()) {
            try {
                toolsDirectory.createNewFile();
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
        resistanceConfig = YamlConfiguration.loadConfiguration(resistanceFile);
        blockdropsConfig = YamlConfiguration.loadConfiguration(blockdropsFile);
    }

    public void loadTools(File directory) {
        for(File file : directory.listFiles()) {
            if(file.isDirectory()) {
                loadTools(file);
            } else {
                FileConfiguration toolsConfig = YamlConfiguration.loadConfiguration(file);
                for(String key : toolsConfig.getKeys(false)) {
                    Material material = Material.getMaterial(toolsConfig.getString(key + ".material"));

                    Tool tool = new Tool(key, ItemBuilder.create(material), plugin,
                            file.getAbsolutePath().replace(toolsDirectory.getAbsolutePath(), ""));
                    tool.breakingPower(toolsConfig.getInt(key + ".breakingPower"));
                    if(toolsConfig.getString(key + ".locale") != null) {
                        tool.name(toolsConfig.getString(key + ".locale"));
                    }
                    if (toolsConfig.getString(key + ".rarity") != null) {
                        tool.rarity(Rarity.valueOf(toolsConfig.getString(key + ".rarity")));
                    } else {
                        tool.rarity(Rarity.COMMON);
                    }
                    String ingredients = toolsConfig.getString(key+ ".ingredients");
                    tool.setMoneyPrice(toolsConfig.getInt(key + ".money"));
                    tool.setVotetokensPrice(toolsConfig.getInt(key + ".votetokens"));
                    if(ingredients != null) {
                        tool.setIngredients(InventoryManager.getNeededItemsFromString(ingredients));
                    }

                    tool.build();
                    toolMap.put(key, tool);
                }
            }
        }
    }

    public void loadBlockResistances() {
        for(String key : resistanceConfig.getKeys(true)) {
            if(countDots(key) == 1) {
                Material material = Material.getMaterial(key.split("\\.")[1]);
                int resistance = resistanceConfig.getInt(key);
                for(Biome biome : plugin.getBiomeManager().getBiomeByRegion().values()) {
                    if(biome.getLocaleKey().equals(key.split("\\.")[0])) {
                        biome.getBlockResistance().put(material, resistance);
                        break;
                    }
                }
            }
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

    private int countDots(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }
}
