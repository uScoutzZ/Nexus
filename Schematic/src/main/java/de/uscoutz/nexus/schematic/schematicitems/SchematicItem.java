package de.uscoutz.nexus.schematic.schematicitems;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.gamemechanics.NexusItem;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SchematicItem extends NexusItem {

    @Getter
    private Schematic schematic;
    @Getter
    private boolean obtainable;
    @Getter @Setter
    private Task task;
    @Getter @Setter
    private int maxObtainable, requiredLevel;
    private NexusSchematicPlugin plugin;

    public SchematicItem(String key, ItemBuilder<ItemMeta> itemBuilder, NexusSchematicPlugin plugin, Schematic schematic, boolean obtainable, String path) {
        super(key, itemBuilder, plugin.getNexusPlugin(), path);
        this.schematic = schematic;
        this.plugin = plugin;
        this.obtainable = obtainable;
        addPersistentData(new NamespacedKey(plugin.getNexusPlugin().getName().toLowerCase(), "schematictype"),
                PersistentDataType.STRING, schematic.getSchematicType().toString().toLowerCase());
        addPersistentData(new NamespacedKey(plugin.getNexusPlugin().getName().toLowerCase(), "schematiclevel"),
                PersistentDataType.INTEGER, schematic.getLevel());
    }

    @Override
    public ItemStack getItemStack() {
        return getItemStack(UUID.randomUUID());
    }

    public ItemStack getItemStack(UUID uuid) {
        ItemStack itemStack = super.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();

        NamespacedKey key = new NamespacedKey(plugin.getNexusPlugin().getName().toLowerCase(), "schematicid");
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, uuid.toString());

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
