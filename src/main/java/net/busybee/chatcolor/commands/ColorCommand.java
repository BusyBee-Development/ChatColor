package net.busybee.chatcolor.commands;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.config.ColorManager;
import net.busybee.chatcolor.inventory.impl.MainMenuGUI;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import net.busybee.chatcolor.models.PatternEntry;
import net.busybee.chatcolor.models.SelectableEntry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ColorCommand implements CommandExecutor, TabCompleter {

    private final ChatColor plugin;

    public ColorCommand(ChatColor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (!player.hasPermission("chatcolor.use")) {
                    plugin.getMessageManager().send(player, "no-permission-command");
                    return true;
                }
                new MainMenuGUI(plugin).open(player);
            } else {
                String usageHint = plugin.getMessageManager().getRaw("usage-set");
                sender.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", usageHint)));
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reset" -> {
                Player target;
                if (args.length > 1) {
                    if (!sender.hasPermission("chatcolor.admin")) {
                        plugin.getMessageManager().send(sender, "no-permission-command");
                        return true;
                    }
                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        plugin.getMessageManager().send(sender, "unknown-player", "player", args[1]);
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player player)) {
                        plugin.getMessageManager().send(sender, "console-must-specify-player");
                        return true;
                    }
                    target = player;
                }
                plugin.getChatColorAPI().resetColor(target);
                if (target == sender) {
                    plugin.getMessageManager().send(target, "color-reset");
                } else {
                    plugin.getMessageManager().send(sender, "color-reset-other", "player", target.getName());
                }
            }
            case "debug" -> {
                if (!sender.hasPermission("chatcolor.debug")) {
                    plugin.getMessageManager().send(sender, "no-permission-command");
                    return true;
                }
                handleDebug(sender, args);
            }
            case "reload" -> {
                if (!sender.hasPermission("chatcolor.reload")) {
                    plugin.getMessageManager().send(sender, "no-permission-command");
                    return true;
                }
                plugin.reload();
                plugin.getMessageManager().send(sender, "config-reloaded");
            }
            case "set" -> {
                if (args.length < 3) {
                    String usageHint = plugin.getMessageManager().getRaw("usage-set");
                    sender.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", usageHint)));
                    return true;
                }
                
                Player target;
                if (args.length > 3) {
                    if (!sender.hasPermission("chatcolor.admin")) {
                        plugin.getMessageManager().send(sender, "no-permission-command");
                        return true;
                    }
                    target = Bukkit.getPlayer(args[3]);
                    if (target == null) {
                        plugin.getMessageManager().send(sender, "unknown-player", "player", args[3]);
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player player)) {
                        plugin.getMessageManager().send(sender, "console-must-specify-player");
                        return true;
                    }
                    target = player;
                }

                String type = args[1].toLowerCase();
                String key = args[2].toLowerCase();
                handleSet(sender, target, type, key);
            }
            case "gui" -> {
                Player target;
                if (args.length > 1) {
                    if (!sender.hasPermission("chatcolor.admin")) {
                        plugin.getMessageManager().send(sender, "no-permission-command");
                        return true;
                    }
                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        plugin.getMessageManager().send(sender, "unknown-player", "player", args[1]);
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player player)) {
                        plugin.getMessageManager().send(sender, "console-must-specify-player");
                        return true;
                    }
                    target = player;
                }
                new MainMenuGUI(plugin).open(target);
            }
            case "create" -> {
                if (!sender.hasPermission("chatcolor.create")) {
                    plugin.getMessageManager().send(sender, "no-permission-command");
                    return true;
                }
                if (args.length < 4) {
                    sendUsage(sender, "usage-create");
                    return true;
                }
                handleCreate(sender, args);
            }
            case "delete", "remove" -> {
                if (!sender.hasPermission("chatcolor.create")) {
                    plugin.getMessageManager().send(sender, "no-permission-command");
                    return true;
                }
                if (args.length < 2) {
                    sendUsage(sender, "usage-delete");
                    return true;
                }
                handleDelete(sender, args[1]);
            }
            case "list" -> {
                if (!sender.hasPermission("chatcolor.use")) {
                    plugin.getMessageManager().send(sender, "no-permission-command");
                    return true;
                }
                handleList(sender, args.length > 1 ? args[1].toLowerCase() : "colors");
            }
            default -> {
                if (sender instanceof Player player) {
                    if (player.hasPermission("chatcolor.use")) {
                        new MainMenuGUI(plugin).open(player);
                    } else {
                        plugin.getMessageManager().send(player, "no-permission-command");
                    }
                } else {
                    String usageHint = plugin.getMessageManager().getRaw("usage-set");
                    sender.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", usageHint)));
                }
            }
        }

        return true;
    }

    private void handleDebug(CommandSender sender, String[] args) {
        net.busybee.chatcolor.utils.ChatDebugger debug = plugin.getChatDebugger();
        if (debug == null) {
            sender.sendMessage("ChatColor debug is not available yet, the chat hook is still binding.");
            return;
        }

        String mode = args.length > 1 ? args[1].toLowerCase() : "";
        switch (mode) {
            case "off" -> {
                debug.stop();
                sender.sendMessage("ChatColor debug off.");
                return;
            }
            case "pipeline" -> {
                debug.dumpPipeline();
                sender.sendMessage("ChatColor pipeline dumped to console.");
                return;
            }
            case "all" -> {
                boolean on = debug.toggleAll();
                sender.sendMessage("ChatColor debug for everyone: " + (on ? "ON" : "OFF"));
                if (on) debug.dumpPipeline();
                return;
            }
            default -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Console cannot chat. Use /color debug all, or /color debug pipeline.");
                    return;
                }
                boolean on = debug.togglePlayer(player.getUniqueId());
                sender.sendMessage("ChatColor debug for you: " + (on ? "ON" : "OFF")
                        + (on ? " - say something and check the console." : ""));
                if (on) debug.dumpPipeline();
            }
        }
    }

    private void sendUsage(CommandSender sender, String usageKey) {
        String usageHint = plugin.getMessageManager().getRaw(usageKey);
        sender.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", usageHint)));
    }

    private void handleCreate(CommandSender sender, String[] args) {
        String name = args[1];
        String tag = args[2];
        String icon = args[3].toUpperCase();
        String permission = args.length > 4 ? args[4] : null;
        String permissionDefault = args.length > 5 ? args[5] : null;

        ColorManager.SaveResult result =
                plugin.getColorManager().saveCustomColor(name, tag, icon, permission, permissionDefault);

        switch (result) {
            case CREATED, UPDATED -> {
                plugin.refreshPermissions();
                plugin.getMessageManager().send(sender,
                        result == ColorManager.SaveResult.CREATED ? "color-created" : "color-updated",
                        "name", name);

                ColorEntry entry = plugin.getColorManager().getColor(ColorManager.sanitizeKey(name));
                if (entry != null) {
                    String node = entry.isPublic()
                            ? plugin.getMessageManager().getRaw("color-permission-everyone")
                            : entry.getPermission();
                    plugin.getMessageManager().send(sender, "color-permission-info", "permission", node);
                }
            }
            case INVALID_NAME -> plugin.getMessageManager().send(sender, "color-invalid-name", "name", name);
            case INVALID_TAG -> plugin.getMessageManager().send(sender, "color-invalid-tag", "tag", tag);
            case INVALID_ICON -> plugin.getMessageManager().send(sender, "color-invalid-icon", "icon", icon);
            case RESERVED_KEY -> plugin.getMessageManager().send(sender, "color-reserved-key",
                    "key", ColorManager.sanitizeKey(name));
            default -> plugin.getMessageManager().send(sender, "color-save-failed");
        }
    }

    private void handleDelete(CommandSender sender, String name) {
        String key = ColorManager.sanitizeKey(name);
        ColorManager.SaveResult result = plugin.getColorManager().deleteCustomColor(name);

        if (result == ColorManager.SaveResult.NOT_FOUND) {
            plugin.getMessageManager().send(sender, "unknown-color", "key", key);
            return;
        }
        if (!result.isSuccess()) {
            plugin.getMessageManager().send(sender, "color-save-failed");
            return;
        }

        int cleared = plugin.getPlayerDataManager().clearColor("SOLID", key);
        plugin.refreshPermissions();
        plugin.getMessageManager().send(sender, "color-deleted", java.util.Map.of(
                "name", key,
                "cleared", String.valueOf(cleared)
        ));
    }

    private void handleList(CommandSender sender, String type) {
        List<? extends SelectableEntry> entries = switch (type) {
            case "gradient", "gradients" -> plugin.getColorManager().getGradientList();
            case "pattern", "patterns" -> plugin.getPatternManager().getPatternList();
            case "custom", "custom-colors" -> new ArrayList<>(plugin.getColorManager().getCustomColors().values());
            case "color", "colors", "solid" -> plugin.getColorManager().getColorList();
            default -> null;
        };

        if (entries == null) {
            sendUsage(sender, "usage-list");
            return;
        }
        if (entries.isEmpty()) {
            plugin.getMessageManager().send(sender, "color-list-empty");
            return;
        }

        plugin.getMessageManager().send(sender, "color-list-header", java.util.Map.of(
                "type", type,
                "count", String.valueOf(entries.size())
        ));

        for (SelectableEntry entry : entries) {
            String access;
            if (entry.isPublic()) {
                access = plugin.getMessageManager().getRaw("color-list-access-everyone");
            } else if (entry.isAllowed(sender)) {
                access = plugin.getMessageManager().getRaw("color-list-access-allowed");
            } else {
                access = plugin.getMessageManager().getRaw("color-list-access-denied")
                        .replace("<permission>", entry.getPermission());
            }

            plugin.getMessageManager().send(sender, "color-list-entry", java.util.Map.of(
                    "tag", entry.getTag() == null ? "" : entry.getTag(),
                    "name", entry.getDisplayName(),
                    "key", entry.getKey(),
                    "access", access
            ));
        }
    }

    private void handleSet(CommandSender sender, Player target, String type, String key) {
        switch (type) {
            case "color", "solid" -> {
                ColorEntry entry = plugin.getConfigManager().getColor(key);
                if (entry == null) {
                    // Check if it's a hex code
                    if (key.matches("^#?[0-9a-fA-F]{6}$")) {
                        if (target == sender && !sender.hasPermission("chatcolor.set.hex")) {
                            plugin.getMessageManager().send(sender, "no-permission");
                            return;
                        }
                        plugin.getChatColorAPI().setCustomColor(target, key);
                        String hex = key.startsWith("#") ? key : "#" + key;
                        if (target == sender) {
                            plugin.getMessageManager().send(target, "hex-color-applied", "color", "<" + hex + ">" + hex + "<reset>");
                        } else {
                            plugin.getMessageManager().send(sender, "hex-color-applied-other", java.util.Map.of(
                                "color", "<" + hex + ">" + hex + "<reset>",
                                "player", target.getName()
                            ));
                        }
                        return;
                    }
                    plugin.getMessageManager().send(sender, "unknown-color", "key", key);
                    return;
                }
                if (target == sender && !entry.isAllowed(target)) {
                    plugin.getMessageManager().send(sender, "no-permission");
                    return;
                }
                plugin.getChatColorAPI().setColor(target, key);
                if (target == sender) {
                    plugin.getMessageManager().send(target, "color-applied", "color", entry.getTag() + entry.getDisplayName() + "<reset>");
                } else {
                    plugin.getMessageManager().send(sender, "color-applied-other", java.util.Map.of(
                        "color", entry.getTag() + entry.getDisplayName() + "<reset>",
                        "player", target.getName()
                    ));
                }
            }
            case "gradient" -> {
                GradientEntry entry = plugin.getConfigManager().getGradient(key);
                if (entry == null) {
                    plugin.getMessageManager().send(sender, "unknown-color", "key", key);
                    return;
                }
                if (target == sender && !entry.isAllowed(target)) {
                    plugin.getMessageManager().send(sender, "no-permission");
                    return;
                }
                plugin.getChatColorAPI().setGradient(target, key);
                if (target == sender) {
                    plugin.getMessageManager().send(target, "color-applied", "color", entry.getTag() + entry.getDisplayName() + "<reset>");
                } else {
                    plugin.getMessageManager().send(sender, "color-applied-other", java.util.Map.of(
                        "color", entry.getTag() + entry.getDisplayName() + "<reset>",
                        "player", target.getName()
                    ));
                }
            }
            case "pattern" -> {
                PatternEntry entry = plugin.getPatternManager().getPattern(key);
                if (entry == null) {
                    plugin.getMessageManager().send(sender, "unknown-color", "key", key);
                    return;
                }
                if (target == sender && !entry.isAllowed(target)) {
                    plugin.getMessageManager().send(sender, "no-permission");
                    return;
                }
                plugin.getChatColorAPI().setPattern(target, key);
                if (target == sender) {
                    plugin.getMessageManager().send(target, "color-applied", "color", entry.getDisplayName());
                } else {
                    plugin.getMessageManager().send(sender, "color-applied-other", java.util.Map.of(
                        "color", entry.getDisplayName(),
                        "player", target.getName()
                    ));
                }
            }
            default -> {
                String usageHint = plugin.getMessageManager().getRaw("usage-set");
                sender.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", usageHint)));
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "reset", "gui", "list", "reload", "create", "delete"));
            if (sender.hasPermission("chatcolor.debug")) {
                completions.add("debug");
            }
            return filter(completions, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("gui")) {
                if (sender.hasPermission("chatcolor.admin")) {
                    completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                completions.addAll(Arrays.asList("color", "gradient", "pattern"));
            } else if (args[0].equalsIgnoreCase("debug")) {
                if (sender.hasPermission("chatcolor.debug")) {
                    completions.addAll(Arrays.asList("all", "off", "pipeline"));
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                completions.addAll(Arrays.asList("colors", "gradients", "patterns", "custom"));
            } else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
                completions.addAll(plugin.getColorManager().getCustomColors().keySet());
            }
            return filter(completions, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            switch (args[1].toLowerCase()) {
                case "color", "solid" -> {
                    plugin.getColorManager().getColorList()
                            .forEach(entry -> completions.add(entry.getKey()));
                    if (sender.hasPermission("chatcolor.set.hex")) {
                        completions.add("#");
                    }
                }
                case "gradient" -> completions.addAll(plugin.getColorManager().getGradients().keySet());
                case "pattern" -> completions.addAll(plugin.getPatternManager().getPatterns().keySet());
            }
            return filter(completions, args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            if (sender.hasPermission("chatcolor.admin")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            }
            return filter(completions, args[3]);
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 3) {
                completions.addAll(Arrays.asList("<red>", "<#FF69B4>", "<gradient:red:gold>"));
                return filter(completions, args[2]);
            }
            if (args.length == 4) {
                completions.addAll(Arrays.asList("WHITE_WOOL", "RED_WOOL", "BLUE_WOOL", "GREEN_WOOL", "YELLOW_WOOL"));
                return filter(completions, args[3]);
            }
            if (args.length == 5) {
                completions.add("chatcolor.custom." + ColorManager.sanitizeKey(args[1]));
                completions.add("none");
                return filter(completions, args[4]);
            }
            if (args.length == 6) {
                completions.addAll(Arrays.asList("op", "true", "false"));
                return filter(completions, args[5]);
            }
        }

        return completions;
    }

    private List<String> filter(List<String> list, String input) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
