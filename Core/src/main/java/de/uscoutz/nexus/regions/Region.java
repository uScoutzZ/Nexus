package de.uscoutz.nexus.regions;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class Region {

    private NexusPlugin plugin;
    private int minX, maxX, minY, maxY, minZ, maxZ;

    @Getter
    private BoundingBox boundingBox;

    public Region(NexusPlugin plugin, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.plugin = plugin;
        this.minX = minX;
        this.minZ = minZ;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        boundingBox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        //plugin.getRegionManager().getRegions().add(this);
    }

    public Region(NexusPlugin plugin, World world, int minX, int maxX, int minZ, int maxZ, String s) {
        this.plugin = plugin;
        minX -= 1;
        minZ -= 1;
        maxX += 1;
        maxZ += 1;
        boundingBox = new BoundingBox(minX, -64, minZ, maxX, 200, maxZ);
        plugin.getRegionManager().getRegions().add(this);

        /*ArmorStand min = (ArmorStand) world.spawnEntity(new Location(world, minX, -50, minZ), EntityType.ARMOR_STAND);
        min.setCustomNameVisible(true);
        min.customName(Component.text("min" + s));

        ArmorStand max = (ArmorStand) world.spawnEntity(new Location(world, maxX, -50, maxZ), EntityType.ARMOR_STAND);
        max.setCustomNameVisible(true);
        max.customName(Component.text("max " + s));*/
    }

    public List<Block> getBlocksInRegion(World world) {
        List<Block> blocks = new ArrayList<>();

        for(int y = minY; y <= maxY; y++) {
            for(int x = minX; x <= maxX; x++) {
                for(int z = minZ; z <= maxZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }

        return blocks;
    }

    public boolean overlap(int minX, int maxX, int minZ, int maxZ) {
        return new BoundingBox(minX, 0, minZ, maxX, 200, maxZ).overlaps(boundingBox);
    }
}
