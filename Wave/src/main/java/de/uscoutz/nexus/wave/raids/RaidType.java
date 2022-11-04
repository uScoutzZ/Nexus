package de.uscoutz.nexus.wave.raids;

import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.customentities.NexusEntityType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaidType implements Cloneable {

    private NexusWavePlugin plugin;

    @Getter
    private Map<Integer, List<NexusEntityType>> mobsByWave;
    @Getter
    private Map<Integer, Integer> mobsPerWave;
    @Getter
    private String raidTypeId;

    @Override
    public RaidType clone() throws CloneNotSupportedException {
        return (RaidType) super.clone();
    }

    public RaidType(String raidTypeId, FileConfiguration fileConfiguration, NexusWavePlugin plugin) {
        this.plugin = plugin;
        this.raidTypeId = raidTypeId.replace(".yml", "");
        mobsByWave = new HashMap<>();
        mobsPerWave = new HashMap<>();

        for(int i = 1; fileConfiguration.get(i + ".mobs") != null; i++) {
            mobsPerWave.put(i, fileConfiguration.getInt(i + ".mobs"));
            mobsByWave.put(i, new ArrayList<>());
            Bukkit.getConsoleSender().sendMessage("[NexusRaid] Adding " + i + " to " + raidTypeId + " (" + mobsPerWave.get(i) + ")");
            String entities = fileConfiguration.getString(i + ".entities");
            for(String entity : entities.split(", ")) {
                try {
                    NexusEntityType nexusEntityType = NexusEntityType.valueOf(entity);
                    mobsByWave.get(i).add(nexusEntityType);
                } catch (IllegalArgumentException exception) {
                    Bukkit.getConsoleSender().sendMessage("[NexusRaid] Type " + entity +" not found");
                }
            }
        }
    }
}
