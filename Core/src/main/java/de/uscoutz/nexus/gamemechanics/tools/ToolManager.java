package de.uscoutz.nexus.gamemechanics.tools;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.item.ItemBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ToolManager {

    private NexusPlugin plugin;

    @Getter
    private Map<String, Tool> toolMap;
    @Getter
    private File toolsFile;
    @Getter
    private FileConfiguration fileConfiguration;

    public ToolManager(NexusPlugin plugin, File toolsFile) {
        this.plugin = plugin;
        toolMap = new HashMap<>();
        this.toolsFile = toolsFile;
        if(!toolsFile.exists()) {
            try {
                toolsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(toolsFile);
    }

    public void loadTools() {
        for(String key : fileConfiguration.getKeys(false)) {
            Bukkit.getConsoleSender().sendMessage("[Nexus] " + key + " tool");
            Material material = Material.getMaterial(fileConfiguration.getString(key + ".material"));

            new Tool(key, ItemBuilder.create(material), plugin)
                    .breakingPower(fileConfiguration.getInt(key + ".breakingPower"))
                    .build();
        }
    }
}
