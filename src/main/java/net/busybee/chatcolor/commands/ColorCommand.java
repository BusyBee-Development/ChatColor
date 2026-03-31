package net.busybee.chatcolor.commands;

import net.busybee.chatcolor.LuminaColor;
import net.busybee.chatcolor.inventory.impl.MainMenuGUI;
import net.busybee.chatcolor.models.ColorEntry;
import net.busybee.chatcolor.models.GradientEntry;
import net.busybee.chatcolor.models.PatternEntry;
import net.kyori.adventure.text.Component;
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

    private final LuminaColor plugin;

    public ColorCommand(LuminaColor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().get("player-only"));
            return true;
        }

        if (!player.hasPermission("luminacolor.use")) {
            plugin.getMessageManager().send(player, "no-permission-command");
            return true;
        }

        if (args.length == 0) {
            MainMenuGUI gui = new MainMenuGUI(plugin);
            plugin.getGuiManager().openGUI(gui, player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reset" -> {
                plugin.getLuminaColorAPI().resetColor(player);
                plugin.getMessageManager().send(player, "color-reset");
            }
            case "reload" -> {
                if (!player.hasPermission("luminacolor.reload")) {
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
                MainMenuGUI gui = new MainMenuGUI(plugin);
                plugin.getGuiManager().openGUI(gui, player);
            }
            default -> {
                MainMenuGUI gui = new MainMenuGUI(plugin);
                plugin.getGuiManager().openGUI(gui, player);
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
                plugin.getLuminaColorAPI().setColor(player, key);
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
                plugin.getLuminaColorAPI().setGradient(player, key);
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
                plugin.getLuminaColorAPI().setPattern(player, key);
                plugin.getMessageManager().send(player, "color-applied", "color", entry.getDisplayName());
            }
            default -> player.sendMessage(plugin.getMessageManager().get("invalid-usage", java.util.Map.of("usage", "/color set <color|gradient|pattern> <key>")));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "reset", "gui", "reload"));
            return filter(completions, args[0]);
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