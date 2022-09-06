package de.uscoutz.nexus.schematic.schematics;

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
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.*;
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
    private long timeToFinish, durability;
    
    public Schematic(SchematicType schematicType, int level, Condition condition, NexusSchematicPlugin plugin) {
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

        if(condition == Condition.DAMAGED) {
            minY += 63;
            maxY += 63;
        } else if(condition == Condition.DESTROYED){
            minY += 126;
            maxY += 126;
        }

        substractX = minX;
        substractY = minY;
        substractZ = minZ;

        xLength = maxX-minX;
        zLength = maxZ-minZ;

        List<Material> spawnLater = Arrays.asList(Material.IRON_DOOR, Material.OAK_SIGN, Material.OAK_WALL_SIGN, Material.BEACON, Material.LANTERN, Material.TORCH, Material.LAVA);
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

        plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(condition).put(level, this);
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
        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(location.getWorld());
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
                profile.getRegions().add(new Region(plugin.getNexusPlugin(), location.getWorld(), minX, maxX, minZ, maxZ, schematicType.toString()));
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

    public void build(Location location, int rotation, long finished, UUID schematicId, double damage) {
        Bukkit.broadcastMessage("§ePasting " + blocks.size() + " blocks");
        if(finished <= System.currentTimeMillis()) {
            build(location, rotation, schematicId, damage, false);
        } else {
            Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(location.getWorld());
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
            countdown.customName(Component.text(counter[0]));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(profile.loaded()) {
                        if(millis[0] <= 0) {

                            for(Player player : profile.getWorld().getWorld().getPlayers()) {
                                player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_finished-building"));
                            }
                            profile.setConcurrentlyBuilding(profile.getConcurrentlyBuilding()-1);
                            FireworkUtilities.spawnRandomFirework(countdown.getEyeLocation());
                            if(schematicType == SchematicType.NEXUS) {
                                profile.setNexusLevel(level);
                            } else if(schematicType == SchematicType.TOWER) {
                                if(profile.getHighestTower() < level) {
                                    profile.setHighestTower(level);
                                    for(Block block : plugin.getGatewayManager().getGateways().get(level).getBlocksInRegion(profile.getWorld().getWorld())) {
                                        block.setType(Material.AIR);
                                    }
                                }
                            }

                            countdown.remove();
                            cancel();
                        } else {
                            millis[0] = millis[0]-1000;
                            counter[0] = String.format("§e§lFINISHED IN: §7%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis[0]),
                                    TimeUnit.MILLISECONDS.toMinutes(millis[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis[0])),
                                    TimeUnit.MILLISECONDS.toSeconds(millis[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis[0])));
                            countdown.customName(Component.text(counter[0]));
                        }
                    } else {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 20, 20);
            final int[] i = {0};
            Laser laser;
            try {
                Location endCrystal = plugin.getNexusPlugin().getLocationManager().getLocation("nexus-crystal", location.getWorld());
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

            List<Location> blocks1 = new ArrayList<>();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(profile.loaded()) {
                        if(i[0] == 0 && System.currentTimeMillis()+timeToFinish > finished+1000) {
                            long secondsSinceStart =
                                    TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis()+timeToFinish)-finished);
                            double alreadyPlaced = secondsSinceStart/(finalBlocksPerSecond/20);
                            for(int j = 0; j < alreadyPlaced; j++) {
                                Location block = setBlock(blocks.get(j), rotation, location, null, schematicId, damage);
                                blocks1.add(block);
                                minX[0] = Math.min(minX[0], block.getBlockX());
                                minZ[0] = Math.min(minZ[0], block.getBlockZ());
                                maxX[0] = Math.max(maxX[0], block.getBlockX());
                                maxZ[0] = Math.max(maxZ[0], block.getBlockZ());
                            }
                            i[0] = (int) alreadyPlaced;
                        } else {
                            if(i[0] < blocks.size()) {
                                Block block = blocks.get(i[0]);
                                Location setBlock = setBlock(block, rotation, location, finalLaser, schematicId, damage);
                                minX[0] = Math.min(minX[0], setBlock.getBlockX());
                                minZ[0] = Math.min(minZ[0], setBlock.getBlockZ());
                                maxX[0] = Math.max(maxX[0], setBlock.getBlockX());
                                maxZ[0] = Math.max(maxZ[0], setBlock.getBlockZ());
                                blocks1.add(setBlock);
                                i[0]++;
                            } else {
                                finalLaser.stop();
                                cancel();
                            }
                        }
                    } else {
                        cancel();
                        finalLaser.stop();
                    }
                }
            }.runTaskTimer(plugin, 0, (long) blocksPerSecond);
            profile.setConcurrentlyBuilding(profile.getConcurrentlyBuilding()+1);
            if(minX[0] != Integer.MAX_VALUE) {
                finish(profile, location, minX[0], maxX[0], minZ[0], maxZ[0], rotation, damage, schematicId, blocks1);
            }
        }
    }

    public void build(Location location, int rotation, UUID schematicId, double damage, boolean update) {
        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(location.getWorld());
        List<Location> blocks1 = new ArrayList<>();
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for(int j = 0; j < blocks.size(); j++) {
            Location block = setBlock(blocks.get(j), rotation, location, null, schematicId, damage);
            minX = Math.min(minX, block.getBlockX());
            minZ = Math.min(minZ, block.getBlockZ());
            maxX = Math.max(maxX, block.getBlockX());
            maxZ = Math.max(maxZ, block.getBlockZ());
            blocks1.add(block);
        }

        if(!update) {
            if(schematicType == SchematicType.NEXUS) {
                profile.setNexusLevel(level);
            } else if(schematicType == SchematicType.TOWER) {
                if(profile.getHighestTower() < level) {
                    profile.setHighestTower(level);
                }
            }

            assert minX != Integer.MAX_VALUE;
            Bukkit.getConsoleSender().sendMessage(blocks1.size() + "");
            finish(profile, location, minX, maxX, minZ, maxZ, rotation, damage, schematicId, blocks1);
        } else {
            Bukkit.broadcastMessage("update: " + damage);
            Region region = plugin.getNexusPlugin().getRegionManager().getRegion(location);
            BuiltSchematic builtSchematic = plugin.getSchematicManager().getSchematicProfileMap().get(
                    profile.getProfileId()).getBuiltSchematics().get(schematicId);
            builtSchematic.setDamage(damage);
            builtSchematic.setBlocks(blocks1);
            builtSchematic.setSchematic(this);
        }
    }

    private void finish(Profile profile, Location location, int minX, int maxX, int minZ, int maxZ, int rotation, double damage, UUID schematicId, List<Location> blocks1) {
        Region region = new Region(plugin.getNexusPlugin(), location.getWorld(), minX, maxX, minZ, maxZ, schematicType.toString());
        profile.getRegions().add(region);
        plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getSchematics().get(schematicType).add(region);
        plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getSchematicsByRegion().put(region,
                new BuiltSchematic(plugin, this, damage, schematicId, profile, rotation, location, blocks1));
    }

    private Location setBlock(Block block, int rotation, Location location, Laser laser, UUID schematicId, double damage) {
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

        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(location.getWorld());
        if(block.getState() instanceof Sign) {
            Block pastedBlock = blockLocation.getBlock();
            Sign sign = (Sign) block.getState();
            Sign pastedSign = (Sign) pastedBlock.getState();
            for(int l = 0; l < sign.lines().size(); l++) {
                pastedSign.line(l, sign.line(l));
            }
            pastedSign.update();
            if(pastedSign.getLine(0).equalsIgnoreCase("[COLLECTOR]")) {
                double percentDamage = damage/durability;
                Condition condition = BuiltSchematic.getCondition(percentDamage);
                List<ItemStack> neededItems = new ArrayList<>();
                int b = condition == Condition.INTACT ? 1:0;
                if(plugin.getNexusPlugin().getDatabaseAdapter().keyExistsTwo("collectors", "schematicId", schematicId, "intact", b)) {
                    ResultSet resultSet = plugin.getNexusPlugin().getDatabaseAdapter().getTwo("collectors", "schematicId", String.valueOf(schematicId), "intact", String.valueOf(b));
                    try {
                        if(resultSet.next()) {
                            neededItems = plugin.getCollectorManager().getNeededItemsFromString(resultSet.getString("neededItems"));
                        }
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    neededItems = new ArrayList<>(plugin.getCollectorManager().getCollectorNeededMap().get(schematicType).get(condition).get(level));
                    plugin.getNexusPlugin().getDatabaseAdapter().set("collectors", schematicId, Collector.toString(neededItems), b);
                }

                Collector oldCollector = plugin.getCollectorManager().getCollectors().get(pastedSign.getLocation().clone().subtract(0, 1, 0).getBlock());
                if(oldCollector != null) {
                    oldCollector.destroy();
                }
                Collector collector;
                if(condition == Condition.INTACT) {
                    collector = new Collector(neededItems, schematicId, plugin, schematicType.getFileConfiguration().getInt("requiredNexus." + (level+1)), Material.EMERALD_BLOCK, "§a§lUPGRADE")
                            .setFilledAction(player1 -> {
                                destroy(profile, schematicId, plugin, schematicType);
                                Schematic nextLevel = plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(Condition.INTACT).get(level+1);
                                nextLevel.build(location, rotation, System.currentTimeMillis()+nextLevel.timeToFinish, schematicId, 0);
                                plugin.getNexusPlugin().getDatabaseAdapter().updateTwoAsync("schematics", "profileId",
                                        profile.getProfileId(),
                                        "schematicId", schematicId,
                                        new DatabaseUpdate("level", level+1),
                                        new DatabaseUpdate("placed", System.currentTimeMillis()));
                            });
                } else {
                    collector = new Collector(neededItems, schematicId, plugin, 0, Material.REDSTONE_BLOCK, "§c§lREPAIR")
                            .setFilledAction(player1 -> {
                                destroy(profile, schematicId, plugin, DestroyAnimation.SILENT);
                                Schematic repaired = plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(Condition.INTACT).get(level);
                                repaired.build(location, rotation, schematicId, 0, true);
                                plugin.getNexusPlugin().getDatabaseAdapter().updateTwoAsync("schematics", "profileId",
                                        profile.getProfileId(), "schematicId", schematicId,
                                        new DatabaseUpdate("damage", 0));
                            });
                }
                collector.spawn(pastedSign.getLocation());

                blockLocation.getBlock().setType(Material.AIR);
            } else if(pastedSign.getLine(0).equals("[STORAGE]")) {
                Block storage;
                if(block.getBlockData() instanceof WallSign) {
                    WallSign wallSign = (WallSign) blockLocation.getBlock().getBlockData();

                    if(wallSign.getFacing() == BlockFace.SOUTH) {
                        storage = blockLocation.clone().subtract(0, 0, 1).getBlock();
                    } else if(wallSign.getFacing() == BlockFace.WEST) {
                        storage = blockLocation.clone().add(1, 0, 0).getBlock();
                    } else if(wallSign.getFacing() == BlockFace.NORTH) {
                        storage = blockLocation.clone().add(0, 0, 1).getBlock();
                    } else {
                        storage = blockLocation.clone().subtract(1, 0, 0).getBlock();
                    }
                    if(storage.getType() == Material.AIR) {
                        Bukkit.getConsoleSender().sendMessage(storage.getLocation() +" ");
                    }
                } else {
                    storage = blockLocation.clone().subtract(0, 1, 0).getBlock();
                }
                String storageId = pastedSign.getLine(1);

                if(storage.getState() instanceof Container) {
                    Container container = (Container) storage.getState();
                    if(!profile.getStorages().containsKey(storageId)) {
                        plugin.getNexusPlugin().getDatabaseAdapter().setAsync("storages",
                                profile.getProfileId(), storageId, "");
                        profile.getStorages().put(storageId, "");
                    }
                    String contents = profile.getStorages().get(storageId);
                    profile.getStorageBlocks().put(storageId, container);
                    if(!contents.equals("")) {
                        try {
                            container.getInventory().setContents(InventorySerializer.fromBase64(contents).getContents());
                        } catch (IllegalArgumentException exception) {
                            for(ItemStack itemStack : InventorySerializer.fromBase64(contents).getContents()) {
                                if(itemStack != null) {
                                    container.getInventory().addItem(itemStack);
                                }
                            }
                        }
                    }
                }
                pastedSign.getBlock().setType(Material.AIR);
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

    public static void destroy(Profile profile, UUID schematicId, NexusSchematicPlugin plugin, DestroyAnimation animation) {
        int minHeight = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getBuiltSchematics().get(schematicId).getBlocks().get(0).getBlockY();
        List<Location> toRemove = new ArrayList<>();
        for (Location blockLocation : plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getBuiltSchematics().get(schematicId).getBlocks()) {
            if (blockLocation.getBlockY() == minHeight) {
                toRemove.add(blockLocation);
            }
        }

        plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getBuiltSchematics().get(schematicId).getBlocks().removeAll(toRemove);

        for (Location location : plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getBuiltSchematics().get(schematicId).getBlocks()) {
            BlockData blockData = location.getBlock().getBlockData();

            location.getBlock().setType(Material.AIR);

            if(animation == DestroyAnimation.UPGRADE) {
                FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, blockData);
                double x = 0;
                double z = 0;
                fallingBlock.setVelocity(new Vector(x, 0.8, z));
                fallingBlock.setDropItem(false);
            }
        }

        if(animation == DestroyAnimation.SILENT || animation == DestroyAnimation.PLAYER) {
            for(Location location : toRemove) {
                Collector oldCollector = plugin.getCollectorManager().getCollectors().get(location.getBlock());
                if(oldCollector != null) {
                    oldCollector.destroy();
                }
                location.getBlock().setType(Material.GRASS_BLOCK);
            }
        }
        if(animation == DestroyAnimation.PLAYER) {
            plugin.getNexusPlugin().getDatabaseAdapter().delete("schematics", "schematicId", schematicId);
            Region region = plugin.getNexusPlugin().getRegionManager().getRegion(plugin.getSchematicManager().getSchematicProfileMap().get(
                    profile.getProfileId()).getBuiltSchematics().get(schematicId).getBlocks().get(0));
            plugin.getNexusPlugin().getRegionManager().getRegions().remove(region);
            profile.getRegions().remove(region);
        }
    }

    public static void destroy(Profile profile, UUID schematicId, NexusSchematicPlugin plugin, SchematicType schematicType) {
        World world = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getBuiltSchematics().get(schematicId).getBlocks().get(0).getWorld();
        if(schematicType == SchematicType.WORKSHOP) {
            profile.saveStorages();
        }

        destroy(profile, schematicId, plugin, DestroyAnimation.UPGRADE);
    }
}
