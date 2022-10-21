package de.uscoutz.nexus.schematic.autominer;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;
import org.bukkit.Material;

import java.util.*;

public class AutoMinerManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private Map<UUID, Map<UUID, AutoMiner>> autoMinersPerProfile;
    @Getter
    private Map<AutoMinerType, List<Material>> materialsPerType;

    public AutoMinerManager(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        autoMinersPerProfile = new HashMap<>();
        materialsPerType = new HashMap<>();
    }

    public enum AutoMinerType {
        STONE(Arrays.asList(Material.COBBLESTONE, Material.COAL, Material.IRON_ORE)),
        WOOD(Arrays.asList(Material.DARK_OAK_LOG, Material.ACACIA_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG));

        AutoMinerType(List<Material> materials) {
            NexusSchematicPlugin.getInstance().getAutoMinerManager().materialsPerType.put(this, materials);
        }
    }
}
