package net.busybee.chatcolor.hooks;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.data.PlayerColorData;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import net.busybee.chatcolor.models.PatternEntry;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.PatternApplier;
import net.busybee.chatcolor.listeners.ChatListener;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final ChatColor plugin;

    public PlaceholderAPIHook(ChatColor plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "chatcolor";
    }

    @Override
    public @NotNull String getAuthor() {
        return "BusyBee";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        if (params.equalsIgnoreCase("color")) {
            String tag = getTag(data);
            return (tag == null || tag.equalsIgnoreCase("NONE")) ? "" : tag;
        }

        if (params.equalsIgnoreCase("color_legacy")) {
            String tag = getTag(data);
            if (tag == null || tag.isEmpty() || tag.equalsIgnoreCase("NONE")) return "";

            Component comp = ColorUtil.colorize(tag + "X");
            String legacy = ColorUtil.getLegacySerializer().serialize(comp);
            return legacy.substring(0, legacy.length() - 1);
        }

        if (params.equalsIgnoreCase("color_key")) {
            return data != null && data.getColorKey() != null ? data.getColorKey() : "";
        }

        if (params.equalsIgnoreCase("color_type")) {
            return data != null && data.getColorType() != null ? data.getColorType() : "NONE";
        }

        boolean mm = false;
        String processingParams = params;
        if (processingParams.startsWith("mm_")) {
            mm = true;
            processingParams = processingParams.substring(3);
        } else if (processingParams.endsWith("_mm")) {
            mm = true;
            processingParams = processingParams.substring(0, processingParams.length() - 3);
        }

        if (processingParams.equalsIgnoreCase("message")) {
            String message = ChatListener.getLastMessage(player.getUniqueId());
            if (message == null || message.isEmpty()) return "%message%";
            Component colored = buildColored(data, message);
            return mm ? ColorUtil.toMiniMessage(colored) : ColorUtil.getLegacySerializer().serialize(colored);
        }

        if (processingParams.startsWith("formatted_msg_")) {
            processingParams = processingParams.substring("formatted_msg_".length());
        }

        if (processingParams.isEmpty()) return "";

        if (processingParams.contains("_")) {
            String[] parts = processingParams.split("_", 2);
            String colorName = parts[0];
            String text = parts[1];

            if (isValidColor(colorName)) {
                Component colored = buildColoredWithOverride(colorName, text);
                return mm ? ColorUtil.toMiniMessage(colored) : ColorUtil.getLegacySerializer().serialize(colored);
            }
        }

        Component colored = buildColored(data, processingParams);
        return mm ? ColorUtil.toMiniMessage(colored) : ColorUtil.getLegacySerializer().serialize(colored);
    }

    private String getTag(PlayerColorData data) {
        if (data == null || !data.hasColor()) {
            return plugin.getConfigManager().getDefaultColor();
        }
        if ("PATTERN".equals(data.getColorType())) return null;

        String tag = data.getColorTag();
        if (tag == null || tag.isEmpty()) {
            if (data.getColorKey() != null) {
                ColorEntry entry = plugin.getColorManager().getColor(data.getColorKey());
                if (entry != null) {
                    tag = entry.getTag();
                } else {
                    GradientEntry gradient = plugin.getColorManager().getGradient(data.getColorKey());
                    if (gradient != null) tag = gradient.getTag();
                }
            }
        }
        return tag;
    }

    private boolean isValidColor(String name) {
        if (plugin.getColorManager().getColor(name) != null) return true;
        if (plugin.getColorManager().getGradient(name) != null) return true;
        if (plugin.getPatternManager().getPattern(name) != null) return true;
        return false;
    }

    private Component buildColoredWithOverride(String colorName, String text) {
        ColorEntry color = plugin.getColorManager().getColor(colorName);
        if (color != null) {
            return ColorUtil.applyTagToText(color.getTag(), text, false);
        }
        GradientEntry gradient = plugin.getColorManager().getGradient(colorName);
        if (gradient != null) {
            return ColorUtil.applyTagToText(gradient.getTag(), text, false);
        }
        PatternEntry pattern = plugin.getPatternManager().getPattern(colorName);
        if (pattern != null) {
            return PatternApplier.apply(text, pattern.getColors(), false);
        }
        return Component.text(text);
    }

    private Component buildColored(PlayerColorData data, String text) {
        if (data == null || !data.hasColor()) {
            String defaultColor = plugin.getConfigManager().getDefaultColor();
            if (defaultColor == null || defaultColor.equalsIgnoreCase("NONE")) {
                return Component.text(text);
            }
            return ColorUtil.applyTagToText(defaultColor, text, false);
        }
        if ("PATTERN".equals(data.getColorType())) {
            PatternEntry pattern = plugin.getPatternManager().getPattern(data.getColorKey());
            if (pattern != null) return PatternApplier.apply(text, pattern.getColors(), false);
            return Component.text(text);
        }
        return ColorUtil.applyTagToText(data.getColorTag(), text, false);
    }
}
