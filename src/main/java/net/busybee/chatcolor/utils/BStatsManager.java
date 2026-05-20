package net.busybee.chatcolor.utils;

import net.busybee.chatcolor.ChatColor;
import org.bstats.bukkit.Metrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BStatsManager {
    public BStatsManager(ChatColor plugin) {
        int id = loadId(plugin);
        if (id != -1) {
            new Metrics(plugin, id);
        }
    }

    private int loadId(ChatColor plugin) {
        Properties props = new Properties();
        try (InputStream is = plugin.getResource("bstats.properties")) {
            if (is != null) {
                props.load(is);
                String idStr = props.getProperty("id");
                if (idStr != null) {
                    return Integer.parseInt(idStr.trim());
                }
            }
        } catch (IOException | NumberFormatException ignored) {}
        return -1;
    }
}
