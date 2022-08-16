package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.collector.Collector;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Wall;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Schematic {

    private NexusSchematicPlugin plugin;

    @Getter
    private SchematicType schematicType;
    @Getter
    private Map<Integer, Block> blocks;
    @Getter
    private int level, substractX, substractY, substractZ, xLength, zLength;
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
        minX = Math.min(corner1.getBlockX()+1, corner2.getBlockX()+1);
        minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        minZ = Math.min(corner1.getBlockZ()+1, corner2.getBlockZ()+1);
        maxX = Math.max(corner1.getBlockX()-1, corner2.getBlockX()-1);
        maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        maxZ = Math.max(corner1.getBlockZ()-1, corner2.getBlockZ()-1);
        substractX = minX;
        substractY = minY;
        substractZ = minZ;

        xLength = maxX-minX;
        zLength = maxZ-minZ;

        for(int i = minX; i <= maxX; i++) {
            for(int j = minY; j <= maxY; j++) {
                for(int k = minZ; k <= maxZ; k++) {
                    Block block = corner1.getWorld().getBlockAt(i, j, k);
                    if(block.getType() != Material.AIR) {
                        blocks.put(blocks.size(), block);
                    }
                }
            }
        }

        Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Add " + schematicType + " level " + level);
        plugin.getSchematicManager().getSchematicsMap().get(schematicType).put(level, this);
    }

    public void preview(Location location, int rotation) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE
                , maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for(int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            Location blockLocation = block.getLocation().clone();
            blockLocation.setX(blockLocation.getX() - substractX);
            blockLocation.setY(blockLocation.getY() - substractY);
            blockLocation.setZ(blockLocation.getZ() - substractZ);
            blockLocation.setWorld(location.getWorld());

            blockLocation = rotate(blockLocation, rotation);
            blockLocation.add(location.getX(), location.getY(), location.getZ());

            minX = Math.min(minX, blockLocation.getBlockX());
            minY = Math.min(minY, blockLocation.getBlockY());
            minZ = Math.min(minZ, blockLocation.getBlockZ());
            maxX = Math.max(maxX, blockLocation.getBlockX());
            maxY = Math.max(maxY, blockLocation.getBlockY());
            maxZ = Math.max(maxZ, blockLocation.getBlockZ());
        }

        Location point1;
        Location point2;
        Location point3;
        Location point4;

        if(rotation == 90) { //RICHTIG
            point1 = new Location(location.getWorld(), maxX+0.9, minY+1.2, maxZ+0.9);
            point2 = new Location(location.getWorld(), minX, minY+1.2, minZ);
            point3 = new Location(location.getWorld(), minX, minY+1.2, maxZ+0.9);
            point4 = new Location(location.getWorld(), maxX+0.9, minY+1.2, minZ);
        } else if(rotation == 270) {
            point1 = new Location(location.getWorld(), maxX+0.9, minY+1.2, maxZ+0.9);
            point2 = new Location(location.getWorld(), minX, minY+1.2, minZ);
            point3 = new Location(location.getWorld(), minX, minY+1.2, maxZ+0.9);
            point4 = new Location(location.getWorld(), maxX+0.9, minY+1.2, minZ);
        } else if(rotation == 180) {//RICHTIG
            point1 = new Location(location.getWorld(), maxX+0.9, minY+1.2, maxZ+0.9);
            point2 = new Location(location.getWorld(), minX, minY+1.2, minZ);
            point3 = new Location(location.getWorld(), minX, minY+1.2, maxZ+0.9);
            point4 = new Location(location.getWorld(), maxX+0.9, minY+1.2, minZ);
        } else {
            point1 = new Location(location.getWorld(), maxX+0.9, minY+1.2, maxZ+0.9);
            point2 = new Location(location.getWorld(), minX, minY+1.2, minZ);
            point3 = new Location(location.getWorld(), minX, minY+1.2, maxZ+0.9);
            point4 = new Location(location.getWorld(), maxX+0.9, minY+1.2, minZ);
        }

        drawLine(point3, point2, 0.5);
        drawLine(point1, point3, 0.5);
        drawLine(point1, point4, 0.5);
        drawLine(point2, point4, 0.5);
    }

    public void drawLine(Location point1, Location point2, double space) {
        World world = point1.getWorld();
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, p1.getX(), p1.getY(), p1.getZ(), 1, new Particle.DustTransition(Color.GREEN, Color.LIME, 2));
            length += space;
        }
    }

    public void build(Location location, int rotation) {
        for(int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            Location blockLocation = block.getLocation().clone();
            blockLocation.setX(blockLocation.getX()-substractX);
            blockLocation.setY(blockLocation.getY()-substractY);
            blockLocation.setZ(blockLocation.getZ()-substractZ);
            blockLocation.setWorld(location.getWorld());
            blockLocation = rotate(blockLocation, rotation);
            blockLocation.add(location.getX(), location.getY(), location.getZ());

            blockLocation.getBlock().setType(block.getType());
            BlockData blockData = block.getBlockData();

            blockLocation.getBlock().setBlockData(blockData);
            if(block.getState() instanceof Sign) {
                Block pastedBlock = blockLocation.getBlock();
                Sign sign = (Sign) block.getState();
                Sign pastedSign = (Sign) pastedBlock.getState();
                for(int l = 0; l < sign.lines().size(); l++) {
                    pastedSign.line(l, sign.line(l));
                }
                pastedSign.update();
                if(pastedSign.getLine(0).equalsIgnoreCase("[COLLECTOR]")) {
                    new Collector(plugin.getCollectorManager().getCollectorNeededMap().get(schematicType).get(level), plugin).setFilledAction(player1 -> {
                        player1.sendMessage("§6Wer das liest ist blöd");
                    }).spawn(pastedSign.getLocation());

                    blockLocation.getBlock().setType(Material.AIR);
                }
            } else if(block.getBlockData() instanceof MultipleFacing) {
                MultipleFacing multipleFacing = (MultipleFacing) block.getBlockData();
                List<BlockFace> newFaces = new ArrayList<>();
                if (rotation == 180) {
                    if (multipleFacing.hasFace(BlockFace.NORTH)) {
                        newFaces.add(BlockFace.SOUTH);
                    }
                    if (multipleFacing.hasFace(BlockFace.EAST)) {
                        newFaces.add(BlockFace.WEST);
                    }
                    if (multipleFacing.hasFace(BlockFace.SOUTH)) {
                        newFaces.add(BlockFace.NORTH);
                    }
                    if (multipleFacing.hasFace(BlockFace.WEST)) {
                        newFaces.add(BlockFace.EAST);
                    }
                } else if (rotation == 270) {
                    if (multipleFacing.hasFace(BlockFace.NORTH)) {
                        newFaces.add(BlockFace.EAST);
                    }
                    if (multipleFacing.hasFace(BlockFace.EAST)) {
                        newFaces.add(BlockFace.SOUTH);
                    }
                    if (multipleFacing.hasFace(BlockFace.SOUTH)) {
                        newFaces.add(BlockFace.WEST);
                    }
                    if (multipleFacing.hasFace(BlockFace.WEST)) {
                        newFaces.add(BlockFace.NORTH);
                    }
                } else if (rotation == 90) {
                    if (multipleFacing.hasFace(BlockFace.NORTH)) {
                        newFaces.add(BlockFace.WEST);
                    }
                    if (multipleFacing.hasFace(BlockFace.EAST)) {
                        newFaces.add(BlockFace.NORTH);
                    }
                    if (multipleFacing.hasFace(BlockFace.SOUTH)) {
                        newFaces.add(BlockFace.EAST);
                    }
                    if (multipleFacing.hasFace(BlockFace.WEST)) {
                        newFaces.add(BlockFace.SOUTH);
                    }
                }
                if (rotation != 0) {
                    for (BlockFace face : multipleFacing.getFaces()) {
                        multipleFacing.setFace(face, false);
                    }
                    for (BlockFace face : newFaces) {
                        multipleFacing.setFace(face, true);
                    }
                    blockLocation.getBlock().setBlockData(multipleFacing);
                }
            } else if(blockData instanceof Wall) {
                Wall wall = (Wall) blockData;
                Map<BlockFace, Wall.Height> newFaces = new HashMap<>();
                if (rotation == 180) {
                    newFaces.put(BlockFace.SOUTH, wall.getHeight(BlockFace.NORTH));
                    newFaces.put(BlockFace.WEST, wall.getHeight(BlockFace.EAST));
                    newFaces.put(BlockFace.NORTH, wall.getHeight(BlockFace.SOUTH));
                    newFaces.put(BlockFace.EAST, wall.getHeight(BlockFace.WEST));

                } else if (rotation == 270) {
                    newFaces.put(BlockFace.EAST, wall.getHeight(BlockFace.NORTH));
                    newFaces.put(BlockFace.SOUTH, wall.getHeight(BlockFace.EAST));
                    newFaces.put(BlockFace.WEST, wall.getHeight(BlockFace.SOUTH));
                    newFaces.put(BlockFace.NORTH, wall.getHeight(BlockFace.WEST));
                } else if (rotation == 90) {
                    newFaces.put(BlockFace.WEST, wall.getHeight(BlockFace.NORTH));
                    newFaces.put(BlockFace.NORTH, wall.getHeight(BlockFace.EAST));
                    newFaces.put(BlockFace.EAST, wall.getHeight(BlockFace.SOUTH));
                    newFaces.put(BlockFace.SOUTH, wall.getHeight(BlockFace.WEST));
                }
                if (rotation != 0) {
                    for (BlockFace face : BlockFace.values()) {
                        if(face == BlockFace.EAST || face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.WEST) {
                            wall.setHeight(face, Wall.Height.NONE);
                        }
                    }
                    for (BlockFace face : newFaces.keySet()) {
                        wall.setHeight(face, newFaces.get(face));
                    }
                    blockLocation.getBlock().setBlockData(wall);
                }
            } else if (blockData instanceof Directional) {
                Directional directional = (Directional) blockData;
                if(rotation == 180) {
                    directional.setFacing(directional.getFacing().getOppositeFace());
                } else if(rotation == 270) {
                    if(directional.getFacing() == BlockFace.SOUTH) {
                        directional.setFacing(BlockFace.WEST);
                    } else if(directional.getFacing() == BlockFace.NORTH) {
                        directional.setFacing(BlockFace.EAST);
                    } else if(directional.getFacing() == BlockFace.WEST) {
                        directional.setFacing(BlockFace.NORTH);
                    } else if(directional.getFacing() == BlockFace.EAST) {
                        directional.setFacing(BlockFace.SOUTH);
                    }
                } else if(rotation == 90) {
                    if(directional.getFacing() == BlockFace.SOUTH) {
                        directional.setFacing(BlockFace.EAST);
                    } else if(directional.getFacing() == BlockFace.NORTH) {
                        directional.setFacing(BlockFace.WEST);
                    } else if(directional.getFacing() == BlockFace.WEST) {
                        directional.setFacing(BlockFace.SOUTH);
                    } else if(directional.getFacing() == BlockFace.EAST) {
                        directional.setFacing(BlockFace.NORTH);
                    }
                }
                blockLocation.getBlock().setBlockData(directional);
            }
        }
    }

    private Location rotate(Location startLocation, int rotation) {
        double x = startLocation.getX(), z = startLocation.getZ();
        if(rotation != 0) {
            if (rotation == 90) {
                startLocation.setZ(-x);
                startLocation.setX(z/*-zLength/2*/);
            } else if (rotation == 180) {
                startLocation.setZ((z * -1)/*+(zLength/2)*/);
                startLocation.setX((x * -1));
            } else if (rotation == 270) {
                startLocation.setZ(x);
                startLocation.setX(-z/*+(zLength/2)*/);
            }
        } else {
            startLocation.setX(x);
            startLocation.setZ(z/*-zLength/2*/);
        }

        return startLocation;
    }
}
