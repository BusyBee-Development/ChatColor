package fr.mrmicky.fastinv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class FastInv implements InventoryHolder {

    private final Map<Integer, Consumer<InventoryClickEvent>> itemHandlers = new HashMap<>();
    private final List<Consumer<InventoryOpenEvent>> openHandlers = new ArrayList<>();
    private final List<Consumer<InventoryCloseEvent>> closeHandlers = new ArrayList<>();
    private final List<Consumer<InventoryClickEvent>> clickHandlers = new ArrayList<>();
    private final List<Consumer<InventoryDragEvent>> dragHandlers = new ArrayList<>();

    private final Inventory inventory;
    private Predicate<Player> closeFilter;
    public FastInv(int size) {
        this(owner -> Bukkit.createInventory(owner, size));
    }
    public FastInv(int size, String title) {
        this(owner -> Bukkit.createInventory(owner, size, title));
    }
    public FastInv(int size, Component title) {
        this(owner -> Bukkit.createInventory(owner, size, title));
    }
    public FastInv(InventoryType type) {
        this(owner -> Bukkit.createInventory(owner, type));
    }
    public FastInv(InventoryType type, String title) {
        this(owner -> Bukkit.createInventory(owner, type, title));
    }
    public FastInv(InventoryType type, Component title) {
        this(owner -> Bukkit.createInventory(owner, type, title));
    }

    public FastInv(Function<FastInv, Inventory> inventoryFunction) {
        Objects.requireNonNull(inventoryFunction, "inventoryFunction");
        Inventory inv = inventoryFunction.apply(this);

        if (inv.getHolder() != this) {
            throw new IllegalStateException("Inventory holder is not FastInv, found: " + inv.getHolder());
        }

        this.inventory = inv;
    }

    protected void onOpen(InventoryOpenEvent event) {
    }

    protected void onClick(InventoryClickEvent event) {
    }

    protected void onDrag(InventoryDragEvent event) {
    }

    protected void onClose(InventoryCloseEvent event) {
    }

    public void addItem(ItemStack item) {
        addItem(item, null);
    }

    public void addItem(ItemStack item, Consumer<InventoryClickEvent> handler) {
        int slot = this.inventory.firstEmpty();
        if (slot >= 0) {
            setItem(slot, item, handler);
        }
    }

    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, null);
    }

    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        this.inventory.setItem(slot, item);

        if (handler != null) {
            this.itemHandlers.put(slot, handler);
        } else {
            this.itemHandlers.remove(slot);
        }
    }

    public void setItems(int slotFrom, int slotTo, ItemStack item) {
        setItems(slotFrom, slotTo, item, null);
    }

    public void setItems(int slotFrom, int slotTo, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (int i = slotFrom; i < slotTo; i++) {
            setItem(i, item, handler);
        }
    }

    public void setItems(int[] slots, ItemStack item) {
        setItems(slots, item, null);
    }

    public void setItems(int[] slots, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (int slot : slots) {
            setItem(slot, item, handler);
        }
    }

    public void setItems(Iterable<Integer> slots, ItemStack item) {
        setItems(slots, item, null);
    }

    public void setItems(Iterable<Integer> slots, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (Integer slot : slots) {
            setItem(slot, item, handler);
        }
    }

    public void removeItem(int slot) {
        this.inventory.clear(slot);
        this.itemHandlers.remove(slot);
    }

    public void removeItems(int... slots) {
        for (int slot : slots) {
            removeItem(slot);
        }
    }

    public void clearItems() {
        this.inventory.clear();
        this.itemHandlers.clear();
    }

    public void setCloseFilter(Predicate<Player> closeFilter) {
        this.closeFilter = closeFilter;
    }
    public void addOpenHandler(Consumer<InventoryOpenEvent> openHandler) {
        this.openHandlers.add(openHandler);
    }
    public void addCloseHandler(Consumer<InventoryCloseEvent> closeHandler) {
        this.closeHandlers.add(closeHandler);
    }
    public void addClickHandler(Consumer<InventoryClickEvent> clickHandler) {
        this.clickHandlers.add(clickHandler);
    }
    public void addDragHandler(Consumer<InventoryDragEvent> dragHandler) {
        this.dragHandlers.add(dragHandler);
    }
    public void open(Player player) {
        Objects.requireNonNull(player, "player").openInventory(this.inventory);
    }

    public int[] getBorders() {
        int size = this.inventory.getSize();
        return IntStream.range(0, size).filter(i -> size < 27 || i < 9
                || i % 9 == 0 || (i - 8) % 9 == 0 || i > size - 9).toArray();
    }

    public int[] getCorners() {
        int size = this.inventory.getSize();
        return IntStream.range(0, size).filter(i -> i < 2 || (i > 6 && i < 10)
                || i == 17 || i == size - 18
                || (i > size - 11 && i < size - 7) || i > size - 3).toArray();
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    void handleOpen(InventoryOpenEvent e) {
        onOpen(e);

        this.openHandlers.forEach(c -> c.accept(e));
    }

    boolean handleClose(InventoryCloseEvent e) {
        onClose(e);

        this.closeHandlers.forEach(c -> c.accept(e));

        return this.closeFilter != null && this.closeFilter.test((Player) e.getPlayer());
    }

    void handleClick(InventoryClickEvent e) {
        onClick(e);

        this.clickHandlers.forEach(c -> c.accept(e));

        Consumer<InventoryClickEvent> clickConsumer = this.itemHandlers.get(e.getRawSlot());

        if (clickConsumer != null) {
            clickConsumer.accept(e);
        }
    }

    void handleDrag(InventoryDragEvent e) {
        onDrag(e);

        this.dragHandlers.forEach(c -> c.accept(e));
    }
}
