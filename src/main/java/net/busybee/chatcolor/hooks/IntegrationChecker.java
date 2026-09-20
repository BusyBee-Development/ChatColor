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
            boolean hasHeadDatabase = Bukkit.getPluginManager().getPlugin("HeadDatabase") != null;

            if (!hasHeadDatabase) {
                long hdbEntries = countHdbEntries();
                if (hdbEntries > 0) {
                    plugin.getLogger().info("[ChatColor] " + hdbEntries + " color/pattern entr"
                            + (hdbEntries == 1 ? "y uses" : "ies use")
                            + " an hdb-id icon, but HeadDatabase isn't installed. Falling back to each entry's configured icon material.");
                }
            }

            if (hasEssentialsChat || hasEssentialsC) {
                plugin.getLogger().info("======================================================");
                if (hasEssentialsChat) plugin.getLogger().info("[ChatColor] EssentialsChat detected!");
                if (hasEssentialsC) plugin.getLogger().info("[ChatColor] EssentialsC detected!");

                plugin.getLogger().info("Active Chat Priority: " + plugin.getActivePriority().name());
                plugin.getLogger().info("------------------------------------------------------");
                plugin.getLogger().info("Compatibility Tip:");
                plugin.getLogger().info("1. Use the standard {MESSAGE} tag in Essentials config.");
                plugin.getLogger().info("2. No placeholder is needed. Essentials formats a message");
                plugin.getLogger().info("   ChatColor has already coloured.");
                plugin.getLogger().info("3. Essentials' chat renderer ignores whatever the renderer");
                plugin.getLogger().info("   chain hands it, so ChatColor colours the message itself");
                plugin.getLogger().info("   from in front of Essentials instead of rendering. Leave");
                plugin.getLogger().info("   event-priority and message-mode on their defaults.");
                plugin.getLogger().info("4. Essentials strips colours from the chat of any player");
                plugin.getLogger().info("   without 'essentials.chat.color' and 'essentials.chat.rgb',");
                plugin.getLogger().info("   but it does that before ChatColor runs, so ours survive.");
                plugin.getLogger().info("   Those nodes only govern codes players type themselves.");
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
                plugin.getLogger().warning("Recommended: keep {message} in LPC's format and leave ChatColor on its");
                plugin.getLogger().warning("defaults ('apply-to-message: true', 'late-bind: false'). ChatColor");
                plugin.getLogger().warning("colours the message itself, no placeholder needed.");
                plugin.getLogger().warning("Only if you want the colour placed by LPC's own format instead:");
                plugin.getLogger().warning("1. Use %chatcolor_message% in place of {message} in LPC's format.");
                plugin.getLogger().warning("2. In ChatColor's config.yml set 'apply-to-message: false' and");
                plugin.getLogger().warning("   'late-bind: true'.");
                plugin.getLogger().warning("LPC 4.x reads its format as MiniMessage; 'papi-output: AUTO' returns");
                plugin.getLogger().warning("MiniMessage for it (now: "
                        + (plugin.isPapiMiniMessage() ? "MINIMESSAGE" : "LEGACY")
                        + "). If chat stops");
                plugin.getLogger().warning("sending, set 'papi-output: MINIMESSAGE'.");
                plugin.getLogger().warning("======================================================");
            }
        }, 20L);
    }

    private long countHdbEntries() {
        long count = plugin.getColorManager().getColorList().stream().filter(e -> e.getHdbId() != null).count();
        count += plugin.getColorManager().getGradientList().stream().filter(e -> e.getHdbId() != null).count();
        count += plugin.getPatternManager().getPatternList().stream().filter(e -> e.getHdbId() != null).count();
        return count;
    }
}
