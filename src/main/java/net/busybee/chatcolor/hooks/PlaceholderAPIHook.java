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
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        if (params.equalsIgnoreCase("color")) {
            if (!data.hasColor()) {
                String defaultColor = plugin.getConfigManager().getDefaultColor();
                return defaultColor.equalsIgnoreCase("NONE") ? "" : defaultColor;
            }
            if (data.getColorType().equals("PATTERN")) return data.getColorKey();
            return data.getColorTag() != null ? data.getColorTag() : "";
        }

        if (params.equalsIgnoreCase("color_key")) {
            return data.getColorKey() != null ? data.getColorKey() : "";
        }

        if (params.equalsIgnoreCase("color_type")) {
            return data.getColorType() != null ? data.getColorType() : "NONE";
        }

        if (params.equalsIgnoreCase("message")) {
            if (!plugin.getConfigManager().isPapiIntegration()) return null;
            String message = ChatListener.getLastMessage(player.getUniqueId());
            if (message.isEmpty()) return "%message%";
            Component colored = buildColored(data, message);
            return ColorUtil.getLegacySerializer().serialize(colored);
        }

        if (params.startsWith("message_")) {
            if (!plugin.getConfigManager().isPapiIntegration()) return null;
            String message = params.substring("message_".length());
            if (message.isEmpty()) return "";
            Component colored = buildColored(data, message);
            return ColorUtil.getLegacySerializer().serialize(colored);
        }

        if (params.startsWith("formatted_msg_")) {
            String message = params.substring("formatted_msg_".length());
            if (message.isEmpty()) return "";
            Component colored = buildColored(data, message);
            return ColorUtil.getLegacySerializer().serialize(colored);
        }

        return null;
    }

    private Component buildColored(PlayerColorData data, String text) {
        if (!data.hasColor()) {
            String defaultColor = plugin.getConfigManager().getDefaultColor();
            if (defaultColor.equalsIgnoreCase("NONE")) {
                return Component.text(text);
            }
            return ColorUtil.applyTagToText(defaultColor, text);
        }
        if (data.getColorType().equals("PATTERN")) {
            PatternEntry pattern = plugin.getPatternManager().getPattern(data.getColorKey());
            if (pattern != null) return PatternApplier.apply(text, pattern.getColors());
            return Component.text(text);
        }
        return ColorUtil.applyTagToText(data.getColorTag(), text);
    }
}
