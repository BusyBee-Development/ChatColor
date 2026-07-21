package net.busybee.chatcolor.hooks;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.data.PlayerColorData;
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
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        if (params.equalsIgnoreCase("color") || params.equalsIgnoreCase("prefix") || params.equalsIgnoreCase("tag")) {
            if (data == null || !data.hasColor()) {
                String def = plugin.getConfigManager().getDefaultColor();
                return (def == null || "NONE".equalsIgnoreCase(def)) ? "" : def;
            }
            return data.getColorTag() != null ? data.getColorTag() : "";
        }

        if (params.equalsIgnoreCase("message")) {
            String msg = ChatListener.getLastMessage(player.getUniqueId());
            if (msg == null || msg.isEmpty()) return "";
            return ColorUtil.toMiniMessage(buildColored(data, msg));
        }

        if (params.equalsIgnoreCase("key")) {
            return (data != null && data.getColorKey() != null) ? data.getColorKey() : "";
        }

        if (params.equalsIgnoreCase("type")) {
            return (data != null && data.getColorType() != null) ? data.getColorType() : "NONE";
        }

        if (params.equalsIgnoreCase("name")) {
            String name = player.getName();
            if (name == null) return "";
            return ColorUtil.toMiniMessage(buildColored(data, name));
        }

        if (params.startsWith("apply_")) {
            return ColorUtil.toMiniMessage(buildColored(data, params.substring(6)));
        }
        if (params.startsWith("apply:")) {
            return ColorUtil.toMiniMessage(buildColored(data, params.substring(6)));
        }

        return null;
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
