package de.uscoutz.nexus.gamemechanics;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.LinkedHashMap;

public class NexusItemManager {

    private NexusPlugin plugin;

    @Getter
    private LinkedHashMap<String, NexusItem> itemMap;

    public NexusItemManager(NexusPlugin plugin) {
        this.plugin = plugin;
        itemMap = new LinkedHashMap<>();
    }

    public boolean isNexusItem(ItemMeta itemMeta) {
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin.getName().toLowerCase(), "key");
        return dataContainer.has(namespacedKey);
    }
}
