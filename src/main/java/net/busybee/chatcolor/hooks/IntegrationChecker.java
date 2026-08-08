package net.busybee.chatcolor.hooks;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.utils.SchedulerUtil;
import org.bukkit.Bukkit;

public class IntegrationChecker {

    private final ChatColor plugin;

    public IntegrationChecker(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void check() {
        SchedulerUtil.runDelayedSync(plugin, () -> {
            boolean hasDiscordSRV = Bukkit.getPluginManager().getPlugin("DiscordSRV") != null;
            boolean hasLPC = Bukkit.getPluginManager().getPlugin("LPC") != null;
            boolean hasEssentialsChat = Bukkit.getPluginManager().getPlugin("EssentialsChat") != null;
            boolean hasEssentialsC = Bukkit.getPluginManager().getPlugin("EssentialsC") != null;

            if (hasEssentialsChat || hasEssentialsC) {
                plugin.getLogger().info("======================================================");
                if (hasEssentialsChat) plugin.getLogger().info("[ChatColor] EssentialsChat detected!");
                if (hasEssentialsC) plugin.getLogger().info("[ChatColor] EssentialsC detected!");

                plugin.getLogger().info("Active Chat Priority: " + plugin.getActivePriority().name());
                plugin.getLogger().info("------------------------------------------------------");
                plugin.getLogger().info("Compatibility Tip:");
                plugin.getLogger().info("1. Use the standard {MESSAGE} tag in Essentials config.");
                plugin.getLogger().info("2. No placeholder is needed. ChatColor colours the message");
                plugin.getLogger().info("   after Essentials has formatted it.");
                plugin.getLogger().info("3. On Paper, hex colors and gradients work automatically.");
                plugin.getLogger().info("4. On Spigot, Essentials strips colours from the chat of any");
                plugin.getLogger().info("   player without 'essentials.chat.color', ChatColor's");
                plugin.getLogger().info("   included. Grant that node, or run Paper so ChatColor can");
                plugin.getLogger().info("   colour at render time where nothing can strip it.");
                plugin.getLogger().info("======================================================");
            }

            if (hasDiscordSRV) {
                plugin.getLogger().warning("======================================================");
                plugin.getLogger().warning("[ChatColor] DiscordSRV detected!");
                plugin.getLogger().warning("To make ChatColor work smoothly with DiscordSRV:");
                plugin.getLogger().warning("1. Open DiscordSRV's config.yml.");
                plugin.getLogger().warning("2. If running Paper, set 'UseModernPaperChatEvent: true' in DiscordSRV.");
                plugin.getLogger().warning("3. Discord automatically strips formatting, but ensure");
                plugin.getLogger().warning("   no raw MiniMessage tags (<...>) leak to Discord channel.");
                plugin.getLogger().warning("======================================================");
            }

            if (hasLPC) {
                plugin.getLogger().warning("======================================================");
                plugin.getLogger().warning("[ChatColor] LPC (LuckPermsChat) detected!");
                plugin.getLogger().warning("To make ChatColor gradients work correctly with LPC:");
                plugin.getLogger().warning("1. Open LPC's config.yml.");
                plugin.getLogger().warning("2. Replace the standard {message} variable with %chatcolor_message%");
                plugin.getLogger().warning("   in your chat formats.");
                plugin.getLogger().warning("3. In ChatColor's config.yml, set 'apply-to-message: false'");
                plugin.getLogger().warning("   to avoid double-coloring, and set 'late-bind: true'.");
                plugin.getLogger().warning("======================================================");
            }
        }, 20L);
    }
}
