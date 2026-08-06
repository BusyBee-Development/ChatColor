package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;

import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final ChatColor plugin;

    private boolean applyToMessage;
    private boolean applyToName;
    private String eventPriority;
    private String chatHook;
    private String defaultColor;
    private java.util.LinkedHashMap<String, String> groupDefaults = new java.util.LinkedHashMap<>();
    private boolean lateBind;
    private boolean cleanConsole;
    private boolean showStandardColors;
    private boolean showStandardGradients;
    private boolean showStandardPatterns;

    public ConfigManager(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        new net.busybee.chatcolor.utils.ConfigMigrator(plugin).migrate("config.yml");
        plugin.reloadConfig();
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        
        try {
            config.options().getClass().getMethod("useQuotes", boolean.class).invoke(config.options(), true);
        } catch (Exception ignored) {}

        this.applyToMessage = config.getBoolean("settings.apply-to-message", true);
        this.applyToName = config.getBoolean("settings.apply-to-name", false);
        this.defaultColor = config.getString("settings.default-color", "NONE");
        
        this.groupDefaults.clear();
        org.bukkit.configuration.ConfigurationSection groupSection = config.getConfigurationSection("settings.group-defaults");
        if (groupSection != null) {
            for (String key : groupSection.getKeys(false)) {
                this.groupDefaults.put(key, groupSection.getString(key));
            }
        }

        this.eventPriority = config.getString("settings.event-priority", "DEFAULT");
        this.chatHook = config.getString("settings.chat-hook", "AUTO");
        this.lateBind = config.getBoolean("settings.late-bind", false);
        this.cleanConsole = config.getBoolean("settings.clean-console", true);
        this.showStandardColors = config.getBoolean("settings.show-standard-colors", true);
        this.showStandardGradients = config.getBoolean("settings.show-standard-gradients", true);
        this.showStandardPatterns = config.getBoolean("settings.show-standard-patterns", true);
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
    public boolean isApplyToMessage() {
        return this.applyToMessage;
    }
    public boolean isApplyToName() {
        return this.applyToName;
    }
    public String getEventPriority() {
        return this.eventPriority;
    }
    public String getChatHook() {
        return this.chatHook;
    }
    public String getDefaultColor() {
        return this.defaultColor;
    }
    public boolean isLateBind() {
        return this.lateBind;
    }
    public boolean isCleanConsole() {
        return this.cleanConsole;
    }
    public boolean isShowStandardColors() {
        return this.showStandardColors;
    }
    public boolean isShowStandardGradients() {
        return this.showStandardGradients;
    }
    public boolean isShowStandardPatterns() {
        return this.showStandardPatterns;
    }
}
