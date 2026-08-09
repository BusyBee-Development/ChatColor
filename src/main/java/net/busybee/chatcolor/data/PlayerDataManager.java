package net.busybee.chatcolor.data;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.utils.ConfigMigrator;
import net.busybee.chatcolor.utils.SchedulerUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerDataManager {

    private static final long FLUSH_DELAY_TICKS = 20L;

    private final ChatColor plugin;
    private final Map<UUID, PlayerColorData> dataMap = new ConcurrentHashMap<>();
    private final Object ioLock = new Object();
    private final AtomicBoolean flushQueued = new AtomicBoolean(false);

    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerDataManager(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "data/players.yml");

        // Player data is records, not settings: there are no defaults to merge in and no
        // documentation to refresh, so it is loaded directly rather than through
        // ConfigMigrator. The one thing it still needs is the relocation an older layout left
        // behind, which is why that lives in a static helper.
        ConfigMigrator.relocateLegacyFile(plugin, "data/players.yml", file);
        FileConfiguration loaded = YamlConfiguration.loadConfiguration(file);

        Map<UUID, PlayerColorData> parsed = new HashMap<>();
        for (String uuidStr : loaded.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String type = loaded.getString(uuidStr + ".type", "NONE");
                String key = loaded.getString(uuidStr + ".key", null);
                String tag = loaded.getString(uuidStr + ".tag", null);
                parsed.put(uuid, new PlayerColorData(uuid, type, key, tag));
            } catch (IllegalArgumentException ignored) {
            }
        }

        synchronized (ioLock) {
            this.dataConfig = loaded;
            this.dataFile = file;
        }
        this.dataMap.putAll(parsed);
        this.dataMap.keySet().removeIf(uuid -> !parsed.containsKey(uuid));
    }

    public PlayerColorData getData(UUID uuid) {
        return this.dataMap.computeIfAbsent(uuid, id -> new PlayerColorData(id, "NONE", null, null));
    }

    public void setData(UUID uuid, PlayerColorData data) {
        this.dataMap.put(uuid, data);
    }

    public int clearColor(String colorType, String colorKey) {
        if (colorType == null || colorKey == null) return 0;

        int cleared = 0;
        for (Map.Entry<UUID, PlayerColorData> entry : this.dataMap.entrySet()) {
            PlayerColorData data = entry.getValue();
            if (colorType.equals(data.getColorType()) && colorKey.equalsIgnoreCase(data.getColorKey())) {
                data.reset();
                save(entry.getKey());
                cleared++;
            }
        }
        return cleared;
    }

    public void save(UUID uuid) {
        PlayerColorData data = this.dataMap.get(uuid);
        if (data == null) return;
        if (plugin.getDisplayNameService() != null) {
            plugin.getDisplayNameService().refresh(uuid);
        }

        synchronized (ioLock) {
            if (this.dataConfig == null) return;
            write(uuid, data);
        }

        queueFlush();
    }

    public void saveAll() {
        synchronized (ioLock) {
            if (this.dataConfig == null) return;
            for (Map.Entry<UUID, PlayerColorData> entry : this.dataMap.entrySet()) {
                write(entry.getKey(), entry.getValue());
            }
            writeToDisk();
        }
    }

    private void queueFlush() {
        if (!flushQueued.compareAndSet(false, true)) return;

        if (!plugin.isEnabled()) {
            flushQueued.set(false);
            flushNow();
            return;
        }

        try {
            SchedulerUtil.runDelayedAsync(plugin, () -> {
                flushQueued.set(false);
                flushNow();
            }, FLUSH_DELAY_TICKS);
        } catch (IllegalPluginAccessException | IllegalStateException | IllegalArgumentException e) {
            flushQueued.set(false);
            flushNow();
        }
    }

    private void flushNow() {
        synchronized (ioLock) {
            writeToDisk();
        }
    }

    private void write(UUID uuid, PlayerColorData data) {
        String path = uuid.toString();
        this.dataConfig.set(path + ".type", data.getColorType());
        this.dataConfig.set(path + ".key", data.getColorKey());
        this.dataConfig.set(path + ".tag", data.getColorTag());
    }

    private void writeToDisk() {
        if (this.dataConfig == null || this.dataFile == null) return;
        try {
            this.dataConfig.save(this.dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.yml: " + e.getMessage());
        }
    }
}
