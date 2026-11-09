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

            if (plugin.hasChatManager()) {
                plugin.getLogger().info("======================================================");
                plugin.getLogger().info("[ChatColor] ChatManager detected!");
                plugin.getLogger().info("Active chat hook: " + (plugin.isLegacyHook() ? "LEGACY" : "MODERN")
                        + ", priority " + plugin.getActivePriority().name());
                plugin.getLogger().info("------------------------------------------------------");
                plugin.getLogger().info("Recommended: keep {message} in ChatManager's Chat_Format and leave");
                plugin.getLogger().info("ChatColor on its defaults. ChatColor colours the message before");
                plugin.getLogger().info("ChatManager formats it, no placeholder needed.");
                plugin.getLogger().info("If you use %chatcolor_message% in place of {message} instead, set");
                plugin.getLogger().info("'apply-to-message: false' in ChatColor's config.yml.");
                plugin.getLogger().info("ChatManager's colour permissions only govern codes players type.");
                plugin.getLogger().info("======================================================");
            }

            if (Bukkit.getPluginManager().isPluginEnabled("ZelChat")) {
                plugin.getLogger().info("======================================================");
                plugin.getLogger().info("[ChatColor] ZelChat detected! "
                        + (plugin.getZelChatHook() != null ? "Hooked into its chat modules." : "Hook FAILED, see above."));
                plugin.getLogger().info("Recommended: use {message} in ZelChat's formats, not %chatcolor_message%.");
                plugin.getLogger().info("ChatColor colours {message} inside ZelChat, so colours, gradients and");
                plugin.getLogger().info("patterns all work alongside [item], [inv], [ec] and mentions.");
                plugin.getLogger().info("%chatcolor_message% is plain text to ZelChat, so [item] stays literal.");
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

            boolean hasLPCPro = Bukkit.getPluginManager().getPlugin("LPC-Pro") != null;
            if (hasLPC || hasLPCPro) {
                String name = hasLPCPro ? "LPC-Pro" : "LPC";
                plugin.getLogger().warning("======================================================");
                plugin.getLogger().warning("[ChatColor] " + name + " detected!");
                plugin.getLogger().warning(name + " builds chat from its own copy of the message, so ChatColor can't");
                plugin.getLogger().warning("colour {message}. Use the placeholder instead:");
                plugin.getLogger().warning("1. Put %chatcolor_message% where {message} is in " + name + "'s format"
                        + (hasLPCPro ? " (formats/groups/<group>.yml, the message text)." : " (chat-format)."));
                plugin.getLogger().warning("2. In ChatColor's config.yml set 'apply-to-message: false' and");
                plugin.getLogger().warning("   'late-bind: true'.");
                if (hasLPCPro) {
                    plugin.getLogger().warning("3. Turn off LPC-Pro's own /color command, which takes /color, /chatcolor");
                    plugin.getLogger().warning("   and /colors: 'commands.color.enabled: false' in LPC-Pro's");
                    plugin.getLogger().warning("   advanced/command-settings.yml.");
                } else {
                    plugin.getLogger().warning("LPC 4.x reads its format as MiniMessage; 'papi-output: AUTO' returns");
                    plugin.getLogger().warning("MiniMessage for it (now: "
                            + (plugin.isPapiMiniMessage() ? "MINIMESSAGE" : "LEGACY")
                            + "). If chat stops sending, set 'papi-output: MINIMESSAGE'.");
                }
                plugin.getLogger().warning("======================================================");
            }

            warnAboutTakenCommands();
        }, 20L);
    }

    /**
     * Warns when another plugin owns /color, /chatcolor or /colors. LPC-Pro does this with its own
     * colour shop, and players then never reach ChatColor's menu or commands.
     */
    private void warnAboutTakenCommands() {
        org.bukkit.command.PluginCommand ours = plugin.getCommand("color");
        if (ours == null) return;

        org.bukkit.command.CommandMap map;
        try {
            map = Bukkit.getCommandMap();
        } catch (NoSuchMethodError e) {
            return; // Spigot has no public command map
        }

        java.util.Map<String, java.util.List<String>> takenBy = new java.util.LinkedHashMap<>();
        for (String label : java.util.List.of("color", "chatcolor", "colors")) {
            org.bukkit.command.Command command = map.getCommand(label);
            if (command == null || command == ours) continue;
            String owner = ownerOf(command);
            takenBy.computeIfAbsent(owner, k -> new java.util.ArrayList<>()).add("/" + label);
        }

        for (java.util.Map.Entry<String, java.util.List<String>> entry : takenBy.entrySet()) {
            plugin.getLogger().warning("======================================================");
            plugin.getLogger().warning("[ChatColor] " + entry.getKey() + " has taken " + String.join(", ", entry.getValue())
                    + " from ChatColor.");
            plugin.getLogger().warning("Players typing those reach " + entry.getKey() + ", not ChatColor.");
            plugin.getLogger().warning("/chatcolor:color always reaches ChatColor. To get the short commands back,");
            plugin.getLogger().warning("turn that command off in " + entry.getKey() + "'s config"
                    + ("LPC-Pro".equals(entry.getKey())
                    ? " ('commands.color.enabled: false' in advanced/command-settings.yml)." : "."));
            plugin.getLogger().warning("======================================================");
        }
    }

    /** The plugin behind a command. Some (LPC-Pro) register plain commands, so fall back to the class loader. */
    private static String ownerOf(org.bukkit.command.Command command) {
        if (command instanceof org.bukkit.command.PluginIdentifiableCommand identifiable) {
            return identifiable.getPlugin().getName();
        }
        ClassLoader loader = command.getClass().getClassLoader();
        for (org.bukkit.plugin.Plugin other : Bukkit.getPluginManager().getPlugins()) {
            if (other.getClass().getClassLoader() == loader) {
                return other.getName();
            }
        }
        return "another plugin (" + command.getClass().getName() + ")";
    }

    private long countHdbEntries() {
        long count = plugin.getColorManager().getColorList().stream().filter(e -> e.getHdbId() != null).count();
        count += plugin.getColorManager().getGradientList().stream().filter(e -> e.getHdbId() != null).count();
        count += plugin.getPatternManager().getPatternList().stream().filter(e -> e.getHdbId() != null).count();
        return count;
    }
}
