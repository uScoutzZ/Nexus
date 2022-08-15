package de.uscoutz.nexus.gamemechanics.tools;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.item.ItemBuilder;
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

public class ToolManager {

    private NexusPlugin plugin;

    @Getter
    private Map<String, Tool> toolMap;
    @Getter
    private Map<Material, Integer> blockResistance;
    @Getter
    private File toolsFile, resistanceFile;
    @Getter
    private FileConfiguration toolsConfig, resistanceConfig;

    public ToolManager(NexusPlugin plugin, File toolsFile, File resistanceFile) {
        this.plugin = plugin;
        toolMap = new HashMap<>();
        blockResistance = new HashMap<>();
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
        toolsConfig = YamlConfiguration.loadConfiguration(toolsFile);
        resistanceConfig = YamlConfiguration.loadConfiguration(resistanceFile);
    }

    public void loadTools() {
        for(String key : toolsConfig.getKeys(false)) {
            Material material = Material.getMaterial(toolsConfig.getString(key + ".material"));

            new Tool(key, ItemBuilder.create(material), plugin)
                    .breakingPower(toolsConfig.getInt(key + ".breakingPower"))
                    .build();
        }
    }

    public void loadBlockResistances() {
        for(String key : resistanceConfig.getKeys(false)) {
            Material material = Material.getMaterial(key);
            int resistance = resistanceConfig.getInt(key);
            blockResistance.put(material, resistance);
        }
    }

    public boolean isTool(ItemMeta itemMeta) {
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin.getName().toLowerCase(), "breakingpower");
        if(dataContainer.has(namespacedKey)) {
            return true;
        } else {
            return false;
        }
    }
}
