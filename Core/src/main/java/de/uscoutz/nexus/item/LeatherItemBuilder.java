package de.uscoutz.nexus.item;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherItemBuilder extends ItemBuilder<LeatherArmorMeta> {

    public LeatherItemBuilder(ItemStack itemStack) {
        super(itemStack);
    }

    public LeatherItemBuilder dye(Color color) {
        getMeta().setColor(color);
        return this;
    }

}