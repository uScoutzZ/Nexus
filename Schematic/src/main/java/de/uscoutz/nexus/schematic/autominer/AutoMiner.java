package de.uscoutz.nexus.schematic.autominer;

import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.inventory.InventoryBuilder;
import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import lombok.Getter;
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
            for(int i = 0; i < 3*9; i++) {
                if(player.getInventory().getItem(i) == null) {
                    canAdd = 64;
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
            inventory = InventoryBuilder.create(18, "AutoMiner");
            inventory.setDeleteOnClose(false);
            for(int i = 0; i < 18; i++) {
                inventory.setItem(i, new ItemStack(Material.AIR), eventConsumer);
            }
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
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if(builtSchematic.isBuilt()) {
                    List<Material> list = plugin.getAutoMinerManager().getMaterialsPerType().get(autoMinerType);
                    inventory.getInventory().addItem(new ItemStack(list.get((int) (Math.random() * list.size()))));
                }
            }
        }.runTaskTimer(plugin, 0, 100);
    }

    public void save() {
        plugin.getNexusPlugin().getDatabaseAdapter().updateTwo("autoMiners", "profileId",
                profile.getProfileId().toString(), "schematicId", builtSchematic.getSchematicId().toString(),
                new DatabaseUpdate("inventory", plugin.getNexusPlugin().getInventoryManager().toBase64(inventory.getInventory())),
                new DatabaseUpdate("autoMinerType", autoMinerType.toString()));
    }
}
