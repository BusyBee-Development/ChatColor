package net.busybee.chatcolor.commands;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.inventory.impl.MainMenuGUI;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import net.busybee.chatcolor.models.PatternEntry;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().get("player-only"));
            return true;
        }

        if (!player.hasPermission("chatcolor.use")) {
            plugin.getMessageManager().send(player, "no-permission-command");
            return true;
        }

        if (args.length == 0) {
            new MainMenuGUI(plugin).open(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reset" -> {
                plugin.getChatColorAPI().resetColor(player);
                plugin.getMessageManager().send(player, "color-reset");
            }
            case "reload" -> {
                if (!player.hasPermission("chatcolor.reload")) {
                    plugin.getMessageManager().send(player, "no-permission-command");
                    return true;
                }
                plugin.reload();
                plugin.getMessageManager().send(player, "config-reloaded");
            }
            case "set" -> {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", "/" + label + " set <type> <key>")));
                    return true;
                }
                String type = args[1].toLowerCase();
                String key = args[2].toLowerCase();
                handleSet(player, type, key);
            }
            case "gui" -> {
                new MainMenuGUI(plugin).open(player);
            }
            case "create" -> {
                if (!player.hasPermission("chatcolor.create")) {
                    plugin.getMessageManager().send(player, "no-permission-command");
                    return true;
                }
                if (args.length < 4) {
                    player.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", "/" + label + " create <name> <tag> <icon> [permission]")));
                    return true;
                }
                String name = args[1];
                String tag = args[2];
                String icon = args[3].toUpperCase();
                String permission = args.length > 4 ? args[4] : null;
                plugin.getColorManager().saveCustomColor(name, tag, icon, permission);
                plugin.getMessageManager().send(player, "color-created", "name", name);
            }
            default -> {
                new MainMenuGUI(plugin).open(player);
            }
        }

        return true;
    }

    private void handleSet(Player player, String type, String key) {
        switch (type) {
            case "color", "solid" -> {
                ColorEntry entry = plugin.getConfigManager().getColor(key);
                if (entry == null) {
                    plugin.getMessageManager().send(player, "unknown-color", "key", key);
                    return;
                }
                if (!player.hasPermission(entry.getPermission())) {
                    plugin.getMessageManager().send(player, "no-permission");
                    return;
                }
                plugin.getChatColorAPI().setColor(player, key);
                plugin.getMessageManager().send(player, "color-applied", "color", entry.getTag() + entry.getDisplayName() + "<reset>");
            }
            case "gradient" -> {
                GradientEntry entry = plugin.getConfigManager().getGradient(key);
                if (entry == null) {
                    plugin.getMessageManager().send(player, "unknown-color", "key", key);
                    return;
                }
                if (!player.hasPermission(entry.getPermission())) {
                    plugin.getMessageManager().send(player, "no-permission");
                    return;
                }
                plugin.getChatColorAPI().setGradient(player, key);
                plugin.getMessageManager().send(player, "color-applied", "color", entry.getTag() + entry.getDisplayName() + "<reset>");
            }
            case "pattern" -> {
                PatternEntry entry = plugin.getPatternManager().getPattern(key);
                if (entry == null) {
                    plugin.getMessageManager().send(player, "unknown-color", "key", key);
                    return;
                }
                if (!player.hasPermission(entry.getPermission())) {
                    plugin.getMessageManager().send(player, "no-permission");
                    return;
                }
                plugin.getChatColorAPI().setPattern(player, key);
                plugin.getMessageManager().send(player, "color-applied", "color", entry.getDisplayName());
            }
            default -> player.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", "/color set <color|gradient|pattern> <key>")));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "reset", "gui", "reload", "create"));
            return filter(completions, args[0]);
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

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("color", "gradient", "pattern"));
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

        return completions;
    }

    private List<String> filter(List<String> list, String input) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
