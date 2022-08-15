package de.uscoutz.nexus.locations;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class LocationManager {

    private NexusPlugin plugin;

    @Getter
    private File locationFile;
    @Getter
    private FileConfiguration fileConfiguration;

    public LocationManager(NexusPlugin plugin, File locationFile) {
        this.plugin = plugin;
        this.locationFile = locationFile;
        if(!locationFile.exists()) {
            try {
                locationFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(locationFile);
    }

    public void reloadFile() {
        fileConfiguration = YamlConfiguration.loadConfiguration(locationFile);
    }

    public void saveLocation(String path, Location location) {
        fileConfiguration.set(path + ".world", location.getWorld().getName());
        fileConfiguration.set(path + ".x", location.getX());
        fileConfiguration.set(path + ".y", location.getY());
        fileConfiguration.set(path + ".z", location.getZ());
        fileConfiguration.set(path + ".yaw", location.getYaw());
        fileConfiguration.set(path + ".pitch", location.getPitch());

        try {
            fileConfiguration.save(locationFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Location getLocation(String path, World world) {
        Location location = null;

        if(fileConfiguration.getString(path + ".world") != null) {
            location = new Location(
                    world,
                    fileConfiguration.getDouble(path + ".x"),
                    fileConfiguration.getDouble(path + ".y"),
                    fileConfiguration.getDouble(path + ".z"),
                    (float) fileConfiguration.getDouble(path + ".yaw"),
                    (float) fileConfiguration.getDouble(path + ".pitch"));
        }

        return  location;
    }
}
