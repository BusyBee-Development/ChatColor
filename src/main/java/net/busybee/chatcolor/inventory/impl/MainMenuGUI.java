package net.busybee.chatcolor.inventory.impl;

import net.busybee.chatcolor.ChatColor;
import fr.mrmicky.fastinv.FastInv;
import net.busybee.chatcolor.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainMenuGUI extends FastInv {

    private static final int SIZE = 27;
    private static final int[] FILLER_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 11, 13, 15, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26
    };

    private final ChatColor plugin;

    public MainMenuGUI(ChatColor plugin) {
        super(SIZE, plugin.getMessageManager().get("gui.titles.main"));
        this.plugin = plugin;
    }

    @Override
    public void open(Player player) {
        decorate(player);
        super.open(player);
    }

    private void decorate(Player player) {
        ItemStack filler = createFiller();
        for (int slot : FILLER_SLOTS) {
            setItem(slot, filler);
        }

        if (player.hasPermission("chatcolor.gui.solid")) {
            setItem(10, createItem(
                            new ItemStack(Material.LIME_DYE),
                            plugin.getMessageManager().getRaw("gui.items.solid.name"),
                            plugin.getMessageManager().getStringList("gui.items.solid.lore").toArray(new String[0])
                    ),
                    event -> {
                        Player clicker = (Player) event.getWhoClicked();
                        new ColorSelectorGUI(plugin, "SOLID").open(clicker);
                    }
            );
        } else {
            setItem(10, filler);
        }

        if (player.hasPermission("chatcolor.gui.gradient")) {
            setItem(12, createItem(
                            new ItemStack(Material.MAGMA_CREAM),
                            plugin.getMessageManager().getRaw("gui.items.gradient.name"),
                            plugin.getMessageManager().getStringList("gui.items.gradient.lore").toArray(new String[0])
                    ),
                    event -> {
                        Player clicker = (Player) event.getWhoClicked();
                        new ColorSelectorGUI(plugin, "GRADIENT").open(clicker);
                    }
            );
        } else {
            setItem(12, filler);
        }

        if (player.hasPermission("chatcolor.gui.pattern")) {
            setItem(14, createItem(
                            new ItemStack(Material.NETHER_STAR),
                            plugin.getMessageManager().getRaw("gui.items.pattern.name"),
                            plugin.getMessageManager().getStringList("gui.items.pattern.lore").toArray(new String[0])
                    ),
                    event -> {
                        Player clicker = (Player) event.getWhoClicked();
                        new ColorSelectorGUI(plugin, "PATTERN").open(clicker);
                    }
            );
        } else {
            setItem(14, filler);
        }

        setItem(16, createItem(
                        new ItemStack(Material.BARRIER),
                        plugin.getMessageManager().getRaw("gui.items.reset.name"),
                        plugin.getMessageManager().getStringList("gui.items.reset.lore").toArray(new String[0])
                ),
                event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getChatColorAPI().resetColor(clicker);
                    clicker.closeInventory();
                    plugin.getMessageManager().send(clicker, "color-reset");
                }
        );
    }

    private ItemStack createItem(ItemStack base, String name, String... lorelines) {
        if (base == null || base.getType() == Material.AIR) base = new ItemStack(Material.PAPER);
        ItemMeta meta = base.getItemMeta();
        if (meta == null) return base;
        meta.displayName(ColorUtil.colorize(name));
        List<Component> lore = new ArrayList<>();
        for (String line : lorelines) {
            lore.add(ColorUtil.colorize(line));
        }
        meta.lore(lore);
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
