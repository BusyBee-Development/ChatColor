package net.busybee.chatcolor.inventory.impl;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.data.PlayerColorData;
import fr.mrmicky.fastinv.FastInv;
import net.busybee.chatcolor.models.PatternEntry;
import net.busybee.chatcolor.models.SelectableEntry;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.PatternApplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ColorSelectorGUI extends FastInv {

    private final ChatColor plugin;
    private final String type;
    private int page;
    private List<? extends SelectableEntry> entries;

    public ColorSelectorGUI(ChatColor plugin, String type) {
        this(plugin, type, 0);
    }

    public ColorSelectorGUI(ChatColor plugin, String type, int page) {
        super(plugin.getGuiManager().getInt("layouts.selector.size", 54), buildTitle(plugin, type));
        this.plugin = plugin;
        this.type = type;
        this.page = page;
    }

    @Override
    public void open(Player player) {
        String perm = switch (this.type) {
            case "SOLID" -> "chatcolor.gui.solid";
            case "GRADIENT" -> "chatcolor.gui.gradient";
            case "PATTERN" -> "chatcolor.gui.pattern";
            default -> null;
        };

        if (perm != null && !player.hasPermission(perm)) {
            plugin.getMessageManager().send(player, "no-permission-command");
            return;
        }

        decorate(player);
        super.open(player);
    }

    private static Component buildTitle(ChatColor plugin, String type) {
        String key = switch (type) {
            case "SOLID" -> "titles.solid";
            case "GRADIENT" -> "titles.gradient";
            case "PATTERN" -> "titles.pattern";
            default -> "titles.default";
        };
        return plugin.getGuiManager().get(key);
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
        int contentSlots = plugin.getGuiManager().getInt("layouts.selector.content-slots", 45);
        int size = getEntries().size();
        return Math.max(1, (int) Math.ceil((double) size / contentSlots));
    }

    private void decorate(Player player) {
        getInventory().clear();
        int contentSlots = plugin.getGuiManager().getInt("layouts.selector.content-slots", 45);
        int navRowStart = plugin.getGuiManager().getInt("layouts.selector.nav-row-start", 45);
        int prevSlot = plugin.getGuiManager().getInt("layouts.selector.previous-page-slot", 45);
        int backSlot = plugin.getGuiManager().getInt("layouts.selector.back-menu-slot", 49);
        int nextSlot = plugin.getGuiManager().getInt("layouts.selector.next-page-slot", 53);
        int guiSize = plugin.getGuiManager().getInt("layouts.selector.size", 54);

        List<? extends SelectableEntry> all = getEntries();
        int start = this.page * contentSlots;
        int end = Math.min(start + contentSlots, all.size());

        ItemStack filler = plugin.getGuiManager().getFillerItem();
        for (int i = navRowStart; i < guiSize; i++) {
            setItem(i, filler);
        }

        for (int i = start; i < end; i++) {
            int slot = i - start;
            final SelectableEntry entry = all.get(i);
            final boolean hasPermission = player.hasPermission(entry.getPermission());
            final PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
            final boolean isSelected = entry.getKey().equalsIgnoreCase(data.getColorKey()) && entry.getEntryType().equalsIgnoreCase(data.getColorType());

            setItem(slot, createEntryIcon(entry, hasPermission, isSelected), event -> {
                Player clicker = (Player) event.getWhoClicked();
                if (!clicker.hasPermission(entry.getPermission())) {
                    plugin.getMessageManager().send(clicker, "no-permission");
                    return;
                }
                applyEntry(clicker, entry);
                clicker.closeInventory();
            });
        }

        if (this.page > 0) {
            setItem(prevSlot, createNavItem(
                            new ItemStack(plugin.getGuiManager().getMaterial("items.previous-page.material", Material.ARROW)),
                            plugin.getGuiManager().getRaw("items.previous-page.name"),
                            "items.previous-page.lore",
                            this.page,
                            getTotalPages()
                    ),
                    event -> {
                        this.page--;
                        decorate((Player) event.getWhoClicked());
                    }
            );
        } else {
            setItem(prevSlot, filler);
        }

        setItem(backSlot, createNavItem(
                        new ItemStack(plugin.getGuiManager().getMaterial("items.back-menu.material", Material.DARK_OAK_DOOR)),
                        plugin.getGuiManager().getRaw("items.back-menu.name"),
                        "items.back-menu.lore",
                        0, 0
                ),
                event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    new MainMenuGUI(plugin).open(clicker);
                }
        );

        if (this.page < getTotalPages() - 1) {
            setItem(nextSlot, createNavItem(
                            new ItemStack(plugin.getGuiManager().getMaterial("items.next-page.material", Material.ARROW)),
                            plugin.getGuiManager().getRaw("items.next-page.name"),
                            "items.next-page.lore",
                            this.page + 2,
                            getTotalPages()
                    ),
                    event -> {
                        this.page++;
                        decorate((Player) event.getWhoClicked());
                    }
            );
        } else {
            setItem(nextSlot, filler);
        }
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

        if (entry.getEntryType().equals("PATTERN")) {
            PatternEntry patternEntry = (PatternEntry) entry;
            Component coloredDisplay = PatternApplier.apply(entry.getDisplayName(), patternEntry.getColors());
            plugin.getMessageManager().send(player, "color-applied", "color", coloredDisplay);
        } else {
            String resetTag = plugin.getGuiManager().getRaw("settings.reset-tag");
            String coloredDisplay = entry.getTag() + entry.getDisplayName() + resetTag;
            plugin.getMessageManager().send(player, "color-applied", "color", coloredDisplay);
        }
    }

    private ItemStack createEntryIcon(SelectableEntry entry, boolean hasPermission, boolean isSelected) {
        Material mat = Material.matchMaterial(entry.getIconMaterial());
        if (mat == null) mat = Material.PAPER;
        ItemStack item = new ItemStack(mat);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String resetTag = plugin.getGuiManager().getRaw("settings.reset-tag");
        if (entry.getEntryType().equals("PATTERN")) {
            PatternEntry patternEntry = (PatternEntry) entry;
            meta.displayName(PatternApplier.applyToName(entry.getDisplayName(), patternEntry.getColors()));
        } else {
            meta.displayName(ColorUtil.colorize(entry.getTag() + entry.getDisplayName() + resetTag));
        }

        List<Component> lore = new ArrayList<>();
        if (isSelected) {
            lore.add(plugin.getGuiManager().get("status.selected"));
            lore.add(Component.empty());
            item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.BREACH, 1);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        if (hasPermission) {
            lore.add(plugin.getGuiManager().get("status.click-to-apply"));
            lore.add(Component.empty());
            lore.add(plugin.getGuiManager().get("status.has-access"));
        } else {
            lore.add(plugin.getGuiManager().get("status.no-access"));
            String permColor = plugin.getGuiManager().getRaw("settings.permission-color");
            lore.add(ColorUtil.colorize(permColor + entry.getPermission()));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavItem(ItemStack base, String name, String loreKey, int current, int total) {
        if (base == null || base.getType() == Material.AIR) base = new ItemStack(Material.PAPER);
        ItemMeta meta = base.getItemMeta();
        if (meta == null) return base;
        meta.displayName(ColorUtil.colorize(name));
        
        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("current", String.valueOf(current));
        placeholders.put("total", String.valueOf(total));
        
        meta.lore(plugin.getGuiManager().getList(loreKey, placeholders));
        base.setItemMeta(meta);
        return base;
    }
}
