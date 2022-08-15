package de.uscoutz.nexus.gamemechanics.tools;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.item.ItemBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Tool {

    private NexusPlugin plugin;

    private String key;
    private ItemBuilder itemBuilder;

    @Getter
    private ItemStack itemStack;
    @Getter
    private int breakingPower;

    public Tool(String key, ItemBuilder itemBuilder, NexusPlugin plugin) {
        this.key = key;
        this.itemBuilder = itemBuilder;
        this.plugin = plugin;
    }

    public Tool breakingPower(int breakingPower) {
        this.breakingPower = breakingPower;
        itemBuilder.lore(plugin.getLocaleManager().translate("de_DE", "tool_breaking-power", breakingPower));
        Bukkit.getConsoleSender().sendMessage(key + " " + breakingPower);
        NamespacedKey key = new NamespacedKey(plugin.getName(), "breakingpower");
        addPersistentData(key, PersistentDataType.INTEGER, breakingPower);
        return this;
    }

    public void build() {
        NamespacedKey key = new NamespacedKey(plugin.getName(), "key");
        addPersistentData(key, PersistentDataType.STRING, key);
        itemStack = itemBuilder.build();
        plugin.getToolManager().getToolMap().put(this.key, this);
    }

    private ItemBuilder addPersistentData(NamespacedKey key, PersistentDataType type, Object value) {
        ItemMeta meta = itemBuilder.build().getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, type, value);
        itemBuilder.setMeta(meta);
        return itemBuilder;
    }
}
