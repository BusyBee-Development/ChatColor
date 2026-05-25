package net.busybee.chatcolor.utils;

import net.busybee.chatcolor.ChatColor;
import org.bstats.bukkit.Metrics;

public class BStatsManager {
    private static final int BSTATS_ID = 30495;

    public BStatsManager(ChatColor plugin) {
        new Metrics(plugin, BSTATS_ID);
    }
}
