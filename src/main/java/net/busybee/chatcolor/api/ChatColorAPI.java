package net.busybee.chatcolor.api;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.data.PlayerColorData;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import net.busybee.chatcolor.models.PatternEntry;
import net.kyori.adventure.text.Component;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.PatternApplier;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatColorAPI {

    private final ChatColor plugin;

    public ChatColorAPI(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void setColor(Player player, String colorKey) {
        ColorEntry entry = plugin.getConfigManager().getColor(colorKey);
        if (entry == null) return;
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.setColorType("SOLID");
        data.setColorKey(colorKey);
        data.setColorTag(entry.getTag());
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    public void setGradient(Player player, String gradientKey) {
        GradientEntry entry = plugin.getConfigManager().getGradient(gradientKey);
        if (entry == null) return;
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.setColorType("GRADIENT");
        data.setColorKey(gradientKey);
        data.setColorTag(entry.getTag());
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    public void setPattern(Player player, String patternKey) {
        PatternEntry entry = plugin.getPatternManager().getPattern(patternKey);
        if (entry == null) return;
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.setColorType("PATTERN");
        data.setColorKey(patternKey);
        data.setColorTag(null);
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    public void resetColor(Player player) {
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.reset();
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    public PlayerColorData getPlayerData(UUID uuid) {
        return plugin.getPlayerDataManager().getData(uuid);
    }

    public Component applyColorToText(Player player, String text) {
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        if (!data.hasColor()) return Component.text(text);
        if (data.getColorType().equals("PATTERN")) {
            PatternEntry pattern = plugin.getPatternManager().getPattern(data.getColorKey());
            if (pattern != null) return PatternApplier.apply(text, pattern.getColors());
            return Component.text(text);
        }
        return ColorUtil.applyTagToText(data.getColorTag(), text);
    }
}
