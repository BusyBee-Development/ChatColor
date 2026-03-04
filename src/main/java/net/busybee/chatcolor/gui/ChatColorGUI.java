package net.busybee.chatcolor.gui;

import net.busybee.chatcolor.ChatColorPlugin;
import com.cryptomorin.xseries.XMaterial;
import net.busybee.chatcolor.configuration.ConfigurationManager;
import net.busybee.chatcolor.gui.clickaction.api.GuiClickAction;
import net.busybee.chatcolor.gui.clickaction.util.GuiClickActionManager;
import net.busybee.chatcolor.pattern.api.BasePattern;
import net.busybee.chatcolor.util.Placeholders;
import net.busybee.chatcolor.util.Util;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChatColorGUI {

    public static void openGui(Player player){
        Map<String, List<BasePattern>> categories = ChatColorPlugin.getInstance().getPatternManager().getPatternsByCategory();
        if (categories.size() > 1) {
            openCategories(player);
        } else if (!categories.isEmpty()) {
            openPatterns(player, categories.keySet().iterator().next(), 1);
        } else {
            // No patterns available, still open empty GUI with decorations
            openCategories(player);
        }
    }

    public static void openCategories(Player player) {
        ConfigurationManager configurationManager = ChatColorPlugin.getInstance().getConfigurationManager();
        FileConfiguration file = configurationManager.getGui();
        Map<String, List<BasePattern>> categories = ChatColorPlugin.getInstance().getPatternManager().getPatternsByCategory();

        String openSoundStr = file.getString("gui.open-sound");
        Sound sound = null;
        try {
            sound = Sound.valueOf(openSoundStr);
        } catch (IllegalArgumentException | NullPointerException ignored){}

        GuiBuilder builder = new GuiBuilder()
                .setRows(file.getInt("gui.categories.rows", 3))
                .setTitle(file.getString("gui.categories.title", "&8Select Category"));

        // Global decorations/items
        addGlobalItems(builder, player, file, "gui.categories.items");

        int[] slots = {10, 11, 12, 13, 14, 15, 16}; // Default slots for categories
        if (file.contains("gui.categories.content-slots")) {
            List<Integer> list = file.getIntegerList("gui.categories.content-slots");
            slots = list.stream().mapToInt(i -> i).toArray();
        }

        int i = 0;
        for (String categoryName : categories.keySet()) {
            if (i >= slots.length) break;

            String key = "gui.categories.category-items." + categoryName;
            ItemStack itemStack;
            if (file.contains(key)) {
                itemStack = Util.getItemFromConfig(file, key);
            } else {
                itemStack = XMaterial.BOOK.parseItem();
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(Util.color("&a" + categoryName));
                    itemStack.setItemMeta(meta);
                }
            }

            builder.setGuiItem(slots[i], itemStack, GuiClickActionManager.getClickActionFromString("OPEN-CATEGORY:" + categoryName));
            i++;
        }

        builder.open(player);
        if(sound != null) player.playSound(player.getLocation(), sound, 1, 1);
    }

    public static void openPatterns(Player player, String category, int page) {
        ConfigurationManager configurationManager = ChatColorPlugin.getInstance().getConfigurationManager();
        FileConfiguration file = configurationManager.getGui();
        FileConfiguration patternsConfig = configurationManager.getPatterns();
        List<BasePattern> allPatterns = ChatColorPlugin.getInstance().getPatternManager().getPatternsByCategory().getOrDefault(category, Collections.emptyList());

        String openSoundStr = file.getString("gui.open-sound");
        Sound sound = null;
        try {
            sound = Sound.valueOf(openSoundStr);
        } catch (IllegalArgumentException | NullPointerException ignored){}

        String title = file.getString("gui.patterns.title", "&8Select Pattern - %category% (Page %page%)")
                .replace("%category%", category)
                .replace("%page%", String.valueOf(page));

        GuiBuilder builder = new GuiBuilder()
                .setRows(file.getInt("gui.patterns.rows", 6))
                .setTitle(title);

        addGlobalItems(builder, player, file, "gui.patterns.items");

        List<Integer> contentSlots = file.getIntegerList("gui.patterns.content-slots");
        if (contentSlots.isEmpty()) {
            for (int s = 0; s < 45; s++) {
                if (s % 9 != 0 && s % 9 != 8 && s / 9 != 0) contentSlots.add(s); // Default slots
            }
        }

        int pageSize = contentSlots.size();
        int totalPages = (int) Math.ceil((double) allPatterns.size() / pageSize);
        if (totalPages == 0) totalPages = 1;

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allPatterns.size());

        for (int i = startIndex; i < endIndex; i++) {
            BasePattern pattern = allPatterns.get(i);
            int slot = contentSlots.get(i - startIndex);

            String key = pattern.getName(false) + ".gui-item";
            if (!patternsConfig.contains(key)) continue;

            String hasPermission = (pattern.getPermission() == null || player.hasPermission(pattern.getPermission())) ? "has-permission" : "has-not-permission";
            String itemKey = key + "." + hasPermission;

            ItemStack itemStack = Util.getItemFromConfig(patternsConfig, itemKey);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Placeholders.setPlaceholders(meta.getDisplayName(), pattern, player));
                meta.setLore(Placeholders.setPlaceholders(meta.getLore(), pattern, player));
                itemStack.setItemMeta(meta);
            }

            List<String> actionsStr = patternsConfig.getStringList(itemKey + ".actions");
            List<GuiClickAction> actions = GuiClickActionManager.getClickActionsFromList(
                    Placeholders.setPlaceholders(actionsStr, pattern, player)
            );

            builder.setGuiItem(slot, itemStack, actions);
        }

        // Pagination buttons
        if (page > 1) {
            builder.setGuiItem(file.getInt("gui.patterns.prev-page-slot", 45),
                    Util.getItemFromConfig(file, "gui.patterns.prev-page-item"),
                    GuiClickActionManager.getClickActionFromString("OPEN-PAGE:" + category + " " + (page - 1)));
        }
        if (page < totalPages) {
            builder.setGuiItem(file.getInt("gui.patterns.next-page-slot", 53),
                    Util.getItemFromConfig(file, "gui.patterns.next-page-item"),
                    GuiClickActionManager.getClickActionFromString("OPEN-PAGE:" + category + " " + (page + 1)));
        }
        
        // Back button
        if (ChatColorPlugin.getInstance().getPatternManager().getPatternsByCategory().size() > 1) {
            builder.setGuiItem(file.getInt("gui.patterns.back-slot", 49),
                    Util.getItemFromConfig(file, "gui.patterns.back-item"),
                    GuiClickActionManager.getClickActionFromString("BACK-TO-CATEGORIES"));
        }

        builder.open(player);
        if(sound != null) player.playSound(player.getLocation(), sound, 1, 1);
    }

    private static void addGlobalItems(GuiBuilder builder, Player player, FileConfiguration file, String basePath) {
        if (!file.contains(basePath)) return;
        for (String key : file.getConfigurationSection(basePath).getKeys(false)) {
            String fullKey = basePath + "." + key;
            int slot = file.getInt(fullKey + ".slot");
            List<Integer> slots = file.getIntegerList(fullKey + ".slots");
            List<String> actionsStr = file.getStringList(fullKey + ".actions");
            List<GuiClickAction> actions = GuiClickActionManager.getClickActionsFromList(
                    Placeholders.setPlaceholders(actionsStr, null, player)
            );

            ItemStack item = Util.getItemFromConfig(file, fullKey);
            if (slots.isEmpty()) {
                builder.setGuiItem(slot, item, actions);
            } else {
                for (int s : slots) {
                    builder.setGuiItem(s, item, actions);
                }
            }
        }
    }

}
