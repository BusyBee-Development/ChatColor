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
    private boolean papiIntegration;
    private boolean lateBind;

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
        this.eventPriority = plugin.getConfig().getString("settings.event-priority", "LOWEST");
        this.papiIntegration = plugin.getConfig().getBoolean("settings.papi-integration", true);
        this.lateBind = plugin.getConfig().getBoolean("settings.late-bind", false);
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
    public boolean isPapiIntegration() {
        return this.papiIntegration;
    }
    public boolean isLateBind() {
        return this.lateBind;
    }
}
