package de.uscoutz.nexus.gamemechanics;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.gamemechanics.shops.ItemPrice;
import de.uscoutz.nexus.gamemechanics.shops.MoneyPrice;
import de.uscoutz.nexus.gamemechanics.shops.NexusPrice;
import de.uscoutz.nexus.item.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class NexusItem {

    private NexusPlugin plugin;

    @Getter
    private ItemBuilder<ItemMeta> itemBuilder;
    @Getter
    private ItemStack itemStack;
    @Getter
    private String key, locale;
    @Getter @Setter
    private List<ItemStack> ingredients;
    @Getter @Setter
    private int moneyPrice;

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

    public NexusPrice[] getPrices() {
        List<NexusPrice> prices = new ArrayList<>();
        if(moneyPrice != 0) {
            prices.add(new MoneyPrice(moneyPrice, plugin));
        }
        if(ingredients != null && ingredients.size() != 0) {
            prices.add(new ItemPrice(ingredients, plugin));
        }

        return prices.toArray(new NexusPrice[prices.size()]);
    }
}
