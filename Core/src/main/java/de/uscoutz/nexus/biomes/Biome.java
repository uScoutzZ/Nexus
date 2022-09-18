package de.uscoutz.nexus.biomes;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.regions.Region;
import lombok.Getter;
import org.bukkit.Bukkit;

public class Biome {

    @Getter
    private String localeKey;
    @Getter
    private Region region;
    private int x, z, radius;

    public Biome(String localeKey, int x, int z, int radius, NexusPlugin plugin) {
        this.localeKey = localeKey;
        this.x = x;
        this.z = z;
        this.radius = radius;
        region = new Region(plugin, Bukkit.getWorlds().get(0), x-radius, x+radius, -64, 200, z-radius, z+radius);
    }
}
