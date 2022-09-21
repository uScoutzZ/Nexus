package de.uscoutz.nexus.utilities;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.events.SchematicInventoryOpenedEvent;
import de.uscoutz.nexus.gamemechanics.tools.Tool;
import de.uscoutz.nexus.inventory.InventoryBuilder;
import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.quests.Quest;
import de.uscoutz.nexus.quests.Task;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.List;

public class InventoryManager {

    private NexusPlugin plugin;

    public InventoryManager(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    public static String toBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(inventory.getSize());

            for(int i = 0; i < inventory.getSize(); ++i) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception var4) {
            throw new IllegalStateException("Unable to save item stacks", var4);
        }
    }

    public static Inventory fromBase64(String data) {
        Inventory inventory = Bukkit.createInventory(null, 9, Component.text("An error occurred"));
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            try {
                inventory = Bukkit.createInventory(null, dataInput.readInt(), Component.text(" "));
            } catch (Exception var6) {
                inventory = Bukkit.createInventory(null, 36, Component.text(" "));
            }

            for(int i = 0; i < inventory.getSize(); ++i) {
                try {
                    inventory.setItem(i, (ItemStack)dataInput.readObject());
                } catch (EOFException exception) {
                    //
                }
            }

            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException | IOException exception) {
            exception.printStackTrace();
        }

        return inventory;
    }

    public static int removeNeededItems(Player player, Material material, Quest quest) {
        return removeNeededItems(player, material, (int) (quest.getTask().getGoal()-quest.getProgress()));
    }

    public static int removeNeededItems(Player player, Material material, int neededItems) {
        ItemStack needed = new ItemStack(material, neededItems);
        int amount = needed.getAmount();
        int added = 0;
        for(ItemStack itemStack : player.getInventory().getContents()) {
            if(itemStack != null) {
                if(itemStack.isSimilar(needed)) {
                    if (itemStack.getAmount() >= amount) {
                        itemStack.setAmount(itemStack.getAmount() - amount);
                        added = added+amount;
                        amount = 0;
                    } else {
                        amount = amount-itemStack.getAmount();
                        added = added+itemStack.getAmount();
                        itemStack.setAmount(0);
                    }
                }
            }

            if(amount == 0) {
                break;
            }
        }

        return added;
    }

    public void openWorkshopSchematics(Player player) {
        SimpleInventory inventory = InventoryBuilder.create(5*9, plugin.getLocaleManager().translate("de_DE", "workshop_schematics"));

        Bukkit.getPluginManager().callEvent(new SchematicInventoryOpenedEvent(inventory, player));

        setNavigationItems(inventory, player, FilterType.SCHEMATICS);
        inventory.open(player);
    }

    public void openWorkshopTools(Player player) {
        SimpleInventory inventory = InventoryBuilder.create(5*9, plugin.getLocaleManager().translate("de_DE", "workshop_tools"));

        for(Tool tool : plugin.getToolManager().getToolMap().values()) {
            inventory.addItem(tool.getItemStack());
        }

        setNavigationItems(inventory, player, FilterType.TOOLS);
        inventory.open(player);
    }

    public void openRescuedItems(Player player) {
        SimpleInventory inventory = InventoryBuilder.create(5*9, plugin.getLocaleManager().translate("de_DE", "villager_rescue"));

        setNavigationItems(inventory, player, FilterType.RESCUED);
        inventory.open(player);
    }

    private void setNavigationItems(SimpleInventory inventory, Player player, FilterType filterType) {
        ItemBuilder schematics = ItemBuilder.create(Material.OAK_LOG)
                .name(plugin.getLocaleManager().translate("de_DE", "workshop_item_schematics"));
        ItemBuilder tools = ItemBuilder.create(Material.GOLDEN_PICKAXE)
                .name(plugin.getLocaleManager().translate("de_DE", "workshop_item_tools"));
        ItemBuilder rescued = ItemBuilder.create(Material.ENDER_CHEST)
                .name(plugin.getLocaleManager().translate("de_DE", "workshop_item_rescue"));

        if(filterType == FilterType.SCHEMATICS) {
            schematics.enchant(Enchantment.FROST_WALKER, 1)
                    .flag(ItemFlag.HIDE_ENCHANTS);
        } else if(filterType == FilterType.TOOLS) {
            tools.enchant(Enchantment.FROST_WALKER, 1)
                    .flag(ItemFlag.HIDE_ENCHANTS);
        } else {
            rescued.enchant(Enchantment.FROST_WALKER, 1)
                    .flag(ItemFlag.HIDE_ENCHANTS);
        }

        inventory.setItem(36, schematics, event -> openWorkshopSchematics(player));
        inventory.setItem(37, tools, event -> openWorkshopTools(player));
        inventory.setItem(38, rescued, event -> openRescuedItems(player));
        inventory.fill(27, 36, ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE).name(" "));
    }

    private enum FilterType {
        SCHEMATICS,
        TOOLS,
        RESCUED;
    }
}
