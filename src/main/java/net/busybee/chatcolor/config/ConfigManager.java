package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;

import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final ChatColor plugin;

    private String mainMenuTitle;
    private String colorSelectorTitle;
    private String gradientSelectorTitle;
    private String patternSelectorTitle;
    private boolean applyToMessage;
    private boolean applyToName;
    private String eventPriority;
    private String defaultColor;
    private java.util.LinkedHashMap<String, String> groupDefaults = new java.util.LinkedHashMap<>();
    private boolean papiIntegration;
    private boolean lateBind;
    private boolean cleanConsole;

    public ConfigManager(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.mainMenuTitle = plugin.getConfig().getString("gui.main-menu-title", "<gradient:blue:aqua><bold>ChatColor");
        this.colorSelectorTitle = plugin.getConfig().getString("gui.color-selector-title", "<aqua><bold>Solid Colors");
        this.gradientSelectorTitle = plugin.getConfig().getString("gui.gradient-selector-title", "<gradient:red:blue><bold>Gradients");
        this.patternSelectorTitle = plugin.getConfig().getString("gui.pattern-selector-title", "<rainbow><bold>Patterns");
        this.applyToMessage = plugin.getConfig().getBoolean("settings.apply-to-message", true);
        this.applyToName = plugin.getConfig().getBoolean("settings.apply-to-name", false);
        this.defaultColor = plugin.getConfig().getString("settings.default-color", "NONE");
        
        this.groupDefaults.clear();
        org.bukkit.configuration.ConfigurationSection groupSection = plugin.getConfig().getConfigurationSection("settings.group-defaults");
        if (groupSection != null) {
            for (String key : groupSection.getKeys(false)) {
                this.groupDefaults.put(key, groupSection.getString(key));
            }
        }

        this.eventPriority = plugin.getConfig().getString("settings.event-priority", "LOWEST");
        this.papiIntegration = plugin.getConfig().getBoolean("settings.papi-integration", true);
        this.lateBind = plugin.getConfig().getBoolean("settings.late-bind", false);
        this.cleanConsole = plugin.getConfig().getBoolean("settings.clean-console", true);
    }

    public java.util.Map<String, String> getGroupDefaults() {
        return this.groupDefaults;
    }

    public Map<String, ColorEntry> getColors() {
        return plugin.getColorManager().getColors();
    }
    public Map<String, GradientEntry> getGradients() {
        return plugin.getColorManager().getGradients();
    }
    public List<ColorEntry> getColorList() {
        return plugin.getColorManager().getColorList();
    }
    public List<GradientEntry> getGradientList() {
        return plugin.getColorManager().getGradientList();
    }
    public ColorEntry getColor(String key) {
        return plugin.getColorManager().getColor(key);
    }
    public GradientEntry getGradient(String key) {
        return plugin.getColorManager().getGradient(key);
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
    public String getEventPriority() {
        return this.eventPriority;
    }
    public String getDefaultColor() {
        return this.defaultColor;
    }
    public boolean isPapiIntegration() {
        return this.papiIntegration;
    }
    public boolean isLateBind() {
        return this.lateBind;
    }
    public boolean isCleanConsole() {
        return this.cleanConsole;
    }
}
