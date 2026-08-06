package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ColorManager {

    private final ChatColor plugin;
    private final File file;
    private FileConfiguration config;
    private volatile Map<String, ColorEntry> colors = Collections.emptyMap();
    private volatile Map<String, GradientEntry> gradients = Collections.emptyMap();
    private volatile Map<String, ColorEntry> customColors = Collections.emptyMap();

    public ColorManager(ChatColor plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "colors/colors.yml");
    }

    public void load() {
        this.config = new net.busybee.chatcolor.utils.ConfigMigrator(plugin).setReaddMissingKeys(false).migrate("colors/colors.yml");

        Map<String, ColorEntry> loadedColors = new LinkedHashMap<>();
        Map<String, GradientEntry> loadedGradients = new LinkedHashMap<>();
        Map<String, ColorEntry> loadedCustom = new LinkedHashMap<>();

        if (plugin.getConfigManager().isShowStandardColors()) {
            loadSection(config.getConfigurationSection("colors"), loadedColors, ColorEntry.class, "chatcolor.color.");
        }

        if (plugin.getConfigManager().isShowStandardGradients()) {
            loadSection(config.getConfigurationSection("gradients"), loadedGradients, GradientEntry.class, "chatcolor.gradient.");
        }

        loadSection(config.getConfigurationSection("custom-colors"), loadedCustom, ColorEntry.class, "chatcolor.custom.");

        this.colors = Collections.unmodifiableMap(loadedColors);
        this.gradients = Collections.unmodifiableMap(loadedGradients);
        this.customColors = Collections.unmodifiableMap(loadedCustom);
    }

    private <T> void loadSection(ConfigurationSection section, Map<String, T> map, Class<T> clazz, String defaultPermPrefix) {
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            String displayName = section.getString(key + ".display-name", key);
            String tag = section.getString(key + ".tag", "<white>");
            String permission = section.getString(key + ".permission", defaultPermPrefix + key);
            String icon = section.getString(key + ".icon", "WHITE_WOOL");
            
            if (clazz == ColorEntry.class) {
                map.put(key, (T) new ColorEntry(key, displayName, tag, permission, icon));
            } else if (clazz == GradientEntry.class) {
                map.put(key, (T) new GradientEntry(key, displayName, tag, permission, icon));
            }
        }
    }

    public void saveCustomColor(String name, String tag, String icon, String permission) {
        String key = name.toLowerCase().replace(" ", "_");
        config.set("custom-colors." + key + ".display-name", name);
        config.set("custom-colors." + key + ".tag", tag);
        config.set("custom-colors." + key + ".icon", icon);

        if (permission == null || permission.isEmpty()) {
            permission = "chatcolor.custom." + key;
        }
        config.set("custom-colors." + key + ".permission", permission);

        try {
            config.save(file);
            load(); // Reload maps
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save colors.yml: " + e.getMessage());
        }
    }

    public Map<String, ColorEntry> getColors() {
        return this.colors;
    }
    public Map<String, GradientEntry> getGradients() {
        return this.gradients;
    }
    public Map<String, ColorEntry> getCustomColors() {
        return this.customColors;
    }

    public List<ColorEntry> getColorList() {
        List<ColorEntry> list = new ArrayList<>(this.colors.values());
        list.addAll(this.customColors.values());
        return list;
    }

    public List<GradientEntry> getGradientList() {
        return new ArrayList<>(this.gradients.values());
    }

    public ColorEntry getColor(String key) {
        ColorEntry entry = this.colors.get(key);
        if (entry == null) entry = this.customColors.get(key);
        return entry;
    }

    public GradientEntry getGradient(String key) {
        return this.gradients.get(key);
    }
}
