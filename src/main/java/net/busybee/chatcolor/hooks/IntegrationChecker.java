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
                plugin.getLogger().warning("2. Replace the standard {message} variable with %chatcolor_message_mm%");
                plugin.getLogger().warning("   in your chat formats.");
                plugin.getLogger().warning("3. In ChatColor's config.yml, set 'apply-to-message: false'");
                plugin.getLogger().warning("   to avoid double-coloring, and set 'late-bind: true'.");
                plugin.getLogger().warning("======================================================");
            }
        }, 20L);
    }
}
