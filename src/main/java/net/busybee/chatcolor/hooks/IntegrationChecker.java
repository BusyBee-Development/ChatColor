package net.busybee.chatcolor.hooks;

import net.busybee.chatcolor.ChatColor;
import org.bukkit.Bukkit;

public class IntegrationChecker {

    private final ChatColor plugin;

    public IntegrationChecker(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void check() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            boolean hasDiscordSRV = Bukkit.getPluginManager().getPlugin("DiscordSRV") != null;
            boolean hasLPC = Bukkit.getPluginManager().getPlugin("LPC") != null;

            if (hasDiscordSRV) {
                plugin.getLogger().warning("======================================================");
                plugin.getLogger().warning("[ChatColor] DiscordSRV detected!");
                plugin.getLogger().warning("To make ChatColor work smoothly with DiscordSRV:");
                plugin.getLogger().warning("1. Open DiscordSRV's config.yml.");
                plugin.getLogger().warning("2. Ensure PlaceholderAPI is enabled in DiscordSRV.");
                plugin.getLogger().warning("3. If you want the colored message in Discord, replace {message}");
                plugin.getLogger().warning("   with %chatcolor_message% in the format strings.");
                plugin.getLogger().warning("   (Note: Discord itself doesn't support RGB gradients well,");
                plugin.getLogger().warning("   so it might look plain or use basic markdown).");
                plugin.getLogger().warning("4. Alternatively, use %chatcolor_color% before the name.");
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
