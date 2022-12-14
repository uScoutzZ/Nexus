package de.uscoutz.nexus.schematic.autominer;

import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.inventory.InventoryBuilder;
import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.Condition;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class AutoMiner {

    private NexusSchematicPlugin plugin;

    private final Profile profile;
    private final BuiltSchematic builtSchematic;
    @Getter
    private SimpleInventory inventory;
    private AutoMinerManager.AutoMinerType autoMinerType;

    public AutoMiner(BuiltSchematic builtSchematic, NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        this.builtSchematic = builtSchematic;
        profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(builtSchematic.getLocation().getWorld());

        Consumer<InventoryClickEvent> eventConsumer = event -> {
            Player player = (Player) event.getWhoClicked();
            if(event.getCurrentItem() == null) {
                return;
            }
            Material type = event.getCurrentItem().getType();
            int canAdd = 0;
            for(int i = 0; i < 4*9; i++) {
                if(player.getInventory().getItem(i) == null) {
                    canAdd += 64;
                } else if(player.getInventory().getItem(i).getType() == type) {
                    canAdd += player.getInventory().getItem(i).getMaxStackSize() - player.getInventory().getItem(i).getAmount();
                }
            }
            if(canAdd >= event.getCurrentItem().getAmount()) {
                player.getInventory().addItem(event.getCurrentItem());
                event.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
            } else {
                event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()-canAdd);
                player.getInventory().addItem(new ItemStack(type, canAdd));
            }
        };

        ResultSet resultSet = plugin.getNexusPlugin().getDatabaseAdapter().getTwoAsync("autoMiners", "profileId", profile.getProfileId().toString(), "schematicId", builtSchematic.getSchematicId().toString());
        try {
            inventory = InventoryBuilder.create(3*9, "AutoMiner");
            inventory.setDeleteOnClose(false);
            for(int i = 0; i < 18; i++) {
                inventory.setItem(i, new ItemStack(Material.AIR), eventConsumer);
            }
            inventory.fill(18, 27, ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE).name(" "));
            if (resultSet.next()) {
                ItemStack[] contents = plugin.getNexusPlugin().getInventoryManager().fromBase64(resultSet.getString("inventory")).getContents();
                for(ItemStack itemStack : contents) {
                    if(itemStack != null) {
                        inventory.getInventory().addItem(itemStack);
                    }
                }
                autoMinerType = AutoMinerManager.AutoMinerType.valueOf(resultSet.getString("autoMinerType"));
            } else {
                autoMinerType = AutoMinerManager.AutoMinerType.STONE;
                plugin.getNexusPlugin().getDatabaseAdapter().setAsync("autoMiners", profile.getProfileId().toString(), builtSchematic.getSchematicId().toString(), autoMinerType.toString(), plugin.getNexusPlugin().getInventoryManager().toBase64(inventory.getInventory()));
            }
            setTypeSwitcher();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if(builtSchematic.isBuilt() && builtSchematic.getCondition() != Condition.DESTROYED) {
                    double random = Math.random();
                    if(random > 0.5 || builtSchematic.getCondition() == Condition.INTACT) {
                        inventory.getInventory().addItem(new ItemStack(autoMinerType.getRandomMaterial().getMaterial()));
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 100);
    }

    private void setTypeSwitcher() {
        inventory.setItem(26, ItemBuilder.create(Material.COMPARATOR)
                .name(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "autominer_change-type"))
                .lore(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "autominer_current-type",
                        plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "autominer_type_" + autoMinerType.toString().toLowerCase()))), inventoryClickEvent -> {
            SimpleInventory simpleInventory = InventoryBuilder.create(1*9, "AutoMiner");
            for(AutoMinerManager.AutoMinerType minerType : AutoMinerManager.AutoMinerType.values()) {
                simpleInventory.addItem(ItemBuilder
                        .create(plugin.getAutoMinerManager().getMaterialsPerType().get(minerType).get(0).getMaterial())
                        .name(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "autominer_type_" + minerType.toString().toLowerCase()))
                        .lore(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "autominer_click-to-change")), inventoryClickEvent1 -> {
                    autoMinerType = minerType;
                    inventoryClickEvent1.getWhoClicked().sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "autominer_changed-type",
                            plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "autominer_type_" + autoMinerType.toString().toLowerCase())));
                    inventoryClickEvent1.getWhoClicked().closeInventory();
                    setTypeSwitcher();
                });
            }
            simpleInventory.open((Player) inventoryClickEvent.getWhoClicked());
        });
    }

    public void save() {
        Inventory savedInventory = Bukkit.createInventory(null, 27);
        for(int i = 0; i < 18; i++) {
            savedInventory.setItem(i, inventory.getInventory().getItem(i));
        }
        plugin.getNexusPlugin().getDatabaseAdapter().updateTwo("autoMiners", "profileId",
                profile.getProfileId().toString(), "schematicId", builtSchematic.getSchematicId().toString(),
                new DatabaseUpdate("inventory", plugin.getNexusPlugin().getInventoryManager().toBase64(savedInventory)),
                new DatabaseUpdate("autoMinerType", autoMinerType.toString()));
    }
}
