package de.uscoutz.nexus.inventory;

import de.uscoutz.nexus.item.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleInventory implements Cloneable{

    @Getter
    private final Inventory inventory;
    @Getter @Setter
    private boolean clickEventCancelled = true, deleteOnClose = true;
    @Getter
    private final Map<Integer, Consumer<InventoryClickEvent>> rightclickHandlers = new HashMap<>();
    @Getter
    private final Map<Integer, Consumer<InventoryClickEvent>> leftclickHandlers = new HashMap<>();
    @Getter
    private Consumer<InventoryCloseEvent> inventoryCloseListener = new Consumer<InventoryCloseEvent>() {
        @Override
        public void accept(InventoryCloseEvent event) {
        }
    };

    public SimpleInventory clone() throws CloneNotSupportedException {
        return (SimpleInventory) super.clone();
    }

    SimpleInventory(int size, String title) {
        inventory = Bukkit.createInventory(null, size, title);
    }

    SimpleInventory(int size) {
        inventory = Bukkit.createInventory(null, size);
    }

    SimpleInventory(InventoryType type) {
        inventory = Bukkit.createInventory(null, type);
    }

    SimpleInventory(InventoryType type, String title) {
        inventory = Bukkit.createInventory(null, type, title);
    }

    public SimpleInventory setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer) {
        this.inventory.setItem(slot, item);
        this.rightclickHandlers.put(slot, eventConsumer);
        return this;
    }

    public SimpleInventory setItem(int slot, ItemBuilder<?> item, Consumer<InventoryClickEvent> eventConsumer) {
        return setItem(slot, item.build(), eventConsumer);
    }

    public SimpleInventory setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer, Consumer<InventoryClickEvent> eventConsumer2) {
        this.inventory.setItem(slot, item);
        this.rightclickHandlers.put(slot, eventConsumer);
        this.leftclickHandlers.put(slot, eventConsumer2);
        return this;
    }

    public SimpleInventory setItem(int slot, ItemBuilder<?> item, Consumer<InventoryClickEvent> eventConsumer, Consumer<InventoryClickEvent> eventConsumer2) {
        return setItem(slot, item.build(), eventConsumer, eventConsumer2);
    }

    public SimpleInventory setItem(int slot, ItemStack item) {
        return setItem(slot, item, (ev) -> {
        });
    }

    public SimpleInventory setItem(int slot, ItemBuilder<?> item) {
        return setItem(slot, item, (ev) -> {
        });
    }

    public SimpleInventory addItem(ItemStack itemStack, Consumer<InventoryClickEvent> eventConsumer) {
        int slot = 0;
        for(int i=0; i<this.inventory.getSize(); i++) {
            if(inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                slot = i;
                break;
            }
        }
        return setItem(slot, itemStack, eventConsumer);
    }

    public SimpleInventory addItem(ItemBuilder<?> item, Consumer<InventoryClickEvent> eventConsumer) {
        return addItem(item.build(), eventConsumer);
    }

    public SimpleInventory addItem(ItemBuilder<?> item) {
        return addItem(item.build(), event -> {
        });
    }

    public SimpleInventory addItem(ItemStack itemStack) {
        return addItem(itemStack, event -> {
        });
    }

    public SimpleInventory fill(int beginning, int end, ItemStack item) {
        for (int i = beginning; i < end; i++) {
            setItem(i, item);
        }
        return this;
    }

    public SimpleInventory fill(int beginning, int end, ItemBuilder<?> item) {
        return fill(beginning, end, item.build());
    }

    public SimpleInventory setCloseListener(Consumer<InventoryCloseEvent> eventConsumer) {
        this.inventoryCloseListener = eventConsumer;
        return this;
    }

    public void open(Player... players) {
        for (Player player : players) {
            player.openInventory(inventory);
        }
    }
}