package de.uscoutz.nexus.wave.profile;

import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;

public class RaidManager {

    private NexusWavePlugin plugin;

    @Getter
    private Map<UUID, RaidProfile> raidProfileMap;
    @Getter
    private Map<Integer, List<RaidType>> raidTypesByNexuslevel;
    @Getter
    private List<Location> spawnLocations;

    public RaidManager(NexusWavePlugin plugin) {
        this.plugin = plugin;
        raidProfileMap = new HashMap<>();
        raidTypesByNexuslevel = new HashMap<>();
        spawnLocations = new ArrayList<>();
        for(int i = 0; i < plugin.getSchematicPlugin().getSchematicManager().getSchematicsMap().get(SchematicType.NEXUS).size(); i++) {
            raidTypesByNexuslevel.put(i, new ArrayList<>());
        }
    }

    public void loadSpawnLocations(double radius) {
        double PI = 3.141592653589793238462643383279502884197169399375105820974944592307816406286208995;
        double x = 0, y = 0;
        for (int factor = 0; factor <= 150; factor++) {
            float decimal = (float) factor / 150;
            float xp = (float) radius * (float) Math.cos(decimal * 2 * PI);
            float yp = (float) radius * (float) Math.sin(decimal * 2 * PI);
            Location tile = new Location(Bukkit.getWorlds().get(0), x + xp, -51.1, y + yp);
            spawnLocations.add(tile);
        }
    }
}
