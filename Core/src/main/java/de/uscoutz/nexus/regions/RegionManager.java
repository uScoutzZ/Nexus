package de.uscoutz.nexus.regions;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class RegionManager {

    private NexusPlugin plugin;

    public RegionManager(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    public Region getRegion(Location location) {
        for(Region region : plugin.getWorldManager().getWorldProfileMap().get(location.getWorld()).getRegions()) {
            if(region.getBoundingBox().getMinX() <= location.getX() && region.getBoundingBox().getMaxX() >= location.getX()
                    && region.getBoundingBox().getMinY() <= location.getY() && region.getBoundingBox().getMaxY() >= location.getY()
                    && region.getBoundingBox().getMinZ() <= location.getZ() && region.getBoundingBox().getMaxZ() >= location.getZ()) {
                return region;
            }
        }

        return null;
    }
}
