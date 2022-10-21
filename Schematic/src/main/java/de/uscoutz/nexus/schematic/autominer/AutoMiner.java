package de.uscoutz.nexus.schematic.autominer;

import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class AutoMiner {

    private NexusSchematicPlugin plugin;

    private final Profile profile;
    private final BuiltSchematic builtSchematic;
    @Getter
    private Inventory inventory;
    private AutoMinerManager.AutoMinerType autoMinerType;

    public AutoMiner(BuiltSchematic builtSchematic, NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        this.builtSchematic = builtSchematic;
        profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(builtSchematic.getLocation().getWorld());

        ResultSet resultSet = plugin.getNexusPlugin().getDatabaseAdapter().getTwoAsync("autoMiners", "profileId", profile.getProfileId().toString(), "schematicId", builtSchematic.getSchematicId().toString());
        try {
            if (resultSet.next()) {
                inventory = plugin.getNexusPlugin().getInventoryManager().fromBase64(resultSet.getString("inventory"));
                autoMinerType = AutoMinerManager.AutoMinerType.valueOf(resultSet.getString("autoMinerType"));
            } else {
                inventory = plugin.getNexusPlugin().getServer().createInventory(null, 18, "AutoMiner");
                autoMinerType = AutoMinerManager.AutoMinerType.STONE;
                plugin.getNexusPlugin().getDatabaseAdapter().setAsync("autoMiners", profile.getProfileId().toString(), builtSchematic.getSchematicId().toString(), autoMinerType.toString(), plugin.getNexusPlugin().getInventoryManager().toBase64(inventory));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if(builtSchematic.isBuilt()) {
                    List<Material> list = plugin.getAutoMinerManager().getMaterialsPerType().get(autoMinerType);
                    inventory.addItem(new ItemStack(list.get((int) (Math.random() * list.size()))));
                }
            }
        }.runTaskTimer(plugin, 0, 100);
    }

    public void save() {
        plugin.getNexusPlugin().getDatabaseAdapter().updateTwo("autoMiners", "profileId",
                profile.getProfileId().toString(), "schematicId", builtSchematic.getSchematicId().toString(),
                new DatabaseUpdate("inventory", plugin.getNexusPlugin().getInventoryManager().toBase64(inventory)),
                new DatabaseUpdate("autoMinerType", autoMinerType.toString()));
    }
}
