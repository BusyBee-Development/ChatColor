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
    public String onPlaceholderRequest(org.bukkit.entity.Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        String result = null;
        String action = params.isEmpty() ? "color" : params;

        if (action.equalsIgnoreCase("color") || action.equalsIgnoreCase("prefix") || action.equalsIgnoreCase("tag")) {
            String tag;
            if (player instanceof org.bukkit.entity.Player online) {
                tag = plugin.getChatColorAPI().resolveActiveTag(online);
            } else {
                tag = (data != null && data.hasColor())
                        ? data.getColorTag() : plugin.getConfigManager().getDefaultColor();
            }
            if (tag == null || tag.equalsIgnoreCase("NONE")) return "";
            
            if (tag.startsWith("<") && tag.endsWith(">")) {
                Component comp = ColorUtil.applyTagToText(tag, "\u200B");
                result = ColorUtil.toLegacy(comp).replace("\u200B", "");
                if (result.isEmpty()) result = tag;
            } else {
                result = tag;
            }
        } else if (action.equalsIgnoreCase("message")) {
            String msg = ChatListener.getLastMessage(player.getUniqueId());
            result = (msg != null && !msg.isEmpty()) ? ColorUtil.toLegacy(buildColored(player, data, msg)) : "";
        } else if (action.equalsIgnoreCase("name")) {
            String name = player.getName();
            result = (name != null) ? ColorUtil.toLegacy(buildColored(player, data, name)) : "";
        } else if (action.startsWith("apply_")) {
            String text = action.substring(6);
            result = ColorUtil.toLegacy(buildColored(player, data, text));
        } else if (action.startsWith("apply:")) {
            String text = action.substring(6);
            result = ColorUtil.toLegacy(buildColored(player, data, text));
        } else if (action.equalsIgnoreCase("key")) {
            result = (data != null && data.getColorKey() != null) ? data.getColorKey() : "";
        } else if (action.equalsIgnoreCase("type")) {
            result = (data != null && data.getColorType() != null) ? data.getColorType() : "NONE";
        }

        return result;
    }

    private Component buildColored(OfflinePlayer player, PlayerColorData data, String text) {
        if (player instanceof org.bukkit.entity.Player online) {
            PatternEntry pattern = plugin.getChatColorAPI().resolveActivePattern(online);
            if (pattern != null) return PatternApplier.apply(text, pattern.getColors(), false);

            String tag = plugin.getChatColorAPI().resolveActiveTag(online);
            return tag == null ? Component.text(text) : ColorUtil.applyTagToText(tag, text, false);
        }

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
