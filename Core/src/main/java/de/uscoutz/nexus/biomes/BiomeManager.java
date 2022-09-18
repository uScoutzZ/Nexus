package de.uscoutz.nexus.biomes;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.regions.Region;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BiomeManager {

    @Getter
    private Map<Region, Biome> biomeByRegion;
    private File file;
    private NexusPlugin plugin;
    private FileConfiguration fileConfiguration;

    public BiomeManager(NexusPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        biomeByRegion = new HashMap<>();
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public void loadBiomes() {
        int i = 0;
        while(fileConfiguration.getString(i + ".radius") != null) {
            int x = fileConfiguration.getInt(i + ".x"),
                    z = fileConfiguration.getInt(i + ".z"),
                    radius = fileConfiguration.getInt(i + ".radius");
            String localeKey = fileConfiguration.getString(i + ".localekey");
            Biome biome = new Biome(localeKey, x, z, radius, plugin);
            biomeByRegion.put(biome.getRegion(), biome);
            Bukkit.getConsoleSender().sendMessage("[Nexus] Biome " + i + " added");
            i++;
        }
    }

    public Biome getBiome(Location location) {
        for(Region region : biomeByRegion.keySet()) {
            if(region.getBoundingBox().getMinX() <= location.getX() && region.getBoundingBox().getMaxX() >= location.getX()
                    && region.getBoundingBox().getMinY() <= location.getY() && region.getBoundingBox().getMaxY() >= location.getY()
                    && region.getBoundingBox().getMinZ() <= location.getZ() && region.getBoundingBox().getMaxZ() >= location.getZ()) {
                return biomeByRegion.get(region);
            }
        }

        return null;
    }
}
