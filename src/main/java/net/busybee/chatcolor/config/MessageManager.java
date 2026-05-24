package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final ChatColor plugin;
    private FileConfiguration messagesConfig;
    private String prefix;

    public MessageManager(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.messagesConfig = new net.busybee.chatcolor.utils.ConfigMigrator(plugin).migrate("messages.yml");
        this.prefix = this.messagesConfig.getString("prefix", "<dark_gray>[<gradient:blue:aqua>ChatColor<dark_gray>] ");
    }

    public Component get(String key) {
        String raw = this.messagesConfig.getString(key, "<red>Missing message: " + key);
        raw = raw.replace("<prefix>", this.prefix);
        return ColorUtil.colorize(raw);
    }

    public Component get(String key, Map<String, String> placeholders) {
        String raw = this.messagesConfig.getString(key, "<red>Missing message: " + key);
        raw = raw.replace("<prefix>", this.prefix);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return ColorUtil.colorize(raw);
    }

    public java.util.List<String> getStringList(String key) {
        return this.messagesConfig.getStringList(key);
    }
    public String getRaw(String key) {
        return this.messagesConfig.getString(key, "<red>Missing message: " + key);
    }
    public void send(Player player, String key) {
        player.sendMessage(get(key));
    }

    public void send(Player player, String key, String placeholder, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(placeholder, value);
        player.sendMessage(get(key, map));
    }

    public void send(Player player, String key, String placeholder, Component value) {
        String raw = this.messagesConfig.getString(key, "<red>Missing message: " + key);
        raw = raw.replace("<prefix>", this.prefix);
        
        String[] parts = raw.split("<" + placeholder + ">", 2);
        if (parts.length < 2) {
            player.sendMessage(ColorUtil.colorize(raw));
            return;
        }
        
        Component message = ColorUtil.colorize(parts[0])
                .append(value)
                .append(ColorUtil.colorize(parts[1]));
        
        player.sendMessage(message);
    }

    public void send(Player player, String key, Map<String, String> placeholders) {
        player.sendMessage(get(key, placeholders));
    }
}
