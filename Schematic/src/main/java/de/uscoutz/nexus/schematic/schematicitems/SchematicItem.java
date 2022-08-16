package de.uscoutz.nexus.schematic.schematicitems;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.gamemechanics.NexusItem;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SchematicItem extends NexusItem {

    @Getter
    private Schematic schematic;
    private SchematicType schematicType;
    private int level;

    public SchematicItem(String key, ItemBuilder<ItemMeta> itemBuilder, NexusPlugin plugin, Schematic schematic) {
        super(key, itemBuilder, plugin);
        this.schematic = schematic;
        addPersistentData(new NamespacedKey(NexusPlugin.getInstance().getName().toLowerCase(), "schematictype"),
                PersistentDataType.STRING, schematic.getSchematicType().toString().toLowerCase());
        addPersistentData(new NamespacedKey(NexusPlugin.getInstance().getName().toLowerCase(), "schematiclevel"),
                PersistentDataType.INTEGER, schematic.getLevel());
    }
}
