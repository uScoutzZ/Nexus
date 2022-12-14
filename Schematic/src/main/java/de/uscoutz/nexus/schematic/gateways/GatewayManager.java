package de.uscoutz.nexus.schematic.gateways;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GatewayManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private Map<Integer, Region> gateways;
    @Getter
    private Map<Integer, Location> holograms;
    @Getter
    private FileConfiguration fileConfiguration;
    @Getter
    private File gatewayFile;

    public GatewayManager(File gatewayFile, NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        this.gatewayFile = gatewayFile;
        gateways = new HashMap<>();
        holograms = new HashMap<>();
        if(!gatewayFile.exists()) {
            try {
                gatewayFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadGateways() {
        fileConfiguration = YamlConfiguration.loadConfiguration(gatewayFile);
        int nexusLevels = plugin.getSchematicManager().getSchematicsMap().get(SchematicType.TOWER).get(Condition.INTACT).size();
        for(int i = 0; i < nexusLevels-1; i++) {
            Location location1, location2;
            location1 = plugin.getNexusPlugin().getLocationManager().getLocation(fileConfiguration, i + ".location1", Bukkit.getWorlds().get(0));
            location2 = plugin.getNexusPlugin().getLocationManager().getLocation(fileConfiguration, i + ".location2", Bukkit.getWorlds().get(0));
            Region region = new Region(plugin.getNexusPlugin(), Bukkit.getWorlds().get(0), Math.min(location1.getBlockX(), location2.getBlockX()),
                    Math.max(location1.getBlockX(), location2.getBlockX()),
                    Math.min(location1.getBlockY(), location2.getBlockY()),
                    Math.max(location1.getBlockY(), location2.getBlockY()),
                    Math.min(location1.getBlockZ(), location2.getBlockZ()),
                    Math.max(location1.getBlockZ(), location2.getBlockZ()));
            gateways.put(i, region);
            holograms.put(i, plugin.getNexusPlugin().getLocationManager().getLocation(fileConfiguration, i + ".location3", Bukkit.getWorlds().get(0)));
        }
    }
}
