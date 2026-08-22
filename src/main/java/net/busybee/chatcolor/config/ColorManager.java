package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.SchedulerUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ColorManager {

    public enum SaveResult {
        CREATED,
        UPDATED,
        DELETED,
        INVALID_NAME,
        INVALID_TAG,
        INVALID_ICON,
        RESERVED_KEY,
        NOT_FOUND,
        NOT_LOADED;

        public boolean isSuccess() {
            return this == CREATED || this == UPDATED || this == DELETED;
        }
    }

    private static final List<String> PUBLIC_ALIASES = List.of("none", "public", "everyone", "all");

    private static final String CUSTOM_SECTION = "custom-colors";
    private static final long FLUSH_DELAY_TICKS = 20L;

    private final ChatColor plugin;
    private final File file;
    private final Object ioLock = new Object();
    private final AtomicBoolean flushQueued = new AtomicBoolean(false);

    private FileConfiguration config;
    private volatile Map<String, ColorEntry> colors = Collections.emptyMap();
    private volatile Map<String, GradientEntry> gradients = Collections.emptyMap();
    private volatile Map<String, ColorEntry> customColors = Collections.emptyMap();

    public ColorManager(ChatColor plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "colors/colors.yml");
    }

    public void load() {
        FileConfiguration loaded = new net.busybee.chatcolor.utils.ConfigMigrator(plugin)
                .migrate("colors/colors.yml");

        synchronized (ioLock) {
            this.config = loaded;
        }
        rebuild();
    }

    private void rebuild() {
        FileConfiguration snapshot;
        synchronized (ioLock) {
            snapshot = this.config;
        }

        if (snapshot == null) {
            this.colors = Collections.emptyMap();
            this.gradients = Collections.emptyMap();
            this.customColors = Collections.emptyMap();
            return;
        }

        Map<String, ColorEntry> loadedColors = new LinkedHashMap<>();
        Map<String, GradientEntry> loadedGradients = new LinkedHashMap<>();
        Map<String, ColorEntry> loadedCustom = new LinkedHashMap<>();

        if (plugin.getConfigManager().isShowStandardColors()) {
            loadSection(snapshot.getConfigurationSection("colors"), loadedColors,
                    "chatcolor.color.", ColorEntry::new);
        }

        if (plugin.getConfigManager().isShowStandardGradients()) {
            loadSection(snapshot.getConfigurationSection("gradients"), loadedGradients,
                    "chatcolor.gradient.", GradientEntry::new);
        }

        loadSection(snapshot.getConfigurationSection(CUSTOM_SECTION), loadedCustom,
                "chatcolor.custom.", ColorEntry::new);

        this.colors = Collections.unmodifiableMap(loadedColors);
        this.gradients = Collections.unmodifiableMap(loadedGradients);
        this.customColors = Collections.unmodifiableMap(loadedCustom);
    }

    @FunctionalInterface
    private interface EntryFactory<T> {
        T create(String key, String displayName, String tag, String permission, String icon, PermissionDefault def, String hdbId);
    }

    private <T> void loadSection(ConfigurationSection section, Map<String, T> map,
                                 String defaultPermPrefix, EntryFactory<T> factory) {
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String displayName = section.getString(key + ".display-name", key);

            String tag = section.getString(key + ".tag", "<white>");
            if (tag == null || tag.isBlank()) tag = "<white>";
            tag = ColorUtil.normalizeTag(tag);
            String permission = section.getString(key + ".permission", defaultPermPrefix + key);
            if (permission != null && PUBLIC_ALIASES.contains(permission.trim().toLowerCase(Locale.ROOT))) {
                permission = "";
            }

            String icon = section.getString(key + ".icon", "WHITE_WOOL");
            String hdbId = section.getString(key + ".hdb-id", null);
            PermissionDefault permissionDefault = parsePermissionDefault(
                    section.getString(key + ".default"), section.getName() + "." + key);

            map.put(key, factory.create(key, displayName, tag, permission, icon, permissionDefault, hdbId));
        }
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

    public SaveResult saveCustomColor(String name, String tag, String icon, String permission,
                                     String permissionDefault) {
        String key = sanitizeKey(name);
        if (key.isEmpty()) {
            return SaveResult.INVALID_NAME;
        }
        if (this.colors.containsKey(key) || this.gradients.containsKey(key)) {
            return SaveResult.RESERVED_KEY;
        }
        if (!ColorUtil.isValidTag(tag)) {
            return SaveResult.INVALID_TAG;
        }

        Material material = resolveIcon(icon);
        if (material == null) {
            return SaveResult.INVALID_ICON;
        }

        String node;
        if (permission == null) {
            node = "chatcolor.custom." + key;
        } else if (permission.isBlank() || PUBLIC_ALIASES.contains(permission.trim().toLowerCase(Locale.ROOT))) {
            node = "";
        } else {
            node = permission.trim();
        }

        boolean existed = this.customColors.containsKey(key);

        synchronized (ioLock) {
            if (this.config == null) return SaveResult.NOT_LOADED;

            String base = CUSTOM_SECTION + "." + key + ".";
            this.config.set(base + "display-name", name);
            this.config.set(base + "tag", ColorUtil.normalizeTag(tag));
            this.config.set(base + "icon", material.name());
            this.config.set(base + "permission", node);
            if (permissionDefault != null && !permissionDefault.isBlank()) {
                this.config.set(base + "default", permissionDefault.trim());
            }
        }

        rebuild();
        queueFlush();
        return existed ? SaveResult.UPDATED : SaveResult.CREATED;
    }

    public SaveResult saveCustomColor(String name, String tag, String icon, String permission) {
        return saveCustomColor(name, tag, icon, permission, null);
    }

    public SaveResult deleteCustomColor(String name) {
        String key = sanitizeKey(name);
        if (key.isEmpty() || !this.customColors.containsKey(key)) {
            return SaveResult.NOT_FOUND;
        }

        synchronized (ioLock) {
            if (this.config == null) return SaveResult.NOT_LOADED;
            this.config.set(CUSTOM_SECTION + "." + key, null);
        }

        rebuild();
        queueFlush();
        return SaveResult.DELETED;
    }

    static Material resolveIcon(String icon) {
        if (icon == null || icon.isBlank()) return null;

        Material material = Material.matchMaterial(icon.trim());
        if (material == null) return null;

        try {
            return material.isItem() ? material : null;
        } catch (NoClassDefFoundError | ExceptionInInitializerError | IllegalStateException noRegistry) {
            return material;
        }
    }

    public static String sanitizeKey(String name) {
        if (name == null) return "";
        return name.toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replaceAll("[^a-z0-9_-]", "");
    }

    private void queueFlush() {
        if (!flushQueued.compareAndSet(false, true)) return;

        if (!plugin.isEnabled()) {
            flushQueued.set(false);
            flush();
            return;
        }

        try {
            SchedulerUtil.runDelayedAsync(plugin, () -> {
                flushQueued.set(false);
                flush();
            }, FLUSH_DELAY_TICKS);
        } catch (IllegalPluginAccessException | IllegalStateException | IllegalArgumentException e) {
            flushQueued.set(false);
            flush();
        }
    }

    public void flush() {
        synchronized (ioLock) {
            if (this.config == null) return;
            try {
                this.config.save(this.file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save colors.yml: " + e.getMessage());
            }
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
        if (key == null) return null;
        ColorEntry entry = this.colors.get(key);
        if (entry == null) entry = this.customColors.get(key);
        return entry;
    }

    public GradientEntry getGradient(String key) {
        return key == null ? null : this.gradients.get(key);
    }
}
