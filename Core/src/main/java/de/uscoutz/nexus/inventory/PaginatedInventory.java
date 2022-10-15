package de.uscoutz.nexus.inventory;

import de.uscoutz.nexus.inventory.exceptions.PaginatedInventoryException;
import de.uscoutz.nexus.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class PaginatedInventory extends SimpleInventory {
    private static final ItemStack ARROW_LEFT_ITEM = ItemBuilder.skull()
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY5NzFkZDg4MWRiYWY0ZmQ2YmNhYTkzNjE0NDkzYzYxMmY4Njk2NDFlZDU5ZDFjOTM2M2EzNjY2YTVmYTYifX19")
            .name("§e<<")
            .build();
    private static final ItemStack ARROW_RIGHT_ITEM = ItemBuilder.skull()
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0=")
            .name("§e>>")
            .build();
    private int pageSwitcherBackSlot = -1;
    private int pageSwitcherForwardSlot = -1;
    private final LinkedHashMap<Integer, ItemStack> inventoryContents;
    private final List<Integer> dynamicSlots = new ArrayList<>();
    private int currentPage;

    PaginatedInventory(int size, String title, int... dynamicSlots) {
        super(size, title);
        this.currentPage = 1;
        this.inventoryContents = new LinkedHashMap<>();
        for(int i : dynamicSlots) {
            this.dynamicSlots.add(i);
        }
        Collections.sort(this.dynamicSlots);
        if (this.dynamicSlots.size() == 0) {
            return;
        }
        if (this.dynamicSlots.get(0) < 0) {
            throw new PaginatedInventoryException("Dynamic slot out of range: value for a dynamic slot can not be less than 0");
        }
        if (this.dynamicSlots.get(this.dynamicSlots.size() - 1) >= getInventory().getSize()) {
            throw new PaginatedInventoryException("Dynamic slot out of range: value for dynamic slot can not be greater than the size of the inventory");
        }
    }

    public LinkedHashMap<Integer, ItemStack> getInventoryContents() {
        return inventoryContents;
    }

    public List<Integer> getDynamicSlots() {
        return dynamicSlots;
    }

    public void addDynamicSlots(Integer... slots) {
        dynamicSlots.addAll(Arrays.asList(slots));
    }

    public void addDynamicSlots(List<Integer> slots) {
        dynamicSlots.addAll(slots);
    }

    public void addDynamicSlots(int[] slots) {
        for (int slot : slots) {
            dynamicSlots.add(slot);
        }
    }

    public void removeDynamicSlots(Integer... slots) {
        getDynamicSlots().removeAll(Arrays.asList(slots));
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getMaxPage() {
        return getPageForIndex(getMaxItemIndex());
    }

    public int getNextPage() {
        return Math.min(getCurrentPage() + 1, getMaxPage());
    }

    public int getPreviousPage() {
        return Math.max(getCurrentPage() - 1, 1);
    }

    public int getPageSwitcherBackSlot() {
        return pageSwitcherBackSlot;
    }

    public PaginatedInventory setPageSwitcherBackSlot(int pageSwitcherBackSlot) {
        if (getPageSwitcherBackSlot() != -1) {
            getInventory().setItem(getPageSwitcherBackSlot(), ItemBuilder.create(Material.AIR).build());
        }
        this.pageSwitcherBackSlot = pageSwitcherBackSlot;
        if (pageSwitcherBackSlot != -1) {
            getInventory().setItem(pageSwitcherBackSlot, ARROW_LEFT_ITEM);
        }
        return this;
    }

    public int getPageSwitcherForwardSlot() {
        return pageSwitcherForwardSlot;
    }

    public PaginatedInventory setPageSwitcherForwardSlot(int pageSwitcherForwardSlot) {
        if (getPageSwitcherForwardSlot() != -1) {
            getInventory().setItem(getPageSwitcherForwardSlot(), ItemBuilder.create(Material.AIR).build());
        }
        this.pageSwitcherForwardSlot = pageSwitcherForwardSlot;
        if (pageSwitcherForwardSlot != -1) {
            getInventory().setItem(pageSwitcherForwardSlot, ARROW_RIGHT_ITEM);
        }
        return this;
    }

    public int getPageForIndex(int index) {
        if (index < 0) {
            return 1;
        }
        return (int) Math.ceil((index + 1) / (float) getDynamicSlots().size());
    }

    public int getMaxItemIndex() {
        OptionalInt optionalHighestItemSlot = getInventoryContents().keySet().stream().mapToInt(Integer::intValue).max();
        if (!optionalHighestItemSlot.isPresent())
            return -1;
        return optionalHighestItemSlot.getAsInt();
    }

    public int getOffsetForPage(int page) {
        return (page - 1) * getInventory().getSize();
    }

    public PaginatedInventory setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        return this;
    }

    public PaginatedInventory setItem(int page, int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer) {
        if (page < 1) {
            throw new PaginatedInventoryException("Page number can not be less than 1");
        }
        if (!getDynamicSlots().contains(slot)) {
            super.setItem(slot, item, eventConsumer);
            return this;
        }
        int targetSlot = slot + getOffsetForPage(page);
        this.inventoryContents.put(targetSlot, item);
        super.getLeftclickHandlers().put(targetSlot, eventConsumer);
        return this;
    }

    @Override
    public PaginatedInventory setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer) {
        return this.setItem(getPageForIndex(slot), slot, item, eventConsumer);
    }

    public PaginatedInventory setItem(int page, int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer, Consumer<InventoryClickEvent> eventConsumer2) {
        if (page < 1) {
            throw new PaginatedInventoryException("Page number can not be less than 1");
        }
        if (!getDynamicSlots().contains(slot)) {
            super.setItem(slot, item, eventConsumer);
            return this;
        }
        int targetSlot = slot + getOffsetForPage(page);
        this.inventoryContents.put(targetSlot, item);
        super.getRightclickHandlers().put(targetSlot, eventConsumer);
        super.getLeftclickHandlers().put(targetSlot, eventConsumer2);
        return this;
    }

    @Override
    public PaginatedInventory setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer, Consumer<InventoryClickEvent> eventConsumer2) {
        return this.setItem(getPageForIndex(slot), slot, item, eventConsumer, eventConsumer2);
    }

    @Override
    public PaginatedInventory setItem(int slot, ItemBuilder<?> item, Consumer<InventoryClickEvent> eventConsumer) {
        return this.setItem(getPageForIndex(slot), slot, item.build(), eventConsumer);
    }

    @Override
    public PaginatedInventory setItem(int slot, ItemStack item) {
        return this.setItem(getPageForIndex(slot), slot, item, event -> {
        });
    }

    @Override
    public PaginatedInventory setItem(int slot, ItemBuilder<?> item) {
        return this.setItem(getPageForIndex(slot), slot, item.build(), event -> {
        });
    }

    public PaginatedInventory addItem(ItemStack item, Consumer<InventoryClickEvent> eventConsumer) {
        int[] inventoryPages = IntStream.range(1, getMaxPage() + 1).toArray();
        for (int inventoryPage : inventoryPages) {
            int offset = getOffsetForPage(inventoryPage);
            for (int dynamicSlot : dynamicSlots) {
                int targetSlot = dynamicSlot + offset;
                if (getInventoryContents().get(targetSlot) == null) {
                    return this.setItem(inventoryPage, dynamicSlot, item, eventConsumer);
                }
            }
        }
        int nextPage = getMaxPage() + 1;
        return this.setItem(nextPage, 0, item, eventConsumer);
    }

    public PaginatedInventory addItem(ItemBuilder<?> item, Consumer<InventoryClickEvent> eventConsumer) {
        return this.addItem(item.build(), eventConsumer);
    }

    public PaginatedInventory addItem(ItemStack item) {
        return this.addItem(item, event -> {
        });
    }

    public PaginatedInventory addItem(ItemBuilder<?> item) {
        return this.addItem(item.build(), event -> {
        });
    }

    public void refresh(int newPage, Player player) {
        if (newPage > getMaxPage() || newPage < 1) {
            return;
        }
        int offset = setCurrentPage(newPage).getOffsetForPage(newPage);
        getDynamicSlots().forEach(dynamicSlot -> getInventory().setItem(dynamicSlot, getInventoryContents().get(dynamicSlot + offset)));
        player.updateInventory();
    }

    public void open(int page, Player player) {
        if (page < 1) {
            throw new PaginatedInventoryException("Page number (" + page + ") can not be less than 1");
        }
        if (page > getMaxPage()) {
            throw new PaginatedInventoryException("Page number (" + page + ") can not be greater than the maximum amount of pages (currently " + getMaxPage() + ")");
        }
        player.openInventory(getInventory());
        new Thread(() -> {
            int offset = setCurrentPage(page).getOffsetForPage(page);
            getDynamicSlots().forEach(dynamicSlot -> getInventory().setItem(dynamicSlot, getInventoryContents().get(dynamicSlot + offset)));
            player.updateInventory();
        }).start();
    }

    @Override
    public void open(Player... players) {
        if (players.length > 1) {
            throw new PaginatedInventoryException("A paginated inventory can not be opened for more than one player");
        }
        if (players[0] != null) {
            this.open(1, players[0]);
        }
    }
}