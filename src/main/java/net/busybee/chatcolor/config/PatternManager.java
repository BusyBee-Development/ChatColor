package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.models.PatternEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatternManager {

    private static final List<String> PUBLIC_ALIASES = List.of("none", "public", "everyone", "all");

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
                String icon = section.getString(key + ".icon", "YELLOW_WOOL");
                List<String> colors = section.getStringList(key + ".colors");
                if (colors.isEmpty()) continue;
                String permission = section.getString(key + ".permission", "chatcolor.pattern." + key);
                if (permission != null && PUBLIC_ALIASES.contains(permission.trim().toLowerCase(Locale.ROOT))) {
                    permission = "";
                }

                PermissionDefault permissionDefault = parsePermissionDefault(
                        section.getString(key + ".default"), "patterns." + key);

                loaded.put(key, new PatternEntry(key, displayName, permission, icon, colors, permissionDefault));
            }
        }

        this.patterns = Collections.unmodifiableMap(loaded);
    }

    private PermissionDefault parsePermissionDefault(String raw, String path) {
        if (raw == null || raw.isBlank()) return PermissionDefault.OP;

        PermissionDefault parsed = PermissionDefault.getByName(raw.trim());
        if (parsed == null) {
            plugin.getLogger().warning("Invalid 'default' for " + path + ": '" + raw
                    + "'. Expected true, false, op or not-op. Using op.");
            return PermissionDefault.OP;
        }
        return parsed;
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
