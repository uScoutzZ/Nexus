package de.uscoutz.nexus.utilities;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.events.SchematicInventoryOpenedEvent;
import de.uscoutz.nexus.events.SchematicItemBoughtEvent;
import de.uscoutz.nexus.gamemechanics.NexusItem;
import de.uscoutz.nexus.gamemechanics.shops.ItemPrice;
import de.uscoutz.nexus.gamemechanics.shops.MoneyPrice;
import de.uscoutz.nexus.gamemechanics.shops.NexusPrice;
import de.uscoutz.nexus.gamemechanics.tools.Tool;
import de.uscoutz.nexus.inventory.InventoryBuilder;
import de.uscoutz.nexus.inventory.PaginatedInventory;
import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.quests.Quest;
import de.uscoutz.nexus.quests.Task;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

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
        return  fromBase64(data, null);
    }

    public static Inventory fromBase64(String data, Inventory playerInventory) {
        Inventory inventory = Bukkit.createInventory(null, 9, Component.text("An error occurred"));
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int size = dataInput.readInt();

            try {
                inventory = Bukkit.createInventory(null, size, Component.text(" "));
            } catch (Exception var6) {
                inventory = Bukkit.createInventory(null, 3*9, Component.text(" "));
            }

            if(playerInventory != null) {
                inventory = playerInventory;
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
        return removeNeededItems(player, Arrays.asList(new ItemStack(material, (int) (quest.getTask().getGoal()-quest.getProgress()))));
    }

    public static int removeNeededItems(Player player, List<ItemStack> toRemove) {
        int added = 0;
        for(ItemStack needed : toRemove) {
            int amount = needed.getAmount();
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
        }

        return added;
    }

    public static List<ItemStack> getNeededItemsFromString(String needed) {
        List<ItemStack> neededItems = new ArrayList<>();

        if(needed != null && !needed.equals("")) {
            for(String stringMaterial : needed.split(", ")) {
                int amount = Integer.parseInt(stringMaterial.split(":")[1]);
                Material material = Material.getMaterial(stringMaterial.split(":")[0]);
                try {
                    neededItems.add(new ItemStack(material, amount));
                } catch (IllegalArgumentException exception) {
                    Bukkit.getConsoleSender().sendMessage("[NexusSchematic] Material " + stringMaterial +" not found");
                }
            }
        }

        return neededItems;
    }

    public void openWorkshopSchematics(Player player) {
        SimpleInventory inventory = InventoryBuilder.create(3*9, plugin.getLocaleManager().translate(
                plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "workshop_schematics"));

        Bukkit.getPluginManager().callEvent(new SchematicInventoryOpenedEvent(inventory, player));

        setNavigationItems(inventory, player, FilterType.SCHEMATICS);
        inventory.open(player);
    }

    public void openWorkshopTools(Player player) {
        PaginatedInventory inventory = InventoryBuilder.createPaginated(4*9, plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "workshop_tools"));
        inventory.addDynamicSlots(IntStream.range(0, 2*9).toArray());

        for(Tool tool : plugin.getToolManager().getToolMap().values()) {
            getShopItem(player, inventory, tool.getItemStack(), tool.getPrices());
        }

        setNavigationItems(inventory, player, FilterType.TOOLS);
        inventory.setPageSwitcherForwardSlot(inventory.getInventory().getSize()-1);
        inventory.setPageSwitcherBackSlot(inventory.getInventory().getSize()-2);
        inventory.open(player);
    }

    public void openProfiles(Player player) {
        SimpleInventory inventory = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).openProfiles(null, false);

        setNavigationItems(inventory, player, FilterType.PROFILES);
        inventory.open(player);
    }

    public void openRescuedItems(Player player) {
        SimpleInventory inventory = InventoryBuilder.create(5*9, plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "villager_rescue"));

        setNavigationItems(inventory, player, FilterType.RESCUED);
        inventory.open(player);
    }


    public ItemStack getShopItem(Player player, SimpleInventory simpleInventory, ItemStack itemStack, NexusPrice... prices) {
        return getShopItem(player, simpleInventory, itemStack, null, prices);
    }

    public ItemStack getShopItem(Player player, SimpleInventory simpleInventory, ItemStack itemStack, String message, NexusPrice... prices) {
        List<Component> lore = new ArrayList<>();
        ItemStack shopItem = itemStack.clone();
        String language = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage();
        lore.add(Component.text(plugin.getLocaleManager().translate(language, "workshop_needed-items")));
        boolean playerHasItems = true;

        if(prices.length == 0) {
            lore.add(Component.text("§cNot configured"));
        }
        for(NexusPrice nexusPrice : prices) {
            if(nexusPrice instanceof ItemPrice itemPrice) {
                for(String string : itemPrice.getDisplayList()) {
                    lore.add(Component.text(string));
                }
            } else {
                lore.add(Component.text(nexusPrice.getDisplay(player)));
            }

            if(!nexusPrice.containsNeeded(player)) {
                playerHasItems = false;
            }
        }

        if(message != null) {
            lore.add(Component.text(" "));
            lore.add(Component.text(message));
        }

        PersistentDataContainer dataContainer = itemStack.getItemMeta().getPersistentDataContainer();
        String key = dataContainer.get(new NamespacedKey(plugin.getName().toLowerCase(), "key"), PersistentDataType.STRING);
        NexusItem item = plugin.getNexusItemManager().getItemMap().get(key);
        lore.add(Component.text(" "));
        lore.add(Component.text(item.getRarity().toString(player)));

        shopItem.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        shopItem.lore(lore);

        PaginatedInventory paginatedInventory = null;
        if(simpleInventory instanceof PaginatedInventory) {
            paginatedInventory = (PaginatedInventory) simpleInventory;
            simpleInventory = paginatedInventory;
        }

        if(player.getGameMode() == GameMode.CREATIVE) {
            playerHasItems = true;
        }

        boolean finalPlayerHasItems = playerHasItems;
        Consumer<InventoryClickEvent> clickEventConsumer;
        if(message == null) { //TODO: diesen Pfusch ausbessern -> checken ob maximum schematics dieses typs erreicht
            clickEventConsumer = event -> {
                if(!finalPlayerHasItems) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0F, 1.0F);
                    player.sendMessage(plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "workshop_missing-items", plugin.getConfig().get("villager-name")));
                } else {
                    player.closeInventory();
                    for(NexusPrice nexusPrice : prices) {
                        nexusPrice.remove(player);
                    }
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);

                    ItemStack playerItem = itemStack.clone();
                    plugin.getNexusItemManager().updateItem(playerItem, language);
                    player.getInventory().addItem(playerItem);
                    Bukkit.getPluginManager().callEvent(new SchematicItemBoughtEvent(key, plugin.getWorldManager().getWorldProfileMap().get(player.getWorld())));
                }
            };
        } else {
            clickEventConsumer = event -> {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0F, 1.0F);
            };
        }

        if(paginatedInventory != null) {
            paginatedInventory.addItem(shopItem, clickEventConsumer);
        } else {
            simpleInventory.addItem(shopItem, clickEventConsumer);
        }
        return shopItem;
    }

    private void setNavigationItems(SimpleInventory inventory, Player player, FilterType filterType) {
        NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
        ItemBuilder schematics = ItemBuilder.create(Material.OAK_LOG)
                .name(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), "workshop_item_schematics"));
        ItemBuilder tools = ItemBuilder.create(Material.GOLDEN_PICKAXE)
                .name(plugin.getLocaleManager().translate("de_DE", "workshop_item_tools"));
        ItemBuilder profiles = ItemBuilder.create(Material.COMPARATOR)
                .name(plugin.getLocaleManager().translate("de_DE", "workshop_item_profiles"));
        ItemBuilder rescued = ItemBuilder.create(Material.ENDER_CHEST)
                .name(plugin.getLocaleManager().translate("de_DE", "workshop_item_rescue"));

        if(filterType == FilterType.SCHEMATICS) {
            schematics.enchant(Enchantment.FROST_WALKER, 1)
                    .flag(ItemFlag.HIDE_ENCHANTS);
        } else if(filterType == FilterType.TOOLS) {
            tools.enchant(Enchantment.FROST_WALKER, 1)
                    .flag(ItemFlag.HIDE_ENCHANTS);
        } else if(filterType == FilterType.PROFILES) {
            profiles.enchant(Enchantment.FROST_WALKER, 1)
                    .flag(ItemFlag.HIDE_ENCHANTS);
        } else if(filterType == FilterType.RESCUED) {
            rescued.enchant(Enchantment.FROST_WALKER, 1)
                    .flag(ItemFlag.HIDE_ENCHANTS);
        }

        inventory.fill(inventory.getInventory().getSize()-9, inventory.getInventory().getSize(), ItemBuilder.create(Material.AIR));
        inventory.setItem(inventory.getInventory().getSize()-9, schematics, event -> openWorkshopSchematics(player));
        inventory.setItem(inventory.getInventory().getSize()-8, tools, event -> openWorkshopTools(player));
        inventory.setItem(inventory.getInventory().getSize()-7, profiles, event -> openProfiles(player));
        inventory.setItem(inventory.getInventory().getSize()-6, rescued, event -> openRescuedItems(player));
        inventory.fill(inventory.getInventory().getSize()-18, inventory.getInventory().getSize()-9, ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE).name(" "));
    }

    private enum FilterType {
        SCHEMATICS,
        TOOLS,
        RESCUED,
        PROFILES;
    }
}
