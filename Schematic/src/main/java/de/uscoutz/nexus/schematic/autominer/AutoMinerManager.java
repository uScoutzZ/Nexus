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
    private Map<AutoMinerType, List<ItemProbability>> materialsPerType;

    public AutoMinerManager(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        autoMinersPerProfile = new HashMap<>();
        materialsPerType = new HashMap<>();
    }

    public enum AutoMinerType {
        STONE(Arrays.asList(new ItemProbability(Material.COBBLESTONE, 70),
                new ItemProbability(Material.COAL, 20),
                new ItemProbability(Material.IRON_ORE, 10))),
        WOOD(Arrays.asList(new ItemProbability(Material.DARK_OAK_LOG, 50),
                new ItemProbability(Material.ACACIA_LOG, 20),
                new ItemProbability(Material.BIRCH_LOG, 10),
                new ItemProbability(Material.JUNGLE_LOG, 10),
                new ItemProbability(Material.OAK_LOG, 5),
                new ItemProbability(Material.SPRUCE_LOG, 5)));

        @Getter
        private List<ItemProbability> materials;

        AutoMinerType(List<ItemProbability> materials) {
            NexusSchematicPlugin.getInstance().getAutoMinerManager().materialsPerType.put(this, materials);
            this.materials = materials;
        }

        public ItemProbability getRandomMaterial() {
            Random rand = new Random();
            int totalSum = 0;

            for(ItemProbability item : materials) {
                totalSum = totalSum + item.getProbability();
            }

            int index = rand.nextInt(totalSum);
            int sum = 0;
            int i=0;
            while(sum < index ) {
                sum = sum + materials.get(i++).getProbability();
            }
            return materials.get(Math.max(0,i-1));

        }
    }
}

class ItemProbability {
    @Getter
    private Material material;
    @Getter
    private int probability;

    public ItemProbability(Material material, int chance) {
        this.material = material;
        this.probability = chance;
    }
}
