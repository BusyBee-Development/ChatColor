package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.models.PatternEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatternManager {

    private final ChatColor plugin;
    private volatile Map<String, PatternEntry> patterns = Collections.emptyMap();
    public PatternManager(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = new net.busybee.chatcolor.utils.ConfigMigrator(plugin).setReaddMissingKeys(false).migrate("colors/patterns.yml");

        Map<String, PatternEntry> loaded = new LinkedHashMap<>();
        ConfigurationSection section = plugin.getConfigManager().isShowStandardPatterns()
                ? config.getConfigurationSection("patterns")
                : null;

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String displayName = section.getString(key + ".display-name", key);
                String permission = section.getString(key + ".permission", "chatcolor.pattern." + key);
                String icon = section.getString(key + ".icon", "YELLOW_WOOL");
                List<String> colors = section.getStringList(key + ".colors");
                if (colors.isEmpty()) continue;
                loaded.put(key, new PatternEntry(key, displayName, permission, icon, colors));
            }
        }

        this.patterns = Collections.unmodifiableMap(loaded);
    }

    public Map<String, PatternEntry> getPatterns() {
        return this.patterns;
    }
    public List<PatternEntry> getPatternList() {
        return new ArrayList<>(this.patterns.values());
    }
    public PatternEntry getPattern(String key) {
        return this.patterns.get(key);
    }
}
