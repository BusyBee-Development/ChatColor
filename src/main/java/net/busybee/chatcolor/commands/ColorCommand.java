package net.busybee.chatcolor.commands;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.inventory.impl.MainMenuGUI;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import net.busybee.chatcolor.models.PatternEntry;
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
                    String usageHint = plugin.getMessageManager().getRaw("usage-create");
                    sender.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", usageHint)));
                    return true;
                }
                String name = args[1];
                String tag = args[2];
                String icon = args[3].toUpperCase();
                String permission = args.length > 4 ? args[4] : null;
                plugin.getColorManager().saveCustomColor(name, tag, icon, permission);
                plugin.getMessageManager().send(sender, "color-created", "name", name);
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

    private void handleSet(CommandSender sender, Player target, String type, String key) {
        switch (type) {
            case "color", "solid" -> {
                ColorEntry entry = plugin.getConfigManager().getColor(key);
                if (entry == null) {
                    plugin.getMessageManager().send(sender, "unknown-color", "key", key);
                    return;
                }
                if (target == sender && !target.hasPermission(entry.getPermission())) {
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
                if (target == sender && !target.hasPermission(entry.getPermission())) {
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
                if (target == sender && !target.hasPermission(entry.getPermission())) {
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
            completions.addAll(Arrays.asList("set", "reset", "gui", "reload", "create"));
            return filter(completions, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("gui")) {
                if (sender.hasPermission("chatcolor.admin")) {
                    completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                completions.addAll(Arrays.asList("color", "gradient", "pattern"));
            }
            return filter(completions, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            switch (args[1].toLowerCase()) {
                case "color", "solid" -> completions.addAll(plugin.getConfigManager().getColors().keySet());
                case "gradient" -> completions.addAll(plugin.getConfigManager().getGradients().keySet());
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

        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            completions.add("RED_WOOL");
            completions.add("BLUE_WOOL");
            completions.add("GREEN_WOOL");
            completions.add("YELLOW_WOOL");
            return filter(completions, args[3]);
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("create")) {
            completions.add("chatcolor.custom." + args[1].toLowerCase().replace(" ", "_"));
            return filter(completions, args[4]);
        }

        return completions;
    }

    private List<String> filter(List<String> list, String input) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
