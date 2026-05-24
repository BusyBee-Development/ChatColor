package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.models.PatternEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatternManager {

    private final ChatColor plugin;
    private final Map<String, PatternEntry> patterns = new LinkedHashMap<>();

    public PatternManager(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.patterns.clear();

        FileConfiguration config = new net.busybee.chatcolor.utils.ConfigMigrator(plugin).migrate("configs/patterns.yml");
        ConfigurationSection section = config.getConfigurationSection("patterns");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String displayName = section.getString(key + ".display-name", key);
            String permission = section.getString(key + ".permission", "chatcolor.pattern." + key);
            String icon = section.getString(key + ".icon", "YELLOW_WOOL");
            List<String> colors = section.getStringList(key + ".colors");
            if (colors.isEmpty()) continue;
            this.patterns.put(key, new PatternEntry(key, displayName, permission, icon, colors));
        }
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
