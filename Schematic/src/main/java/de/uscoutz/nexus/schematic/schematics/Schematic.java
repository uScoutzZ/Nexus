package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class Schematic {

    private NexusSchematicPlugin plugin;

    @Getter
    private SchematicType schematicType;
    @Getter
    private Map<Integer, Block> blocks;
    @Getter
    private int level, substractX, substractY, substractZ;
    @Getter
    private Location corner1, corner2;


    public Schematic(SchematicType schematicType, int level, NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        this.schematicType = schematicType;
        this.level = level;
        blocks = new HashMap<>();

        corner1 = schematicType.getLocation1().clone().add(0, 0, schematicType.getZDistance()*level);
        corner2 = schematicType.getLocation2().clone().add(0, 0, schematicType.getZDistance()*level);

        int minX, minY, minZ, maxX, maxY, maxZ;
        minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        substractX = minX;
        substractY = minY;
        substractZ = minZ;
        for(int i = minX; i <= maxX; i++) {
            for(int j = minY; j <= maxY; j++) {
                for(int k = minZ; k <= maxZ; k++) {
                    Block block = corner1.getWorld().getBlockAt(i, j, k);
                    blocks.put(blocks.size(), block);
                }
            }
        }

        plugin.getSchematicManager().getSchematicsMap().get(schematicType).put(level, this);
    }

    public void build(Location location, int rotation) {
        for(int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            Location blockLocation = block.getLocation().clone();
            blockLocation.setX(blockLocation.getX()-substractX + location.getX());
            blockLocation.setY(blockLocation.getY()-substractY + location.getY());
            blockLocation.setZ(blockLocation.getZ()-substractZ + location.getZ());

            if(rotation != 0) {
                double x = blockLocation.getX(), z = blockLocation.getZ();
                if(rotation == 90) {
                    blockLocation.setZ(x);
                    blockLocation.setX(-z);
                } else if(rotation == 180) {
                    blockLocation.setZ(z*-1);
                    blockLocation.setX(x*-1);
                } else if(rotation == 270) {
                    blockLocation.setZ(-x);
                    blockLocation.setX(z);
                }
            }

            blockLocation.getBlock().setType(block.getType());
            blockLocation.getBlock().setBlockData(block.getBlockData());
        }
    }
}
