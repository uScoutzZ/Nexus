package de.uscoutz.nexus.gamemechanics;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.item.ItemBuilder;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NexusItem {

    private NexusPlugin plugin;

    @Getter
    private ItemBuilder<ItemMeta> itemBuilder;
    @Getter
    private ItemStack itemStack;
    @Getter
    private String key, locale;

    public NexusItem(String key, ItemBuilder<ItemMeta> itemBuilder, NexusPlugin plugin) {
        this.key = key;
        this.itemBuilder = itemBuilder;
        this.plugin = plugin;
    }

    public NexusItem name(String locale) {
        this.locale = locale;
        itemBuilder.name(plugin.getLocaleManager().translate("de_DE", locale));
        return this;
    }

    public void build() {
        NamespacedKey key = new NamespacedKey(plugin.getName().toLowerCase(), "key");
        addPersistentData(key, PersistentDataType.STRING, this.key);
        itemStack = itemBuilder.build();
    }

    public ItemBuilder<ItemMeta> addPersistentData(NamespacedKey key, PersistentDataType type, Object value) {
        ItemMeta meta = itemBuilder.build().getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, type, value);
        itemBuilder.setMeta(meta);
        return itemBuilder;
    }
}
