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

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * API for interacting with ChatColor.
 * This class provides methods to manage player colors, gradients, and patterns.
 */
public class ChatColorAPI {

    private final ChatColor plugin;

    public ChatColorAPI(ChatColor plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets a player's color to a solid color.
     *
     * @param player   The player whose color should be set.
     * @param colorKey The key of the color to set (from colors.yml).
     */
    public void setColor(Player player, String colorKey) {
        ColorEntry entry = plugin.getColorManager().getColor(colorKey);
        if (entry == null) return;
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.setColorType("SOLID");
        data.setColorKey(colorKey);
        data.setColorTag(entry.getTag());
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    /**
     * Sets a player's color to a gradient.
     *
     * @param player      The player whose color should be set.
     * @param gradientKey The key of the gradient to set (from colors.yml).
     */
    public void setGradient(Player player, String gradientKey) {
        GradientEntry entry = plugin.getColorManager().getGradient(gradientKey);
        if (entry == null) return;
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.setColorType("GRADIENT");
        data.setColorKey(gradientKey);
        data.setColorTag(entry.getTag());
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    /**
     * Sets a player's color to a pattern.
     *
     * @param player     The player whose color should be set.
     * @param patternKey The key of the pattern to set (from patterns.yml).
     */
    public void setPattern(Player player, String patternKey) {
        PatternEntry entry = plugin.getPatternManager().getPattern(patternKey);
        if (entry == null) return;
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.setColorType("PATTERN");
        data.setColorKey(patternKey);
        data.setColorTag(null);
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    /**
     * Resets a player's color to the default.
     *
     * @param player The player whose color should be reset.
     */
    public void resetColor(Player player) {
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.reset();
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    /**
     * Gets the color data for a player.
     *
     * @param uuid The UUID of the player.
     * @return The PlayerColorData object for the player.
     */
    public PlayerColorData getPlayerData(UUID uuid) {
        return plugin.getPlayerDataManager().getData(uuid);
    }

    /**
     * Applies a player's current color to a string and returns a Component.
     *
     * @param player The player whose color should be used.
     * @param text   The text to colorize.
     * @return A colorized Component.
     */
    public Component applyColorToText(Player player, String text) {
        return applyColorToComponent(player, Component.text(text));
    }

    /**
     * Applies a player's current color to a Component.
     *
     * @param player    The player whose color should be used.
     * @param component The component to colorize.
     * @return A colorized Component.
     */
    public Component applyColorToComponent(Player player, Component component) {
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        if (!data.hasColor()) {
            String defaultTag = getDefaultColorForPlayer(player);
            if (defaultTag.equalsIgnoreCase("NONE")) {
                return component;
            }
            return ColorUtil.applyTagToComponent(defaultTag, component);
        }
        if (data.getColorType().equals("PATTERN")) {
            PatternEntry pattern = plugin.getPatternManager().getPattern(data.getColorKey());
            if (pattern != null) return PatternApplier.apply(component, pattern.getColors());
            return component;
        }
        return ColorUtil.applyTagToComponent(data.getColorTag(), component);
    }

    /**
     * Gets the default color tag for a player based on their groups/permissions.
     *
     * @param player The player to check.
     * @return The color tag (e.g., "<red>" or "NONE").
     */
    public String getDefaultColorForPlayer(Player player) {
        for (Map.Entry<String, String> entry : plugin.getConfigManager().getGroupDefaults().entrySet()) {
            if (player.hasPermission("chatcolor.group." + entry.getKey())) {
                return entry.getValue();
            }
        }
        return plugin.getConfigManager().getDefaultColor();
    }

    /**
     * Gets all registered solid colors.
     * @return A collection of ColorEntry.
     */
    public Collection<ColorEntry> getAvailableColors() {
        return plugin.getColorManager().getColors().values();
    }

    /**
     * Gets all registered custom colors.
     * @return A collection of ColorEntry.
     */
    public Collection<ColorEntry> getAvailableCustomColors() {
        return plugin.getColorManager().getCustomColors().values();
    }

    /**
     * Gets all registered gradients.
     * @return A collection of GradientEntry.
     */
    public Collection<GradientEntry> getAvailableGradients() {
        return plugin.getColorManager().getGradients().values();
    }

    /**
     * Gets all registered patterns.
     * @return A collection of PatternEntry.
     */
    public Collection<PatternEntry> getAvailablePatterns() {
        return plugin.getPatternManager().getPatterns().values();
    }
}
