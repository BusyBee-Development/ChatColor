package net.busybee.chatcolor.api;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.data.PlayerColorData;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import net.busybee.chatcolor.models.PatternEntry;
import net.busybee.chatcolor.models.SelectableEntry;
import net.kyori.adventure.text.Component;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.PatternApplier;
import net.busybee.chatcolor.utils.TokenSafeColorizer;
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
     * Sets a player's color to a custom hex color.
     *
     * @param player  The player whose color should be set.
     * @param hexCode The hex code to set (e.g., "#ABCDEF" or "ABCDEF").
     */
    public void setCustomColor(Player player, String hexCode) {
        if (!hexCode.startsWith("#")) {
            hexCode = "#" + hexCode;
        }
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        data.setColorType("CUSTOM");
        data.setColorKey(hexCode);
        data.setColorTag("<" + hexCode + ">");
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
     * <p>A RANDOM or RANDOM_GRADIENT pattern uses the player's fixed seed here, so the same text
     * always comes out the same (names don't flicker). Chat passes a fresh seed per message with
     * {@link #applyColorToComponent(Player, Component, long)}.
     *
     * @param player    The player whose color should be used.
     * @param component The component to colorize.
     * @return A colorized Component.
     */
    public Component applyColorToComponent(Player player, Component component) {
        return applyColorToComponent(player, component, fixedSeed(player.getUniqueId()));
    }

    /**
     * Applies a player's current color to a Component, using {@code seed} for random patterns.
     * The same seed always gives the same colors.
     *
     * @param player    The player whose color should be used.
     * @param component The component to colorize.
     * @param seed      Seed for RANDOM and RANDOM_GRADIENT patterns; ignored otherwise.
     * @return A colorized Component.
     */
    public Component applyColorToComponent(Player player, Component component, long seed) {
        PatternEntry pattern = resolveActivePattern(player);
        if (pattern != null) {
            return PatternApplier.apply(component, pattern, seed);
        }

        String tag = resolveActiveTag(player);
        if (tag == null) {
            return component;
        }
        return ColorUtil.applyTagToComponent(tag, component);
    }

    /**
     * Applies a player's current color to a Component, keeping every string in {@code keepIntact}
     * as one uncolored run of text. Use this when another plugin will later look for those
     * strings with {@code replaceText} (for example ZelChat's {@code [item]} or mentioned names),
     * since a per-character gradient or pattern would otherwise split them apart.
     *
     * @param player     The player whose color should be used.
     * @param component  The component to colorize.
     * @param keepIntact Text to leave whole and uncolored.
     * @return A colorized Component.
     */
    public Component applyColorToComponent(Player player, Component component, Collection<String> keepIntact) {
        return applyColorToComponent(player, component, keepIntact, fixedSeed(player.getUniqueId()));
    }

    /**
     * Same as {@link #applyColorToComponent(Player, Component, Collection)}, with a seed for
     * random patterns.
     */
    public Component applyColorToComponent(Player player, Component component, Collection<String> keepIntact,
                                           long seed) {
        PatternEntry pattern = resolveActivePattern(player);
        String tag = pattern == null ? resolveActiveTag(player) : null;
        if (pattern == null && tag == null) {
            return component;
        }

        Component colored = TokenSafeColorizer.apply(component, keepIntact, text -> pattern != null
                ? PatternApplier.apply(text, pattern, seed)
                : ColorUtil.styleLiteral(tag, text));
        return colored != null ? colored : applyColorToComponent(player, component, seed);
    }

    /**
     * The seed a player's random pattern uses outside chat (names, placeholders, the API): the
     * same for that player every time, so their name keeps one look.
     */
    public static long fixedSeed(UUID uuid) {
        return uuid.getMostSignificantBits() ^ Long.rotateLeft(uuid.getLeastSignificantBits(), 17);
    }

    /**
     * The color tag that currently applies to a player, or null when none does.
     *
     * <p>The entry is looked up fresh rather than trusting the tag stored when the player
     * picked it, so edits to colors.yml take effect and a revoked permission actually
     * stops applying. Returns null while an active pattern is in use, since a pattern is
     * a list of colors rather than one tag - see {@link #resolveActivePattern(Player)}.
     *
     * @param player The player to resolve for.
     * @return A MiniMessage tag, or null.
     */
    public String resolveActiveTag(Player player) {
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        if (!data.hasColor()) {
            return defaultTagFor(player);
        }

        String type = data.getColorType();
        String key = data.getColorKey();

        if ("PATTERN".equals(type)) {
            PatternEntry pattern = plugin.getPatternManager().getPattern(key);
            if (pattern != null && pattern.isAllowed(player)) return null;
            return defaultTagFor(player);
        }

        if ("CUSTOM".equals(type)) {
            return player.hasPermission("chatcolor.set.hex") ? data.getColorTag() : defaultTagFor(player);
        }

        SelectableEntry entry = "GRADIENT".equals(type)
                ? plugin.getColorManager().getGradient(key)
                : plugin.getColorManager().getColor(key);

        if (entry != null) {
            return entry.isAllowed(player) ? entry.getTag() : defaultTagFor(player);
        }

        // The entry is no longer in colors.yml, so fall back to the tag we stored.
        return data.getColorTag();
    }

    /**
     * The pattern a player currently has applied, or null when they have none or may no
     * longer use the one they picked.
     *
     * @param player The player to resolve for.
     * @return A PatternEntry, or null.
     */
    public PatternEntry resolveActivePattern(Player player) {
        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        if (!data.hasColor() || !"PATTERN".equals(data.getColorType())) {
            return null;
        }

        PatternEntry pattern = plugin.getPatternManager().getPattern(data.getColorKey());
        return (pattern != null && pattern.isAllowed(player)) ? pattern : null;
    }

    private String defaultTagFor(Player player) {
        String defaultTag = getDefaultColorForPlayer(player);
        // A group-default can be null if the key is present but empty in YAML, and
        // this runs for every chat message, so don't trust it.
        if (defaultTag == null || defaultTag.isBlank() || defaultTag.equalsIgnoreCase("NONE")) {
            return null;
        }
        return defaultTag;
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
     * Gets the standard solid colors from the colors section of colors.yml.
     * Use {@link #getAllColors()} if you also want the owner-defined custom colors.
     *
     * @return A collection of ColorEntry.
     */
    public Collection<ColorEntry> getAvailableColors() {
        return plugin.getColorManager().getColors().values();
    }

    /**
     * Gets every solid color a player could select: the standard ones followed by the
     * custom colors defined in the custom-colors section.
     *
     * @return A collection of ColorEntry.
     */
    public Collection<ColorEntry> getAllColors() {
        return plugin.getColorManager().getColorList();
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
