package net.busybee.chatcolor.utils;

import net.busybee.chatcolor.ChatColor;

public class Warning {

    public static void send(ChatColor plugin) {
        plugin.getLogger().warning("==================================================");
        plugin.getLogger().warning("⚠️ Notice: Placeholder syntax/handling has changed in v2026.7.2!");
        plugin.getLogger().warning("Please check the documentation to verify your setup:");
        plugin.getLogger().warning("https://busybeedev.net/docs/chatcolor/papi");
        plugin.getLogger().warning("==================================================");
    }
}
