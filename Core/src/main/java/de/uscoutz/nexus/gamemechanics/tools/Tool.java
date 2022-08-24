package de.uscoutz.nexus.gamemechanics.tools;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.gamemechanics.NexusItem;
import de.uscoutz.nexus.item.ItemBuilder;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Tool extends NexusItem {

    @Getter
    private int breakingPower;

    private NexusPlugin nexusPlugin;

    public Tool(String key, ItemBuilder<ItemMeta> itemBuilder, NexusPlugin plugin) {
        super(key, itemBuilder, plugin);
        nexusPlugin = plugin;
    }

    public Tool breakingPower(int breakingPower) {
        this.breakingPower = breakingPower;
        getItemBuilder().lore(nexusPlugin.getLocaleManager().translate("de_DE", "tool_breaking-power", breakingPower));
        NamespacedKey key = new NamespacedKey(nexusPlugin.getName().toLowerCase(), "breakingpower");
        addPersistentData(key, PersistentDataType.INTEGER, breakingPower);
        return this;
    }
}
