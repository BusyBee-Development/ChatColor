package net.busybee.chatcolor.data;

import net.busybee.chatcolor.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final ChatColor plugin;
    private final Map<UUID, PlayerColorData> dataMap = new ConcurrentHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerDataManager(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.dataConfig = new net.busybee.chatcolor.utils.ConfigMigrator(plugin).migrate("data/players.yml");
        this.dataFile = new File(plugin.getDataFolder(), "data/players.yml");

        this.dataMap.clear();

        for (String uuidStr : this.dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String type = this.dataConfig.getString(uuidStr + ".type", "NONE");
                String key = this.dataConfig.getString(uuidStr + ".key", null);
                String tag = this.dataConfig.getString(uuidStr + ".tag", null);
                this.dataMap.put(uuid, new PlayerColorData(uuid, type, key, tag));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public synchronized void saveAll() {
        if (this.dataConfig == null) return;

        for (Map.Entry<UUID, PlayerColorData> entry : this.dataMap.entrySet()) {
            String path = entry.getKey().toString();
            PlayerColorData data = entry.getValue();
            this.dataConfig.set(path + ".type", data.getColorType());
            this.dataConfig.set(path + ".key", data.getColorKey());
            this.dataConfig.set(path + ".tag", data.getColorTag());
        }

        try {
            this.dataConfig.save(this.dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.yml: " + e.getMessage());
        }
    }

    public PlayerColorData getData(UUID uuid) {
        return this.dataMap.computeIfAbsent(uuid, id -> new PlayerColorData(id, "NONE", null, null));
    }

    public void setData(UUID uuid, PlayerColorData data) {
        this.dataMap.put(uuid, data);
    }

    public synchronized void save(UUID uuid) {
        PlayerColorData data = this.dataMap.get(uuid);
        if (data == null || this.dataConfig == null) return;
        if (plugin.getDisplayNameService() != null) {
            plugin.getDisplayNameService().refresh(uuid);
        }

        String path = uuid.toString();
        this.dataConfig.set(path + ".type", data.getColorType());
        this.dataConfig.set(path + ".key", data.getColorKey());
        this.dataConfig.set(path + ".tag", data.getColorTag());

        try {
            this.dataConfig.save(this.dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + uuid + ": " + e.getMessage());
        }
    }
}
