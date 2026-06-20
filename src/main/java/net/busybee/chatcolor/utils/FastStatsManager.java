package net.busybee.chatcolor.utils;

import dev.faststats.bukkit.BukkitContext;
import dev.faststats.ErrorTracker;
import dev.faststats.data.Metric;
import net.busybee.chatcolor.ChatColor;

public class FastStatsManager {
    private static final String FAST_STATS_TOKEN = "30d8d244e27052da0508ee242ee389ff";
    private final ChatColor plugin;
    private final BukkitContext context;

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .anonymize("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "[uuid hidden]")
            .ignoreError(java.lang.reflect.InvocationTargetException.class);

    public FastStatsManager(ChatColor plugin) {
        this.plugin = plugin;

        this.context = new BukkitContext.Factory(plugin, FAST_STATS_TOKEN)
                .errorTrackerService(ERROR_TRACKER)
                .metrics(factory -> factory
                        .addMetric(Metric.number("total_colors", () -> (double) plugin.getColorManager().getColorList().size()))
                        .addMetric(Metric.number("total_gradients", () -> (double) plugin.getColorManager().getGradientList().size()))
                        .addMetric(Metric.number("total_patterns", () -> (double) plugin.getPatternManager().getPatternList().size()))
                        .create())
                .create();
    }

    public void onEnable() {
        context.ready();
        plugin.getLogger().info("FastStats metrics have been enabled!");
    }

    public void onDisable() {
        context.shutdown();
    }
}
