package de.uscoutz.nexus.gamemechanics;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.gamemechanics.tools.Tool;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class NexusItemManager {

    private NexusPlugin plugin;

    @Getter
    private LinkedHashMap<String, NexusItem> itemMap;

    public NexusItemManager(NexusPlugin plugin) {
        this.plugin = plugin;
        itemMap = new LinkedHashMap<>();
    }
    public void updateItem(ItemStack itemStack, String language) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        String key = dataContainer.get(new NamespacedKey(plugin.getName().toLowerCase(), "key"), PersistentDataType.STRING);
        NexusItem nexusItem = plugin.getNexusItemManager().getItemMap().get(key);

        List<Component> lore = new ArrayList<>();
        if(itemMeta.lore() != null) {
            itemMeta.lore().clear();
        }
        if(plugin.getToolManager().isTool(itemMeta)) {
            Tool tool = plugin.getToolManager().getToolMap().get(key);
            NamespacedKey namespacedKey = new NamespacedKey(plugin.getName().toLowerCase(), "breakingpower");
            int breakingPower = dataContainer.get(namespacedKey, PersistentDataType.INTEGER);
            int toolBreakingPower = tool.getBreakingPower();

            lore.add(Component.text(plugin.getLocaleManager().translate(
                    language, "tool_breaking-power", breakingPower)));
            if(breakingPower != toolBreakingPower) {
                itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, toolBreakingPower);
            }
        }
        if(nexusItem.getLocale() != null) {
            String displayName = plugin.getLocaleManager().translate(language, nexusItem.getLocale());
            itemMeta.displayName(Component.text(displayName));
        } else {
            itemMeta.displayName(Component.text(""));
        }

        lore.add(Component.text(""));
        lore.add(Component.text(nexusItem.getRarity().toString(language)));

        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        itemStack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }

    public boolean isNexusItem(ItemMeta itemMeta) {
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin.getName().toLowerCase(), "key");
        return dataContainer.has(namespacedKey);
    }
}
