package net.busybee.chatcolor.inventory.impl;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.data.PlayerColorData;
import net.busybee.chatcolor.inventory.InventoryButton;
import net.busybee.chatcolor.inventory.InventoryGUI;
import net.busybee.chatcolor.models.PatternEntry;
import net.busybee.chatcolor.models.SelectableEntry;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.PatternApplier;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ColorSelectorGUI extends InventoryGUI {

    private static final int CONTENT_SLOTS = 45;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_BACK = 49;
    private static final int SLOT_NEXT = 53;
    private static final int NAV_ROW_START = 45;

    private final ChatColor plugin;
    private final String type;
    private int page = 0;
    private List<? extends SelectableEntry> entries;

    public ColorSelectorGUI(ChatColor plugin, String type) {
        this.plugin = plugin;
        this.type = type;
    }

    private List<? extends SelectableEntry> getEntries() {
        if (this.entries == null) {
            switch (this.type) {
                case "SOLID" -> this.entries = plugin.getConfigManager().getColorList();
                case "GRADIENT" -> this.entries = plugin.getConfigManager().getGradientList();
                case "PATTERN" -> this.entries = plugin.getPatternManager().getPatternList();
                default -> this.entries = new ArrayList<>();
            }
        }
        return this.entries;
    }

    private int getTotalPages() {
        int size = getEntries().size();
        return Math.max(1, (int) Math.ceil((double) size / CONTENT_SLOTS));
    }

    @Override
    protected Inventory createInventory() {
        Component title = buildTitle();
        return Bukkit.createInventory(null, 54, title);
    }

    private Component buildTitle() {
        String raw = switch (this.type) {
            case "SOLID" -> plugin.getConfigManager().getColorSelectorTitle();
            case "GRADIENT" -> plugin.getConfigManager().getGradientSelectorTitle();
            case "PATTERN" -> plugin.getConfigManager().getPatternSelectorTitle();
            default -> "<white>Colors";
        };
        return ColorUtil.colorize(raw);
    }

    @Override
    public void decorate(Player player) {
        getInventory().clear();
        clearButtons();

        List<? extends SelectableEntry> all = getEntries();
        int start = this.page * CONTENT_SLOTS;
        int end = Math.min(start + CONTENT_SLOTS, all.size());

        ItemStack filler = createFiller();
        for (int i = NAV_ROW_START; i < 54; i++) {
            getInventory().setItem(i, filler);
        }

        for (int i = start; i < end; i++) {
            int slot = i - start;
            final SelectableEntry entry = all.get(i);
            final boolean hasPermission = player.hasPermission(entry.getPermission());

            addButton(slot, new InventoryButton()
                    .creator(p -> createEntryIcon(entry, hasPermission))
                    .consumer(event -> {
                        Player clicker = (Player) event.getWhoClicked();
                        if (!clicker.hasPermission(entry.getPermission())) {
                            plugin.getMessageManager().send(clicker, "no-permission");
                            return;
                        }
                        applyEntry(clicker, entry);
                        clicker.closeInventory();
                    })
            );
        }

        if (this.page > 0) {
            addButton(SLOT_PREV, new InventoryButton()
                    .creator(p -> createNavItem(
                            XMaterial.matchXMaterial("ARROW").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.ARROW.parseMaterial())),
                            "<yellow><bold>← Previous Page",
                            "<gray>Page " + this.page + " / " + getTotalPages()
                    ))
                    .consumer(event -> {
                        this.page--;
                        decorate((Player) event.getWhoClicked());
                    })
            );
        }

        addButton(SLOT_BACK, new InventoryButton()
                .creator(p -> createNavItem(
                        XMaterial.matchXMaterial("DARK_OAK_DOOR").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.DARK_OAK_DOOR.parseMaterial())),
                        "<red><bold>← Back to Main Menu",
                        "<gray>Return to the color category menu."
                ))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.closeInventory();
                    MainMenuGUI mainMenu = new MainMenuGUI(plugin);
                    plugin.getGuiManager().openGUI(mainMenu, clicker);
                })
        );

        if (this.page < getTotalPages() - 1) {
            addButton(SLOT_NEXT, new InventoryButton()
                    .creator(p -> createNavItem(
                            XMaterial.matchXMaterial("ARROW").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.ARROW.parseMaterial())),
                            "<yellow><bold>Next Page →",
                            "<gray>Page " + (this.page + 2) + " / " + getTotalPages()
                    ))
                    .consumer(event -> {
                        this.page++;
                        decorate((Player) event.getWhoClicked());
                    })
            );
        }

        super.decorate(player);
    }

    private void applyEntry(Player player, SelectableEntry entry) {
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        if (entry.getEntryType().equals("PATTERN")) {
            data.setColorType("PATTERN");
            data.setColorKey(entry.getKey());
            data.setColorTag(null);
        } else {
            data.setColorType(entry.getEntryType());
            data.setColorKey(entry.getKey());
            data.setColorTag(entry.getTag());
        }

        plugin.getPlayerDataManager().setData(player.getUniqueId(), data);
        plugin.getPlayerDataManager().save(player.getUniqueId());

        String coloredDisplay = entry.getEntryType().equals("PATTERN") ? entry.getDisplayName() : entry.getTag() + entry.getDisplayName() + "<reset>";
        plugin.getMessageManager().send(player, "color-applied", "color", coloredDisplay);
    }

    private ItemStack createEntryIcon(SelectableEntry entry, boolean hasPermission) {
        ItemStack item = XMaterial.matchXMaterial(entry.getIconMaterial())
                .map(XMaterial::parseItem)
                .orElse(XMaterial.matchXMaterial("PAPER").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.PAPER.parseMaterial())));

        if (item == null) item = new ItemStack(XMaterial.PAPER.parseMaterial());

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (entry.getEntryType().equals("PATTERN")) {
            PatternEntry patternEntry = (PatternEntry) entry;
            meta.displayName(PatternApplier.applyToName(entry.getDisplayName(), patternEntry.getColors()));
        } else {
            meta.displayName(ColorUtil.colorize(entry.getTag() + entry.getDisplayName() + "<reset>"));
        }

        List<Component> lore = new ArrayList<>();
        if (hasPermission) {
            lore.add(ColorUtil.colorize("<gray>Click to apply this color."));
            lore.add(Component.empty());
            lore.add(ColorUtil.colorize("<green>✔ <gray>You have access."));
        } else {
            lore.add(ColorUtil.colorize("<gray>You need permission:"));
            lore.add(ColorUtil.colorize("<red>" + entry.getPermission()));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavItem(ItemStack base, String name, String lore) {
        if (base == null) base = XMaterial.matchXMaterial("PAPER").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.PAPER.parseMaterial()));
        ItemMeta meta = base.getItemMeta();
        if (meta == null) return base;
        meta.displayName(ColorUtil.colorize(name));
        List<Component> loreList = new ArrayList<>();
        loreList.add(ColorUtil.colorize(lore));
        meta.lore(loreList);
        base.setItemMeta(meta);
        return base;
    }

    private ItemStack createFiller() {
        ItemStack item = XMaterial.matchXMaterial("GRAY_STAINED_GLASS_PANE")
                .map(XMaterial::parseItem)
                .orElse(new ItemStack(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }
}