package net.busybee.chatcolor.inventory.impl;

import net.busybee.chatcolor.LuminaColor;
import net.busybee.chatcolor.inventory.InventoryButton;
import net.busybee.chatcolor.inventory.InventoryGUI;
import net.busybee.chatcolor.utils.ColorUtil;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainMenuGUI extends InventoryGUI {

    private static final int SIZE = 27;
    private static final int[] FILLER_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 11, 13, 15, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26
    };

    private final LuminaColor plugin;

    public MainMenuGUI(LuminaColor plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Inventory createInventory() {
        Component title = ColorUtil.colorize(plugin.getConfigManager().getMainMenuTitle());
        return Bukkit.createInventory(null, SIZE, title);
    }

    @Override
    public void decorate(Player player) {
        ItemStack filler = createFiller();
        for (int slot : FILLER_SLOTS) {
            getInventory().setItem(slot, filler);
        }

        addButton(10, new InventoryButton()
                .creator(p -> createItem(
                        XMaterial.matchXMaterial("LIME_DYE").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.PAPER.parseMaterial())),
                        "<green><bold>Solid Colors",
                        "<gray>Browse and apply solid colors",
                        "<gray>and hex RGB colors."
                ))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.closeInventory();
                    ColorSelectorGUI gui = new ColorSelectorGUI(plugin, "SOLID");
                    plugin.getGuiManager().openGUI(gui, clicker);
                })
        );

        addButton(12, new InventoryButton()
                .creator(p -> createItem(
                        XMaterial.matchXMaterial("MAGMA_CREAM").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.PAPER.parseMaterial())),
                        "<gradient:red:blue><bold>Gradients",
                        "<gray>Apply smooth gradient colors",
                        "<gray>across your messages."
                ))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.closeInventory();
                    ColorSelectorGUI gui = new ColorSelectorGUI(plugin, "GRADIENT");
                    plugin.getGuiManager().openGUI(gui, clicker);
                })
        );

        addButton(14, new InventoryButton()
                .creator(p -> createItem(
                        XMaterial.matchXMaterial("NETHER_STAR").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.PAPER.parseMaterial())),
                        "<rainbow><bold>Patterns",
                        "<gray>Apply cycling color patterns",
                        "<gray>character by character."
                ))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.closeInventory();
                    ColorSelectorGUI gui = new ColorSelectorGUI(plugin, "PATTERN");
                    plugin.getGuiManager().openGUI(gui, clicker);
                })
        );

        addButton(16, new InventoryButton()
                .creator(p -> createItem(
                        XMaterial.matchXMaterial("BARRIER").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.PAPER.parseMaterial())),
                        "<red><bold>Reset Color",
                        "<gray>Remove your current chat color."
                ))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getLuminaColorAPI().resetColor(clicker);
                    clicker.closeInventory();
                    plugin.getMessageManager().send(clicker, "color-reset");
                })
        );

        super.decorate(player);
    }

    private ItemStack createItem(ItemStack base, String name, String... lorelines) {
        if (base == null) base = XMaterial.matchXMaterial("PAPER").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.PAPER.parseMaterial()));
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
        ItemStack item = XMaterial.matchXMaterial("GRAY_STAINED_GLASS_PANE").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }
}