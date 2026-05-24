package net.busybee.chatcolor.inventory.impl;

import net.busybee.chatcolor.ChatColor;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class MainMenuGUI extends FastInv {
    private final ChatColor plugin;

    public MainMenuGUI(ChatColor plugin) {
        super(plugin.getGuiManager().getInt("layouts.main.size", 27), plugin.getGuiManager().get("titles.main"));
        this.plugin = plugin;
    }

    @Override
    public void open(Player player) {
        decorate(player);
        super.open(player);
    }

    private void decorate(Player player) {
        ItemStack filler = plugin.getGuiManager().getFillerItem();
        for (int slot : plugin.getGuiManager().getIntList("layouts.main.filler-slots")) {
            setItem(slot, filler);
        }

        if (player.hasPermission("chatcolor.gui.solid")) {
            setItem(plugin.getGuiManager().getInt("layouts.main.solid-slot", 10), 
                    plugin.getGuiManager().getItemStack("items.solid", Material.LIME_DYE),
                    event -> {
                        Player clicker = (Player) event.getWhoClicked();
                        new ColorSelectorGUI(plugin, "SOLID").open(clicker);
                    }
            );
        }

        if (player.hasPermission("chatcolor.gui.gradient")) {
            setItem(plugin.getGuiManager().getInt("layouts.main.gradient-slot", 12), 
                    plugin.getGuiManager().getItemStack("items.gradient", Material.MAGMA_CREAM),
                    event -> {
                        Player clicker = (Player) event.getWhoClicked();
                        new ColorSelectorGUI(plugin, "GRADIENT").open(clicker);
                    }
            );
        }

        if (player.hasPermission("chatcolor.gui.pattern")) {
            setItem(plugin.getGuiManager().getInt("layouts.main.pattern-slot", 14), 
                    plugin.getGuiManager().getItemStack("items.pattern", Material.NETHER_STAR),
                    event -> {
                        Player clicker = (Player) event.getWhoClicked();
                        new ColorSelectorGUI(plugin, "PATTERN").open(clicker);
                    }
            );
        }

        setItem(plugin.getGuiManager().getInt("layouts.main.reset-slot", 16), 
                plugin.getGuiManager().getItemStack("items.reset", Material.BARRIER),
                event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getChatColorAPI().resetColor(clicker);
                    clicker.closeInventory();
                    plugin.getMessageManager().send(clicker, "color-reset");
                }
        );
    }
}
