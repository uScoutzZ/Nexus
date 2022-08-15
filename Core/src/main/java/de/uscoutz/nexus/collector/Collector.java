package de.uscoutz.nexus.collector;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Collector {

    private NexusPlugin plugin;

    @Getter
    private Location location;

    private LinkedHashMap<Material, Integer> neededItems;
    private Map<Material, ArmorStand> hologram;
    private Location blockLocation;
    private Material oldBlockType;
    private Consumer<Player> actionOnFull;

    public Collector(Location location, List<ItemStack> neededItems, NexusPlugin plugin) {
        this.plugin = plugin;
        this.location = location;
        hologram = new HashMap<>();
        this.neededItems = new LinkedHashMap<>();

        for(ItemStack neededItem : neededItems) {
            this.neededItems.put(neededItem.getType(), neededItem.getAmount());
        }
    }

    public void spawn() {
        spawnHolograms();
        blockLocation = location.clone().subtract(0, 1, 0);
        Block block = blockLocation.getBlock();
        oldBlockType = block.getType();
        block.setType(Material.EMERALD_BLOCK);

        plugin.getCollectorManager().getCollectors().put(block, this);
    }

    private void spawnHolograms() {
        for(Material needed : neededItems.keySet()) {
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.getBlock().getLocation().clone()
                    .add(0.5, hologram.size(), 0.5), EntityType.ARMOR_STAND);

            Item item = (Item) location.getWorld().spawnEntity(armorStand.getLocation(), EntityType.DROPPED_ITEM);
            item.setItemStack(new ItemStack(needed));
            item.setCanPlayerPickup(false);

            armorStand.setSmall(true);
            armorStand.addPassenger(item);
            armorStand.setCustomName("§7" + neededItems.get(needed) + "x " + item.getItemStack().getI18NDisplayName());
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            hologram.put(needed, armorStand);
        }
    }

    public void collect(Player player, Item item) {
        ItemStack itemStack = item.getItemStack();
        if(neededItems.containsKey(itemStack.getType())) {
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
                    actionOnFull.accept(player);
                    destroy();
                } else {
                    destroyHolograms();
                    spawnHolograms();
                }
            } else {
                hologram.get(itemStack.getType()).setCustomName("§7" + neededAmount + "x " + item.getItemStack().getI18NDisplayName());
            }
        } else {
            player.getInventory().addItem(item.getItemStack());
        }
        item.remove();
    }

    private void destroyHolograms() {
        for(ArmorStand armorStand : hologram.values()) {
            armorStand.getPassengers().get(0).remove();
            armorStand.remove();
        }
        hologram.clear();
    }

    public void destroy() {
        blockLocation.getBlock().setType(oldBlockType);
        destroyHolograms();
    }

    public Collector setFilledAction(Consumer<Player> action) {
        this.actionOnFull = action;
        return this;
    }
}
