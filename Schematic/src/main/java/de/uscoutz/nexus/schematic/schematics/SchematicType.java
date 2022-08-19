package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public enum SchematicType {

    NEXUS,
    TOWER,
    WALL,
    WORKSHOP,
    HOME,
    PORTAL;

    @Getter
    private int zDistance;
    @Getter @Setter
    private Location location1, location2;
    @Getter
    private FileConfiguration fileConfiguration;

    public void loadFile() {
        File file = NexusSchematicPlugin.getInstance().getFileManager().getSchematicFilesMap().get(this);
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if(fileConfiguration.getString("corner1.world") != null) {
            zDistance = fileConfiguration.getInt("zDistance");
            for(int i = 0; i < 2; i++) {
                Location location = new Location(
                        Bukkit.getWorld(fileConfiguration.getString("corner" + i + ".world")),
                        fileConfiguration.getDouble("corner" + i + ".x"),
                        fileConfiguration.getDouble("corner" + i + ".y"),
                        fileConfiguration.getDouble("corner" + i + ".z"));
                if (i == 0) {
                    location1 = location;
                } else {
                    location2 = location;
                }
            }
        }
    }
}
