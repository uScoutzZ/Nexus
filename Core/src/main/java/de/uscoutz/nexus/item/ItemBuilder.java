package de.uscoutz.nexus.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder<T extends ItemMeta> {

    private final ItemStack itemStack;
    final T meta;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = (T) itemStack.getItemMeta();
    }

    public static ItemBuilder<ItemMeta> create(Material material) {
        return new ItemBuilder<>(new ItemStack(material));
    }

    public static ItemBuilder<ItemMeta> create(Material material, short durability) {
        return new ItemBuilder<>(new ItemStack(material, 1, durability));
    }

    public static LeatherItemBuilder leather(Material material) {
        if (!material.name().startsWith("LEATHER_")) {
            throw new IllegalArgumentException("leather() must be called with a valid leather armor part!");
        }
        return new LeatherItemBuilder(new ItemStack(material));
    }

    public static SkullItemBuilder skull() {
        return new SkullItemBuilder(new ItemStack(Material.PLAYER_HEAD, 1, (short) 3));
    }

    public ItemBuilder<T> amount(int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder<T> name(String name) {
        this.meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder<T> lore(List<String> lore) {
        if(meta.getLore() != null) {
            List<String> newLore = this.meta.getLore();
            newLore.addAll(lore);
            this.meta.setLore(newLore);
        } else {
            this.meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder<T> lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder<T> lore(String lore) {
        return lore(lore.split("\n"));
    }

    public ItemBuilder<T> enchant(Enchantment enchantment, int lvl) {
        getMeta().addEnchant(enchantment, lvl, true);
        return this;
    }

    public ItemBuilder<T> flag(ItemFlag... flags) {
        getMeta().addItemFlags(flags);
        return this;
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(meta);
        return this.itemStack;
    }

    public ItemBuilder<T> unbreakable() {
        this.meta.setUnbreakable(true);
        return this;
    }

    T getMeta() {
        return meta;
    }
}
