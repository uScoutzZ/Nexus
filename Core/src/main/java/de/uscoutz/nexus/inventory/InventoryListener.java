package de.uscoutz.nexus.inventory;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;
import java.util.function.Consumer;

public class InventoryListener implements Listener {

    private NexusPlugin plugin;

    public InventoryListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        SimpleInventory inventory = InventoryBuilder.getInventories().get(event.getInventory());

        if (inventory == null) {
            return;
        }
        if (inventory.getInventoryCloseListener() != null) {
            inventory.getInventoryCloseListener().accept(event);
        }
        if(inventory.isDeleteOnClose()) {
            InventoryBuilder.getInventories().remove(event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        SimpleInventory inventory = InventoryBuilder.getInventories().get(event.getInventory());

        if (inventory == null) {
            return;
        }
        if(inventory.isClickEventCancelled()) {
            event.setCancelled(true);
        }
        if(event.getClickedInventory() == event.getWhoClicked().getInventory()) {
            return;
        }

        Map<Integer, Consumer<InventoryClickEvent>> handlers;
        if(event.getClick().isLeftClick()) {
            handlers = inventory.getLeftclickHandlers();
        } else {
            handlers = inventory.getRightclickHandlers();
            if(handlers.size() == 0 || !handlers.containsKey(event.getSlot())) {
                handlers = inventory.getLeftclickHandlers();
            }
        }

        if(inventory instanceof PaginatedInventory) {
            PaginatedInventory paginatedInventory = (PaginatedInventory) inventory;
            if(event.getSlot() == paginatedInventory.getPageSwitcherBackSlot()) {
                paginatedInventory.refresh(paginatedInventory.getPreviousPage(), (Player) event.getWhoClicked());
                return;
            }
            if(event.getSlot() == paginatedInventory.getPageSwitcherForwardSlot()) {
                paginatedInventory.refresh(paginatedInventory.getNextPage(), (Player) event.getWhoClicked());
                return;
            }
            int targetSlot = event.getSlot() + paginatedInventory.getOffsetForPage(paginatedInventory.getCurrentPage());
            Consumer<InventoryClickEvent> listener = handlers.get(targetSlot);
            if(listener == null) {
                return;
            }
            listener.accept(event);
        } else {
            Consumer<InventoryClickEvent> listener = handlers.get(event.getSlot());
            if(listener == null) {
                return;
            }
            listener.accept(event);
        }
    }
}