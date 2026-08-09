package net.busybee.chatcolor.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigLedger {

    private static final String LEDGER_PATH = "data/.config-state.yml";
    private static final String FILES_SECTION = "files";
    private static final String KEYS_SECTION = "keys";
    private static final String VERSION_KEY = "plugin-version";

    private final Plugin plugin;
    private final File file;
    private final FileConfiguration state;

    public ConfigLedger(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), LEDGER_PATH);
        this.state = read();
    }

    private FileConfiguration read() {
        YamlConfiguration loaded = new YamlConfiguration();
        if (!file.exists()) {
            return loaded;
        }
        try {
            loaded.load(file);
        } catch (InvalidConfigurationException | IOException e) {
            plugin.getLogger().warning("Could not read " + LEDGER_PATH + " (" + e.getMessage()
                    + "). Treating this install as new.");
            return new YamlConfiguration();
        }
        return loaded;
    }

    public Map<String, String> shippedKeys(@NotNull String resourcePath) {
        ConfigurationSection keys = state.getConfigurationSection(keysPath(resourcePath));
        if (keys == null) {
            return Collections.emptyMap();
        }

        Map<String, String> shipped = new LinkedHashMap<>();
        for (String key : keys.getKeys(false)) {
            shipped.put(unescape(key), String.valueOf(keys.get(key)));
        }
        return shipped;
    }

    public boolean isUnknown(@NotNull String resourcePath) {
        return state.getConfigurationSection(keysPath(resourcePath)) == null;
    }

    public void record(@NotNull String resourcePath, @NotNull Map<String, String> defaults) {
        String base = filePath(resourcePath);
        state.set(base, null);
        state.set(base + "." + VERSION_KEY, pluginVersion());

        ConfigurationSection keys = state.createSection(keysPath(resourcePath));
        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            keys.set(escape(entry.getKey()), entry.getValue());
        }
    }

    public void save() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                plugin.getLogger().warning("Could not create " + parent + " for " + LEDGER_PATH + ".");
                return;
            }
            state.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save " + LEDGER_PATH + ": " + e.getMessage());
        }
    }

    public static Map<String, String> flatten(@NotNull ConfigurationSection section) {
        Map<String, String> leaves = new LinkedHashMap<>();
        collect(section, "", leaves);
        return leaves;
    }

    private static void collect(ConfigurationSection section, String prefix, Map<String, String> out) {
        for (String key : section.getKeys(false)) {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            ConfigurationSection child = section.getConfigurationSection(key);

            if (child == null) {
                out.put(path, String.valueOf(section.get(key)));
            } else if (child.getKeys(false).isEmpty()) {
                out.put(path, "");
            } else {
                collect(child, path, out);
            }
        }
    }

    private String pluginVersion() {
        return plugin.getDescription() == null ? "unknown" : plugin.getDescription().getVersion();
    }

    private static String filePath(String resourcePath) {
        return FILES_SECTION + "." + escape(resourcePath);
    }
    private static String keysPath(String resourcePath) {
        return filePath(resourcePath) + "." + KEYS_SECTION;
    }
    private static String escape(String path) {
        return path.replace("~", "~t").replace(".", "~d");
    }
    private static String unescape(String path) {
        return path.replace("~d", ".").replace("~t", "~");
    }
}
