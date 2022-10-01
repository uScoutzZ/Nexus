package de.uscoutz.nexus.schematic.collector;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.util.*;

public class Collector {

    private NexusSchematicPlugin plugin;

    @Getter
    private Location location;
    @Getter
    private boolean destroyed;

    private UUID schematicId;
    private LinkedHashMap<Material, Integer> neededItems;
    private Map<Material, ArmorStand> hologram;
    private Location blockLocation;
    private Material oldBlockType;
    private Consumer<Player> actionOnFull;
    private int requiredNexusLevel;
    private Material blockType;
    private Block block;
    @Getter
    private String title;
    @Getter
    private BuiltSchematic schematic;
    private Profile profile;
    private SchematicProfile schematicProfile;

    public Collector(List<ItemStack> neededItems, UUID schematicId, NexusSchematicPlugin plugin, int requiredNexusLevel, Material blockType, String title) {
        this.plugin = plugin;
        hologram = new HashMap<>();
        this.neededItems = new LinkedHashMap<>();
        destroyed = false;
        this.schematicId = schematicId;
        this.requiredNexusLevel = requiredNexusLevel;
        this.blockType = blockType;
        this.title = title;

        for(ItemStack neededItem : neededItems) {
            this.neededItems.put(neededItem.getType(), neededItem.getAmount());
        }
    }

    public void spawn(Location location) {
        this.location = location;
        spawnHolograms();
        blockLocation = location.clone().subtract(0, 1, 0);
        block = blockLocation.getBlock();
        oldBlockType = block.getType();
        block.setType(blockType);

        this.profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(blockLocation.getWorld());
        schematicProfile = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId());
        this.schematic = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getBuiltSchematics().get(schematicId);
        schematicProfile.getCollectors().put(block, this);
    }

    private void spawnHolograms() {
        for(Material needed : neededItems.keySet()) {
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.getBlock().getLocation().clone()
                    .add(0.5, hologram.size(), 0.5), EntityType.ARMOR_STAND);

            Item item = (Item) location.getWorld().spawnEntity(armorStand.getLocation(), EntityType.DROPPED_ITEM);
            item.setItemStack(new ItemStack(needed));
            item.setCanPlayerPickup(false);
            item.setUnlimitedLifetime(true);

            armorStand.setSmall(true);
            armorStand.addPassenger(item);
            //armorStand.customName(Component.text("§7" + neededItems.get(needed) + "x " + item.getItemStack().getI18NDisplayName()));
            armorStand.setCustomName("§7" + neededItems.get(needed) + "x " + item.getItemStack().getI18NDisplayName());
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            hologram.put(needed, armorStand);
        }

        if(title != null) {
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.getBlock().getLocation().clone()
                    .add(0.5, hologram.size()-0.4, 0.5), EntityType.ARMOR_STAND);
            armorStand.setSmall(true);
            armorStand.customName(Component.text(title));
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            hologram.put(Material.AIR, armorStand);
        }
    }

    public void collect(Player player, Item item) {
        ItemStack itemStack = item.getItemStack();
        if(neededItems.containsKey(itemStack.getType())) {
            int maxConcurrentBuildings = plugin.getNexusPlugin().getConfig().getInt("concurrently-building");
            if((schematic.getSchematic().getSchematicType() == SchematicType.WORKSHOP && !profile.getQuests().containsKey(Task.REPAIR_WORKSHOP)
                    || (schematic.getSchematic().getSchematicType() == SchematicType.NEXUS && !profile.getQuests().containsKey(Task.UPGRADE_NEXUS)))) {
                player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "finish-quest-first"));
                player.getInventory().addItem(item.getItemStack());
            } else {
                if(profile.getConcurrentlyBuilding() >= maxConcurrentBuildings) {
                    player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_too-much-concurrent-buildings", maxConcurrentBuildings));
                    player.getInventory().addItem(item.getItemStack());
                } else {
                    int b = blockType == Material.EMERALD_BLOCK ? 1:0;
                    if(requiredNexusLevel <= plugin.getNexusPlugin().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getCurrentProfile().getNexusLevel()) {
                        int neededAmount = neededItems.get(itemStack.getType());
                        if(neededAmount >= itemStack.getAmount()) {
                            neededItems.replace(itemStack.getType(), neededAmount- itemStack.getAmount());
                        } else if(neededAmount < itemStack.getAmount()) {
                            neededItems.replace(itemStack.getType(), 0);
                            itemStack.setAmount(itemStack.getAmount()-neededAmount);
                            player.getInventory().addItem(itemStack);
                        }
                        neededAmount = neededItems.get(itemStack.getType());
                        if(neededAmount == 0) {
                            neededItems.remove(itemStack.getType());
                            if(neededItems.size() == 0) {
                                destroy();
                                actionOnFull.accept(player);
                                plugin.getNexusPlugin().getDatabaseAdapter().deleteTwo("collectors", "schematicId", schematicId, "intact", b);
                            } else {
                                destroyHolograms();
                                spawnHolograms();
                            }
                        } else {
                            //hologram.get(itemStack.getType()).customName(Component.text("§7" + neededAmount + "x " + item.getItemStack().getI18NDisplayName()));
                            hologram.get(itemStack.getType()).setCustomName("§7" + neededAmount + "x " + item.getItemStack().getI18NDisplayName());
                        }

                        if(!destroyed) {
                            plugin.getNexusPlugin().getDatabaseAdapter().updateTwoAsync("collectors", "schematicId", schematicId,
                                    "intact", b, new DatabaseUpdate("neededItems", toString()));
                        }
                    } else {
                        player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "collector_wrong-level"));
                        player.getInventory().addItem(item.getItemStack());
                    }
                }
            }
        } else {
            player.getInventory().addItem(item.getItemStack());
        }
        item.remove();
    }

    public String toString() {
        StringBuilder collector = new StringBuilder();
        for(Material material : neededItems.keySet()) {
            if(!collector.toString().equals("")) {
                collector.append(", ");
            }
            collector.append(material).append(":").append(neededItems.get(material));
        }

        return collector.toString();
    }

    public static String toString(List<ItemStack> needed) {
        StringBuilder collector = new StringBuilder();
        for(ItemStack material : needed) {
            if(!collector.toString().equals("")) {
                collector.append(", ");
            }
            collector.append(material.getType()).append(":").append(material.getAmount());
        }

        return collector.toString();
    }

    private void destroyHolograms() {
        for(ArmorStand armorStand : hologram.values()) {
            if(armorStand.getPassengers().size() != 0) {
                armorStand.getPassengers().get(0).remove();
            }

            armorStand.remove();
        }
        hologram.clear();
    }

    public void destroy() {
        destroyed = true;
        schematicProfile.getCollectors().remove(block);
        blockLocation.getBlock().setType(oldBlockType);
        destroyHolograms();
    }

    public void setTitle(String title) {
        this.title = title;
        for(ArmorStand armorStand : hologram.values()) {
            if(armorStand.getPassengers().size() != 0) {
                armorStand.customName(Component.text(title));
            }

            armorStand.remove();
        }
    }

    public Collector setFilledAction(Consumer<Player> action) {
        this.actionOnFull = action;
        return this;
    }
}
