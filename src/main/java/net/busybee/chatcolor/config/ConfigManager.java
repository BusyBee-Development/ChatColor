package net.busybee.chatcolor.config;

import net.busybee.chatcolor.LuminaColor;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final LuminaColor plugin;
    private final Map<String, ColorEntry> colors = new LinkedHashMap<>();
    private final Map<String, GradientEntry> gradients = new LinkedHashMap<>();

    private String mainMenuTitle;
    private String colorSelectorTitle;
    private String gradientSelectorTitle;
    private String patternSelectorTitle;
    private boolean applyToMessage;
    private boolean applyToName;

    public ConfigManager(LuminaColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.colors.clear();
        this.gradients.clear();

        this.mainMenuTitle = plugin.getConfig().getString("gui.main-menu-title", "<gradient:blue:aqua><bold>LuminaColor");
        this.colorSelectorTitle = plugin.getConfig().getString("gui.color-selector-title", "<aqua><bold>Solid Colors");
        this.gradientSelectorTitle = plugin.getConfig().getString("gui.gradient-selector-title", "<gradient:red:blue><bold>Gradients");
        this.patternSelectorTitle = plugin.getConfig().getString("gui.pattern-selector-title", "<rainbow><bold>Patterns");
        this.applyToMessage = plugin.getConfig().getBoolean("settings.apply-to-message", true);
        this.applyToName = plugin.getConfig().getBoolean("settings.apply-to-name", true);

        ConfigurationSection colorsSection = plugin.getConfig().getConfigurationSection("colors");
        if (colorsSection != null) {
            for (String key : colorsSection.getKeys(false)) {
                String displayName = colorsSection.getString(key + ".display-name", key);
                String tag = colorsSection.getString(key + ".tag", "<white>");
                String permission = colorsSection.getString(key + ".permission", "luminacolor.color." + key);
                String icon = colorsSection.getString(key + ".icon", "WHITE_WOOL");
                this.colors.put(key, new ColorEntry(key, displayName, tag, permission, icon));
            }
        }

        ConfigurationSection gradientsSection = plugin.getConfig().getConfigurationSection("gradients");
        if (gradientsSection != null) {
            for (String key : gradientsSection.getKeys(false)) {
                String displayName = gradientsSection.getString(key + ".display-name", key);
                String tag = gradientsSection.getString(key + ".tag", "<white>");
                String permission = gradientsSection.getString(key + ".permission", "luminacolor.gradient." + key);
                String icon = gradientsSection.getString(key + ".icon", "WHITE_WOOL");
                this.gradients.put(key, new GradientEntry(key, displayName, tag, permission, icon));
            }
        }
    }

    public Map<String, ColorEntry> getColors() {
        return this.colors;
    }

    public Map<String, GradientEntry> getGradients() {
        return this.gradients;
    }

    public List<ColorEntry> getColorList() {
        return new ArrayList<>(this.colors.values());
    }

    public List<GradientEntry> getGradientList() {
        return new ArrayList<>(this.gradients.values());
    }

    public ColorEntry getColor(String key) {
        return this.colors.get(key);
    }

    public GradientEntry getGradient(String key) {
        return this.gradients.get(key);
    }

    public String getMainMenuTitle() {
        return this.mainMenuTitle;
    }

    public String getColorSelectorTitle() {
        return this.colorSelectorTitle;
    }

    public String getGradientSelectorTitle() {
        return this.gradientSelectorTitle;
    }

    public String getPatternSelectorTitle() {
        return this.patternSelectorTitle;
    }

    public boolean isApplyToMessage() {
        return this.applyToMessage;
    }

    public boolean isApplyToName() {
        return this.applyToName;
    }
}