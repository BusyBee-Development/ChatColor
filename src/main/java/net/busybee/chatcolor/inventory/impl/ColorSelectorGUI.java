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

    private static final int CONTENT_SLOTS = 45;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_BACK = 49;
    private static final int SLOT_NEXT = 53;
    private static final int NAV_ROW_START = 45;

    private final ChatColor plugin;
    private final String type;
    private int page;
    private List<? extends SelectableEntry> entries;

    public ColorSelectorGUI(ChatColor plugin, String type) {
        this(plugin, type, 0);
    }

    public ColorSelectorGUI(ChatColor plugin, String type, int page) {
        super(54, buildTitle(plugin, type));
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
            case "SOLID" -> "gui.titles.solid";
            case "GRADIENT" -> "gui.titles.gradient";
            case "PATTERN" -> "gui.titles.pattern";
            default -> "gui.titles.default";
        };
        return plugin.getMessageManager().get(key);
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

    private void decorate(Player player) {
        getInventory().clear();
        List<? extends SelectableEntry> all = getEntries();
        int start = this.page * CONTENT_SLOTS;
        int end = Math.min(start + CONTENT_SLOTS, all.size());

        ItemStack filler = createFiller();
        for (int i = NAV_ROW_START; i < 54; i++) {
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
            setItem(SLOT_PREV, createNavItem(
                            new ItemStack(Material.ARROW),
                            plugin.getMessageManager().getRaw("gui.items.previous-page.name"),
                            plugin.getMessageManager().getStringList("gui.items.previous-page.lore"),
                            this.page,
                            getTotalPages()
                    ),
                    event -> {
                        this.page--;
                        decorate((Player) event.getWhoClicked());
                    }
            );
        } else {
            setItem(SLOT_PREV, filler);
        }

        setItem(SLOT_BACK, createNavItem(
                        new ItemStack(Material.DARK_OAK_DOOR),
                        plugin.getMessageManager().getRaw("gui.items.back-menu.name"),
                        plugin.getMessageManager().getStringList("gui.items.back-menu.lore"),
                        0, 0
                ),
                event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    new MainMenuGUI(plugin).open(clicker);
                }
        );

        if (this.page < getTotalPages() - 1) {
            setItem(SLOT_NEXT, createNavItem(
                            new ItemStack(Material.ARROW),
                            plugin.getMessageManager().getRaw("gui.items.next-page.name"),
                            plugin.getMessageManager().getStringList("gui.items.next-page.lore"),
                            this.page + 2,
                            getTotalPages()
                    ),
                    event -> {
                        this.page++;
                        decorate((Player) event.getWhoClicked());
                    }
            );
        } else {
            setItem(SLOT_NEXT, filler);
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
            String coloredDisplay = entry.getTag() + entry.getDisplayName() + "<reset>";
            plugin.getMessageManager().send(player, "color-applied", "color", coloredDisplay);
        }
    }

    private ItemStack createEntryIcon(SelectableEntry entry, boolean hasPermission, boolean isSelected) {
        Material mat = Material.matchMaterial(entry.getIconMaterial());
        if (mat == null) mat = Material.PAPER;
        ItemStack item = new ItemStack(mat);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (entry.getEntryType().equals("PATTERN")) {
            PatternEntry patternEntry = (PatternEntry) entry;
            meta.displayName(PatternApplier.applyToName(entry.getDisplayName(), patternEntry.getColors()));
        } else {
            meta.displayName(ColorUtil.colorize(entry.getTag() + entry.getDisplayName() + "<reset>"));
        }

        List<Component> lore = new ArrayList<>();
        if (isSelected) {
            lore.add(plugin.getMessageManager().get("gui.status.selected"));
            lore.add(Component.empty());
            item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.BREACH, 1);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        if (hasPermission) {
            lore.add(plugin.getMessageManager().get("gui.status.click-to-apply"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("gui.status.has-access"));
        } else {
            lore.add(plugin.getMessageManager().get("gui.status.no-access"));
            lore.add(ColorUtil.colorize("<red>" + entry.getPermission()));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavItem(ItemStack base, String name, List<String> lore, int current, int total) {
        if (base == null || base.getType() == Material.AIR) base = new ItemStack(Material.PAPER);
        ItemMeta meta = base.getItemMeta();
        if (meta == null) return base;
        meta.displayName(ColorUtil.colorize(name));
        List<Component> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ColorUtil.colorize(line
                    .replace("<current>", String.valueOf(current))
                    .replace("<total>", String.valueOf(total))
            ));
        }
        meta.lore(loreList);
        base.setItemMeta(meta);
        return base;
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }
}
