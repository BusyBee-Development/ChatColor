package net.busybee.chatcolor.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ConfigMigrator {

    private static final int BACKUPS_KEPT = 10;
    private static final Map<String, Map<String, String>> RENAMES = Map.of();
    private final Plugin plugin;
    private final List<String> addedKeys = new ArrayList<>();
    private final List<String> removedKeys = new ArrayList<>();
    private final List<String> renamedKeys = new ArrayList<>();
    private final List<String> conflicts = new ArrayList<>();

    private Map<String, String> shipped = Collections.emptyMap();
    private Set<String> addable = Collections.emptySet();
    private Set<String> retirable = Collections.emptySet();

    public ConfigMigrator(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration migrate(@NotNull String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);

        if (fileName.contains("/") || fileName.contains("\\")) {
            relocateLegacyFile(plugin, fileName, configFile);
        }

        return migrate(fileName, configFile);
    }

    public static void relocateLegacyFile(@NotNull Plugin plugin, @NotNull String fileName,
                                          @NotNull File target) {
        if (target.exists()) {
            return;
        }

        String simpleName = new File(fileName).getName();
        File[] candidates = {
                new File(plugin.getDataFolder(), simpleName),
                new File(plugin.getDataFolder(), "configs/" + simpleName),
        };

        for (File candidate : candidates) {
            if (!candidate.exists()) {
                continue;
            }
            File parent = target.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                plugin.getLogger().warning("Could not create " + parent + " to move " + simpleName + " into.");
                return;
            }
            if (candidate.renameTo(target)) {
                plugin.getLogger().info("Migrated " + candidate.getName() + " to " + fileName);
            }
            return;
        }
    }

    public FileConfiguration migrate(@NotNull String resourcePath, @NotNull File configFile) {
        addedKeys.clear();
        removedKeys.clear();
        renamedKeys.clear();
        conflicts.clear();

        if (!configFile.exists()) {
            return createFromResource(resourcePath, configFile);
        }

        FileConfiguration userConfig = new YamlConfiguration();
        try {
            userConfig.load(configFile);
            applyOptions(userConfig);
        } catch (InvalidConfigurationException | IOException e) {
            plugin.getLogger().severe("Detected corruption in " + configFile.getName() + "! Backing up and regenerating...");
            createBackupRaw(configFile, true);

            if (!configFile.delete()) {
                plugin.getLogger().severe("Could not delete the corrupted " + configFile.getName() + ".");
                return userConfig;
            }
            return migrate(resourcePath, configFile);
        }

        FileConfiguration defaultConfig = loadResource(resourcePath);
        if (defaultConfig == null) {
            return userConfig;
        }

        ConfigLedger ledger = new ConfigLedger(plugin);
        Map<String, String> defaults = ConfigLedger.flatten(defaultConfig);
        prepare(ledger, resourcePath, userConfig, defaults);

        applyRenames(resourcePath, userConfig);

        if (!needsMigration(userConfig, defaultConfig)) {
            ledger.record(resourcePath, defaults);
            ledger.save();
            return userConfig;
        }

        createBackupRaw(configFile, false);
        FileConfiguration mergedConfig = mergeAndReorder(userConfig, defaultConfig);

        if (!save(mergedConfig, configFile)) {
            return null;
        }

        ledger.record(resourcePath, defaults);
        ledger.save();
        logMigrationSummary(configFile.getName());
        return mergedConfig;
    }

    private FileConfiguration createFromResource(String resourcePath, File configFile) {
        try (InputStream in = plugin.getResource(resourcePath)) {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                plugin.getLogger().warning("Could not create " + parent + " for " + configFile.getName() + ".");
            }
            if (in != null) {
                Files.copy(in, configFile.toPath());
                plugin.getLogger().info("Created new " + configFile.getName() + " from " + resourcePath);
            } else if (!configFile.createNewFile() && !configFile.exists()) {
                plugin.getLogger().warning("Could not create " + configFile.getName() + ".");
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not create default " + configFile.getName() + " from " + resourcePath);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        applyOptions(config);
        FileConfiguration defaultConfig = loadResource(resourcePath);
        if (defaultConfig != null) {
            ConfigLedger ledger = new ConfigLedger(plugin);
            ledger.record(resourcePath, ConfigLedger.flatten(defaultConfig));
            ledger.save();
        }
        return config;
    }

    private FileConfiguration loadResource(String resourcePath) {
        InputStream defaultStream = plugin.getResource(resourcePath);
        if (defaultStream == null) {
            return null;
        }

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
        );
        applyOptions(defaultConfig);
        return defaultConfig;
    }

    private void prepare(ConfigLedger ledger, String resourcePath, ConfigurationSection userConfig,
                         Map<String, String> defaults) {
        this.shipped = ledger.shippedKeys(resourcePath);

        if (ledger.isUnknown(resourcePath)) {
            this.addable = defaults.keySet();
            this.retirable = Collections.emptySet();
            return;
        }

        Set<String> newKeys = new LinkedHashSet<>(defaults.keySet());
        newKeys.removeAll(shipped.keySet());
        this.addable = newKeys;
        Set<String> retired = new LinkedHashSet<>();
        Map<String, String> current = ConfigLedger.flatten(userConfig);
        for (Map.Entry<String, String> entry : shipped.entrySet()) {
            String path = entry.getKey();
            if (defaults.containsKey(path) || !current.containsKey(path)) {
                continue;
            }
            if (Objects.equals(current.get(path), entry.getValue())) {
                retired.add(path);
            } else {
                conflicts.add(path + " is no longer used, but you have customised it - left in place");
            }
        }
        this.retirable = retired;
    }

    protected Map<String, String> renamesFor(String resourcePath) {
        return RENAMES.getOrDefault(resourcePath, Collections.emptyMap());
    }

    private void applyRenames(String resourcePath, ConfigurationSection userConfig) {
        Map<String, String> renames = renamesFor(resourcePath);
        if (renames.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> rename : renames.entrySet()) {
            String from = rename.getKey();
            String to = rename.getValue();
            if (!userConfig.contains(from) || userConfig.contains(to)) {
                continue;
            }
            userConfig.set(to, userConfig.get(from));
            userConfig.set(from, null);
            renamedKeys.add(from + " -> " + to);
        }
    }

    private FileConfiguration mergeAndReorder(@NotNull FileConfiguration userConfig, @NotNull FileConfiguration defaultConfig) {
        YamlConfiguration newConfig = new YamlConfiguration();
        applyOptions(newConfig);
        newConfig.options().setHeader(defaultConfig.options().getHeader());
        newConfig.options().setFooter(defaultConfig.options().getFooter());

        transferSection(userConfig, defaultConfig, newConfig, "");
        return newConfig;
    }

    private void applyOptions(FileConfiguration config) {
        try {
            config.options().getClass().getMethod("useQuotes", boolean.class).invoke(config.options(), true);
        } catch (Exception ignored) {}
    }

    private void transferSection(ConfigurationSection userConfig, ConfigurationSection defaultConfig, ConfigurationSection newConfig, String path) {
        for (String key : defaultConfig.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (defaultConfig.isConfigurationSection(key)) {
                ConfigurationSection userSubSection = userConfig != null ? userConfig.getConfigurationSection(key) : null;
                boolean typeConflict = userSubSection == null && userConfig != null && userConfig.contains(key);
                if (typeConflict) {
                    conflicts.add(fullPath + " changed from a value to a section - your old value is in the backup");
                }

                if (userSubSection == null && !typeConflict
                        && !sectionHasAddableLeaf(fullPath, defaultConfig.getConfigurationSection(key))) {
                    continue;
                }

                ConfigurationSection newSubSection = newConfig.createSection(key);

                newConfig.setComments(key, defaultConfig.getComments(key));
                newConfig.setInlineComments(key, defaultConfig.getInlineComments(key));

                ConfigurationSection defaultSubSection = defaultConfig.getConfigurationSection(key);

                if (defaultSubSection != null) {
                    transferSection(userSubSection,
                                    defaultSubSection, newSubSection, fullPath);
                }
            } else {
                Object value;
                if (userConfig != null && userConfig.contains(key) && !userConfig.isConfigurationSection(key)) {
                    value = userConfig.get(key);
                    if (value == null) {
                        value = "";
                    }
                } else if (addable.contains(fullPath)) {
                    value = defaultConfig.get(key);
                    addedKeys.add(fullPath);
                } else {
                    continue;
                }

                newConfig.set(key, value);
                newConfig.setComments(key, defaultConfig.getComments(key));
                newConfig.setInlineComments(key, defaultConfig.getInlineComments(key));
            }
        }

        if (userConfig != null) {
            for (String userKey : userConfig.getKeys(false)) {
                if (!defaultConfig.contains(userKey)) {
                    String fullPath = path.isEmpty() ? userKey : path + "." + userKey;
                    if (retirable.contains(fullPath)) {
                        removedKeys.add(fullPath);
                        continue;
                    }
                    if (userConfig.isConfigurationSection(userKey)) {
                        ConfigurationSection userSubSection = userConfig.getConfigurationSection(userKey);
                        ConfigurationSection newSubSection = newConfig.createSection(userKey);
                        newConfig.setComments(userKey, userConfig.getComments(userKey));
                        newConfig.setInlineComments(userKey, userConfig.getInlineComments(userKey));
                        if (userSubSection != null) {
                            copyRecursive(userSubSection, newSubSection);
                        }
                    } else {
                        newConfig.set(userKey, userConfig.get(userKey));
                        newConfig.setComments(userKey, userConfig.getComments(userKey));
                        newConfig.setInlineComments(userKey, userConfig.getInlineComments(userKey));
                    }
                }
            }
        }
    }

    private boolean sectionHasAddableLeaf(String fullPath, ConfigurationSection defaultSubSection) {
        if (defaultSubSection == null) {
            return addable.contains(fullPath);
        }

        Set<String> leaves = ConfigLedger.flatten(defaultSubSection).keySet();
        if (leaves.isEmpty()) {
            return addable.contains(fullPath);
        }
        for (String leaf : leaves) {
            if (addable.contains(fullPath + "." + leaf)) {
                return true;
            }
        }
        return false;
    }

    private void copyRecursive(ConfigurationSection from, ConfigurationSection to) {
        for (String key : from.getKeys(false)) {
            if (from.isConfigurationSection(key)) {
                copyRecursive(from.getConfigurationSection(key), to.createSection(key));
            } else {
                to.set(key, from.get(key));
            }
            to.setComments(key, from.getComments(key));
            to.setInlineComments(key, from.getInlineComments(key));
        }
    }

    private boolean needsMigration(@NotNull FileConfiguration userConfig, @NotNull FileConfiguration defaultConfig) {
        if (!renamedKeys.isEmpty() || !retirable.isEmpty()) {
            return true;
        }

        Map<String, String> current = ConfigLedger.flatten(userConfig);
        for (String key : addable) {
            if (!current.containsKey(key)) {
                return true;
            }
        }

        if (!defaultConfig.options().getHeader().equals(userConfig.options().getHeader())
                || !defaultConfig.options().getFooter().equals(userConfig.options().getFooter())) {
            return true;
        }

        return commentsDiffer(userConfig, defaultConfig);
    }

    private boolean commentsDiffer(ConfigurationSection userConfig, ConfigurationSection defaultConfig) {
        for (String key : defaultConfig.getKeys(false)) {
            if (!userConfig.contains(key)) {
                continue;
            }
            if (!defaultConfig.getComments(key).equals(userConfig.getComments(key))
                    || !defaultConfig.getInlineComments(key).equals(userConfig.getInlineComments(key))) {
                return true;
            }

            ConfigurationSection defaultSub = defaultConfig.getConfigurationSection(key);
            ConfigurationSection userSub = userConfig.getConfigurationSection(key);
            if (defaultSub != null && userSub != null && commentsDiffer(userSub, defaultSub)) {
                return true;
            }
        }
        return false;
    }

    private boolean save(FileConfiguration config, File configFile) {
        File temp = new File(configFile.getParentFile(), configFile.getName() + ".tmp");
        try {
            config.save(temp);
            Path from = temp.toPath();
            Path to = configFile.toPath();
            try {
                Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save migrated " + configFile.getName() + ": " + e.getMessage());
            try {
                Files.deleteIfExists(temp.toPath());
            } catch (IOException ignored) {}
            return false;
        }
    }

    private void createBackupRaw(@NotNull File configFile, boolean isCorrupted) {
        File backupsDir = new File(configFile.getParentFile(), "backups");
        if (!backupsDir.isDirectory() && !backupsDir.mkdirs()) {
            plugin.getLogger().warning("Could not create the backups directory; skipping backup.");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String backupFileName = configFile.getName() + (isCorrupted ? ".corrupted-" : ".backup-") + timestamp;
        File backupFile = new File(backupsDir, backupFileName);

        try {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Created " + (isCorrupted ? "corrupted file backup" : "backup") + ": backups/" + backupFileName);
            pruneBackups(backupsDir, configFile.getName());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create backup: " + e.getMessage());
        }
    }

    private void pruneBackups(File backupsDir, String baseName) {
        File[] existing = backupsDir.listFiles((dir, name) -> name.startsWith(baseName + ".backup-"));
        if (existing == null || existing.length <= BACKUPS_KEPT) {
            return;
        }

        Arrays.sort(existing, (a, b) -> b.getName().compareTo(a.getName()));
        for (int i = BACKUPS_KEPT; i < existing.length; i++) {
            if (!existing[i].delete()) {
                plugin.getLogger().warning("Could not prune old backup " + existing[i].getName() + ".");
            }
        }
    }

    private void logMigrationSummary(@NotNull String fileName) {
        if (addedKeys.isEmpty() && removedKeys.isEmpty() && renamedKeys.isEmpty() && conflicts.isEmpty()) {
            plugin.getLogger().info(fileName + " updated (documentation refreshed, settings unchanged)");
            return;
        }

        plugin.getLogger().info("=== " + fileName + " Migration Summary ===");
        plugin.getLogger().info("Your existing settings were kept. Previous copy: backups/");

        logKeys("Added " + addedKeys.size() + " new key(s):", "+ ", addedKeys);
        logKeys("Removed " + removedKeys.size() + " obsolete key(s):", "- ", removedKeys);
        logKeys("Moved " + renamedKeys.size() + " renamed key(s):", "~ ", renamedKeys);
        logKeys("Needs your attention:", "! ", conflicts);

        plugin.getLogger().info("=== Migration Complete ===");
    }

    private void logKeys(String heading, String bullet, List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }
        plugin.getLogger().info(heading);
        for (String key : keys) {
            plugin.getLogger().info("  " + bullet + key);
        }
    }

    public List<String> getAddedKeys() {
        return new ArrayList<>(addedKeys);
    }
    public List<String> getRemovedKeys() {
        return new ArrayList<>(removedKeys);
    }
    public List<String> getRenamedKeys() {
        return new ArrayList<>(renamedKeys);
    }
    public List<String> getConflicts() {
        return new ArrayList<>(conflicts);
    }
}
