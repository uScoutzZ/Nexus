package de.uscoutz.nexus.wave.raids;

import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class RaidManager {

    private NexusWavePlugin plugin;

    @Getter
    private File file;
    @Getter
    private Map<UUID, RaidProfile> raidProfileMap;
    @Getter
    private Map<Integer, List<RaidType>> raidTypesByNexuslevel;
    @Getter
    private List<Location> spawnLocations;

    public RaidManager(File file, NexusWavePlugin plugin) {
        this.plugin = plugin;
        if(!file.exists()) {
            file.mkdir();
        }
        this.file = file;
        raidProfileMap = new HashMap<>();
        raidTypesByNexuslevel = new HashMap<>();
        spawnLocations = new ArrayList<>();
        for(int i = 0; i < plugin.getSchematicPlugin().getSchematicManager().getSchematicsMap().get(SchematicType.NEXUS).get(Condition.INTACT).size(); i++) {
            raidTypesByNexuslevel.put(i, new ArrayList<>());
        }
    }

    public void loadSpawnLocations(double radius) {
        double PI = Math.PI;
        Location spawn = plugin.getNexusPlugin().getLocationManager().getLocation("nexus-crystal", Bukkit.getWorlds().get(0));
        double x = spawn.getX(), y = spawn.getZ();
        for (int factor = 0; factor <= 150; factor++) {
            float decimal = (float) factor / 150;
            float xp = (float) radius * (float) Math.cos(decimal * 2 * PI);
            float yp = (float) radius * (float) Math.sin(decimal * 2 * PI);
            Location tile = new Location(spawn.getWorld(), x + xp, -50.1, y + yp);
            spawnLocations.add(tile);
        }

        Bukkit.getConsoleSender().sendMessage("[NexusRaid] Found " + spawnLocations.size() +" locations for spawns");
    }

    public void loadRaidTypes() {
        for(File config : file.listFiles()) {
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(config);
            raidTypesByNexuslevel.get(fileConfiguration.getInt("nexus-level")).add(new RaidType(config.getName(), fileConfiguration, plugin));
        }
    }
}
