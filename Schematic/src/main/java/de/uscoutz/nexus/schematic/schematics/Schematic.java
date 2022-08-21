package de.uscoutz.nexus.schematic.schematics;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.collector.Collector;
import de.uscoutz.nexus.schematic.laser.Laser;
import de.uscoutz.nexus.utilities.FireworkUtilities;
import de.uscoutz.nexus.utilities.InventorySerializer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Wall;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.entity.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Schematic {

    private NexusSchematicPlugin plugin;

    @Getter
    private SchematicType schematicType;
    @Getter
    private Map<Integer, Block> blocks;
    @Getter
    private int level, substractX, substractY, substractZ, xLength, zLength, minX, maxX, minZ, maxZ;
    @Getter
    private Location corner1, corner2;
    @Getter @Setter
    private long timeToFinish;


    public Schematic(SchematicType schematicType, int level, NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        this.schematicType = schematicType;
        this.level = level;
        blocks = new HashMap<>();

        corner1 = schematicType.getLocation1().clone().add(0, 0, schematicType.getZDistance()*level);
        corner2 = schematicType.getLocation2().clone().add(0, 0, schematicType.getZDistance()*level);

        int minY, maxY;
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

        List<Material> spawnLater = Arrays.asList(Material.IRON_DOOR, Material.OAK_SIGN, Material.BEACON, Material.LANTERN, Material.TORCH, Material.LAVA);
        List<Block> toAdd = new ArrayList<>();
        for(int j = minY; j <= maxY; j++) {
            for(int i = minX; i <= maxX; i++) {
                for(int k = minZ; k <= maxZ; k++) {
                    Block block = corner1.getWorld().getBlockAt(i, j, k);
                    if(block.getType() != Material.AIR) {
                        if(spawnLater.contains(block.getType())) {
                            toAdd.add(block);
                        } else {
                            blocks.put(blocks.size(), block);
                        }
                    }
                }
            }
        }

        for(Block block : toAdd) {
            blocks.put(blocks.size(), block);
        }

        Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Add " + schematicType + " level " + level);
        plugin.getSchematicManager().getSchematicsMap().get(schematicType).put(level, this);
    }

    private List<Integer> getMinMaxByLocation(Location location, int rotation) {
        int minY = location.getBlockY();

        List<Integer> point = new ArrayList<>();
        List<Location> points = new ArrayList<>();
        points.add(new Location(location.getWorld(), this.maxX-substractX, minY, this.maxZ-substractZ));
        points.add(new Location(location.getWorld(), this.minX-substractX, minY, this.minZ-substractZ));
        points.add(new Location(location.getWorld(), this.minX-substractX, minY, this.maxZ-substractZ));
        points.add(new Location(location.getWorld(), this.maxX-substractX, minY, this.minZ-substractZ));
        for(Location pointX : points) {
            points.set(points.indexOf(pointX), rotate(pointX, rotation));
            pointX.add(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            minX = Math.min(minX, pointX.getBlockX());
            minZ = Math.min(minZ, pointX.getBlockZ());
            maxX = Math.max(maxX, pointX.getBlockX());
            maxZ = Math.max(maxZ, pointX.getBlockZ());
        }

        point.add(minX);
        point.add(minZ);
        point.add(maxX);
        point.add(maxZ);

        return point;
    }

    public boolean preview(Location location, int rotation, boolean set) {
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        int minY = location.getBlockY();

        List<Location> points = new ArrayList<>();
        points.add(new Location(location.getWorld(), this.maxX-substractX, minY, this.maxZ-substractZ));
        points.add(new Location(location.getWorld(), this.minX-substractX, minY, this.minZ-substractZ));
        points.add(new Location(location.getWorld(), this.minX-substractX, minY, this.maxZ-substractZ));
        points.add(new Location(location.getWorld(), this.maxX-substractX, minY, this.minZ-substractZ));

        for(Location pointX : points) {
            points.set(points.indexOf(pointX), rotate(pointX, rotation));
            pointX.add(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            minX = Math.min(minX, pointX.getBlockX());
            minZ = Math.min(minZ, pointX.getBlockZ());
            maxX = Math.max(maxX, pointX.getBlockX());
            maxZ = Math.max(maxZ, pointX.getBlockZ());
        }

        Location point1;
        Location point2;
        Location point3;
        Location point4;

        point1 = new Location(location.getWorld(), maxX+0.9, minY+1.2, maxZ+0.9);
        point2 = new Location(location.getWorld(), minX, minY+1.2, minZ);
        point3 = new Location(location.getWorld(), minX, minY+1.2, maxZ+0.9);
        point4 = new Location(location.getWorld(), maxX+0.9, minY+1.2, minZ);

        Color color1, color2;
        boolean overlap = false;
        Profile profile = NexusPlugin.getInstance().getWorldManager().getWorldProfileMap().get(location.getWorld());
        for(Region profileRegion : profile.getRegions()) {
            if(profileRegion.overlap(minX, maxX, minZ, maxZ)) {
                overlap = true;
                break;
            }
        }

        if(overlap) {
            color1 = Color.RED;
            color2 = Color.RED;
        } else {
            color1 = Color.GREEN;
            color2 = Color.LIME;
            if(set) {
                profile.getRegions().add(new Region(location.getWorld(), minX, maxX, minZ, maxZ, schematicType.toString()));
            }
        }

        drawLine(point3, point2, 0.5, color1, color2);
        drawLine(point1, point3, 0.5, color1, color2);
        drawLine(point1, point4, 0.5, color1, color2);
        drawLine(point2, point4, 0.5, color1, color2);

        return overlap;
    }

    private void drawLine(Location point1, Location point2, double space, Color color1, Color color2) {
        World world = point1.getWorld();
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, p1.getX(), p1.getY(), p1.getZ(), 1, new Particle.DustTransition(color1, color2, 2));
            length += space;
        }
    }

    public void build(Location location, int rotation, long finished, UUID schematicId) {
        Bukkit.broadcastMessage("§ePasting " + blocks.size() + " blocks");
        if(finished <= System.currentTimeMillis()) {
            build(location, rotation, schematicId);
        } else {
            final long[] millis = {finished - System.currentTimeMillis()};

            final String[] counter = {String.format("§e§lFINISHED IN: §7%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis[0]),
                    TimeUnit.MILLISECONDS.toMinutes(millis[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis[0])),
                    TimeUnit.MILLISECONDS.toSeconds(millis[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis[0])))};
            double divided = (double)zLength/2;
            Location asLocation = new Location(location.getWorld(), 0, 0, divided);
            ArmorStand countdown = (ArmorStand) location.getWorld().spawnEntity(rotate(asLocation, rotation)
                    .add(location.getX()+0.5, location.getY(), location.getZ()+0.5), EntityType.ARMOR_STAND);
            countdown.setVisible(false);
            countdown.setGravity(false);
            countdown.setCustomNameVisible(true);
            countdown.setCustomName(counter[0]);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(millis[0] <= 0) {
                        Profile profile = NexusPlugin.getInstance().getWorldManager().getWorldProfileMap().get(location.getWorld());
                        for(Player player : profile.getWorld().getWorld().getPlayers()) {
                            player.sendMessage(NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "schematic_finished-building"));
                        }
                        FireworkUtilities.spawnRandomFirework(countdown.getEyeLocation());
                        if(schematicType == SchematicType.NEXUS) {
                            profile.setNexusLevel(level);
                        }
                        countdown.remove();
                        cancel();
                    } else {
                        millis[0] = millis[0]-1000;
                        counter[0] = String.format("§e§lFINISHED IN: §7%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis[0]),
                                TimeUnit.MILLISECONDS.toMinutes(millis[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis[0])),
                                TimeUnit.MILLISECONDS.toSeconds(millis[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis[0])));
                        countdown.setCustomName(counter[0]);
                    }
                }
            }.runTaskTimer(plugin, 20, 20);
            final int[] i = {0};
            Laser laser;
            try {
                Location endCrystal = NexusPlugin.getInstance().getLocationManager().getLocation("nexus-crystal", location.getWorld());
                laser = new Laser.GuardianLaser(endCrystal, location, -1, 100);
                laser.start(plugin);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            Laser finalLaser = laser;
            double blocksPerSecond = ((double)TimeUnit.MILLISECONDS.toSeconds(timeToFinish)/blocks.size())*20;
            double d = Math.pow(10, 0);
            blocksPerSecond = Math.round(blocksPerSecond * d) / d;
            double finalBlocksPerSecond = blocksPerSecond;

            final int[] minX = {Integer.MAX_VALUE};
            final int[] minZ = { Integer.MAX_VALUE };
            final int[] maxX = { Integer.MIN_VALUE };
            final int[] maxZ = { Integer.MIN_VALUE };

            plugin.getSchematicManager().getBuiltSchematics().put(schematicId, new ArrayList<>());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(i[0] == 0 && System.currentTimeMillis()+timeToFinish > finished+1000) {
                        long secondsSinceStart =
                                TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis()+timeToFinish)-finished);
                        double alreadyPlaced = secondsSinceStart/(finalBlocksPerSecond/20);
                        for(int j = 0; j < alreadyPlaced; j++) {
                            Location block = setBlock(blocks.get(j) ,rotation, location, null, schematicId);
                            plugin.getSchematicManager().getBuiltSchematics().get(schematicId).add(block);
                            minX[0] = Math.min(minX[0], block.getBlockX());
                            minZ[0] = Math.min(minZ[0], block.getBlockZ());
                            maxX[0] = Math.max(maxX[0], block.getBlockX());
                            maxZ[0] = Math.max(maxZ[0], block.getBlockZ());
                        }
                        i[0] = (int) alreadyPlaced;
                    } else {
                        if(i[0] < blocks.size()) {
                            Block block = blocks.get(i[0]);
                            Location setBlock = setBlock(block, rotation, location, finalLaser, schematicId);
                            minX[0] = Math.min(minX[0], setBlock.getBlockX());
                            minZ[0] = Math.min(minZ[0], setBlock.getBlockZ());
                            maxX[0] = Math.max(maxX[0], setBlock.getBlockX());
                            maxZ[0] = Math.max(maxZ[0], setBlock.getBlockZ());
                            plugin.getSchematicManager().getBuiltSchematics().get(schematicId).add(setBlock);
                            i[0]++;
                        } else {
                            finalLaser.stop();
                            cancel();
                        }
                    }
                }
            }.runTaskTimer(plugin, 0, (long) blocksPerSecond);
            Profile profile = NexusPlugin.getInstance().getWorldManager().getWorldProfileMap().get(location.getWorld());
            if(minX[0] != Integer.MAX_VALUE) {
                profile.getRegions().add(new Region(location.getWorld(), minX[0], maxX[0], minZ[0], maxZ[0], schematicType.toString()));
            }
        }
    }

    public void build(Location location, int rotation, UUID schematicId) {
        plugin.getSchematicManager().getBuiltSchematics().put(schematicId, new ArrayList<>());
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for(int j = 0; j < blocks.size(); j++) {
            Location block = setBlock(blocks.get(j), rotation, location, null, schematicId);
            minX = Math.min(minX, block.getBlockX());
            minZ = Math.min(minZ, block.getBlockZ());
            maxX = Math.max(maxX, block.getBlockX());
            maxZ = Math.max(maxZ, block.getBlockZ());
            plugin.getSchematicManager().getBuiltSchematics().get(schematicId).add(block);
        }

        Profile profile = NexusPlugin.getInstance().getWorldManager().getWorldProfileMap().get(location.getWorld());
        if(schematicType == SchematicType.NEXUS) {
            profile.setNexusLevel(level);
        }

        assert minX != Integer.MAX_VALUE;

        profile.getRegions().add(new Region(location.getWorld(), minX, maxX, minZ, maxZ, schematicType.toString()));
    }

    private Location setBlock(Block block, int rotation, Location location, Laser laser, UUID schematicId) {
        Location blockLocation = block.getLocation().clone();
        blockLocation.setX(blockLocation.getX()-substractX);
        blockLocation.setY(blockLocation.getY()-substractY);
        blockLocation.setZ(blockLocation.getZ()-substractZ);
        blockLocation.setWorld(location.getWorld());
        blockLocation = rotate(blockLocation, rotation);
        blockLocation.add(location.getX(), location.getY(), location.getZ());

        if(laser != null) {
            try {
                laser.moveEnd(blockLocation);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

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
                List<ItemStack> neededItems = new ArrayList<>();
                if(NexusPlugin.getInstance().getDatabaseAdapter().keyExists("collectors", "schematicId", schematicId)) {
                    ResultSet resultSet = NexusPlugin.getInstance().getDatabaseAdapter().get("collectors", "schematicId", String.valueOf(schematicId));
                    try {
                        if(resultSet.next()) {
                            neededItems = plugin.getCollectorManager().getNeededItemsFromString(resultSet.getString("neededItems"));
                        }
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    neededItems = new ArrayList<>(plugin.getCollectorManager().getCollectorNeededMap().get(schematicType).get(level));
                    NexusPlugin.getInstance().getDatabaseAdapter().set("collectors", schematicId, Collector.toString(neededItems));
                }

                Collector collector = new Collector(neededItems, schematicId, plugin, schematicType.getFileConfiguration().getInt("requiredNexus." + (level+1)))
                        .setFilledAction(player1 -> {
                            destroy(schematicId, plugin, schematicType);
                            Schematic nextLevel = plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(level+1);
                            Profile profile = NexusPlugin.getInstance().getWorldManager().getWorldProfileMap().get(location.getWorld());
                            nextLevel.build(location, rotation, System.currentTimeMillis()+nextLevel.timeToFinish, schematicId);
                            NexusPlugin.getInstance().getDatabaseAdapter().updateTwoAsync("schematics", "profileId",
                                    profile.getProfileId(),
                                    "schematicId", schematicId,
                                    new DatabaseUpdate("level", level+1),
                                    new DatabaseUpdate("placed", System.currentTimeMillis()));
                        });
                collector.spawn(pastedSign.getLocation());

                blockLocation.getBlock().setType(Material.AIR);
            } else if(pastedSign.getLine(0).equals("[STORAGE]")) {
                String storageId = pastedSign.getLine(1);
                Profile profile = NexusPlugin.getInstance().getWorldManager().getWorldProfileMap().get(location.getWorld());
                Block storage = blockLocation.clone().subtract(0, 1, 0).getBlock();
                if(storage.getState() instanceof Container) {
                    Container container = (Container) storage.getState();
                    if(!profile.getStorages().containsKey(storageId)) {
                        NexusPlugin.getInstance().getDatabaseAdapter().setAsync("storages",
                                profile.getProfileId(), storageId, "");
                        profile.getStorages().put(storageId, "");
                    }
                    String contents = profile.getStorages().get(storageId);
                    profile.getStorageBlocks().put(storageId, container);
                    if(!contents.equals("")) {
                        container.getInventory().setContents(InventorySerializer.fromBase64(contents).getContents());
                    }
                }
                pastedSign.getBlock().setType(Material.AIR);
            }
        } else {
            Map<Integer, Integer> order = new HashMap<>();
            order.put(90, 3);
            order.put(180, 2);
            order.put(270, 1);
            List<BlockFace> blockFaces = new ArrayList<>();
            blockFaces.add(BlockFace.NORTH);
            blockFaces.add(BlockFace.EAST);
            blockFaces.add(BlockFace.SOUTH);
            blockFaces.add(BlockFace.WEST);

            if(block.getBlockData() instanceof MultipleFacing) {
                MultipleFacing multipleFacing = (MultipleFacing) block.getBlockData();
                List<BlockFace> newFaces = new ArrayList<>();

                if (rotation != 0) {
                    for(BlockFace blockFace : blockFaces) {
                        if(multipleFacing.hasFace(blockFace)) {
                            newFaces.add(getBlockFace(blockFace, order.get(rotation)));
                        }
                    }
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

                if (rotation != 0) {
                    int newRotation = rotation;
                    if(newRotation == 90) {
                        newRotation = 270;
                    } else if(newRotation == 270) {
                        newRotation = 90;
                    }
                    for(BlockFace blockFace : blockFaces) {
                        newFaces.put(blockFace, wall.getHeight(getBlockFace(blockFace,
                                order.get(newRotation))));
                    }
                    for (BlockFace face : blockFaces) {
                        wall.setHeight(face, Wall.Height.NONE);
                    }
                    for (BlockFace face : newFaces.keySet()) {
                        wall.setHeight(face, newFaces.get(face));
                    }
                    blockLocation.getBlock().setBlockData(wall);
                }
            } else if (blockData instanceof Directional) {
                Directional directional = (Directional) blockData;

                if(rotation != 0) {
                    if(directional.getFacing() != BlockFace.UP && directional.getFacing() != BlockFace.DOWN) {
                        directional.setFacing(getBlockFace(directional.getFacing(), order.get(rotation)));
                        blockLocation.getBlock().setBlockData(directional);
                    }

                }
            }
        }

        return blockLocation;
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

    private BlockFace getBlockFace(BlockFace blockFace, int rotate) {
        List<BlockFace> blockFaces = new ArrayList<>();
        blockFaces.add(BlockFace.NORTH);
        blockFaces.add(BlockFace.EAST);
        blockFaces.add(BlockFace.SOUTH);
        blockFaces.add(BlockFace.WEST);
        int index = blockFaces.indexOf(blockFace);
        if(index+rotate >= blockFaces.size()) {
            return blockFaces.get((index+rotate)-blockFaces.size());
        } else {
            return blockFaces.get(index+rotate);
        }
    }

    public static void destroy(UUID schematicId, NexusSchematicPlugin plugin, boolean animated) {
        int minHeight = plugin.getSchematicManager().getBuiltSchematics().get(schematicId).get(0).getBlockY();
        List<Location> toRemove = new ArrayList<>();
        for (Location blockLocation : plugin.getSchematicManager().getBuiltSchematics().get(schematicId)) {
            if (blockLocation.getBlockY() == minHeight) {
                toRemove.add(blockLocation);
            }
        }

        plugin.getSchematicManager().getBuiltSchematics().get(schematicId).removeAll(toRemove);

        for (Location location : plugin.getSchematicManager().getBuiltSchematics().get(schematicId)) {
            BlockData blockData = location.getBlock().getBlockData();
            location.getBlock().setType(Material.AIR);

            if(animated) {
                FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, blockData);
                /*double x = -0.25+new Random().nextDouble(0.5);
                double z = -0.25+new Random().nextDouble(0.5);*/
                double x = 0;
                double z = 0;
                fallingBlock.setVelocity(new Vector(x, 0.8, z));
                fallingBlock.setDropItem(false);
            }
        }

        if(!animated) {
            for(Location location : toRemove) {
                location.getBlock().setType(Material.GRASS_BLOCK);
            }
        }
    }

    public static void destroy(UUID schematicId, NexusSchematicPlugin plugin, SchematicType schematicType) {
        World world = plugin.getSchematicManager().getBuiltSchematics().get(schematicId).get(0).getWorld();
        Profile profile = NexusPlugin.getInstance().getWorldManager().getWorldProfileMap().get(world);
        if(schematicType == SchematicType.WORKSHOP) {
            profile.saveStorages();
        }

        destroy(schematicId, plugin, true);
    }
}
