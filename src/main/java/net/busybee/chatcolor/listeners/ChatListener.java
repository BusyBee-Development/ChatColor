package net.busybee.chatcolor.listeners;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.data.PlayerColorData;
import net.busybee.chatcolor.models.PatternEntry;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.PatternApplier;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final ChatColor plugin;
    private static final Map<UUID, String> lastMessages = new ConcurrentHashMap<>();

    public ChatListener(ChatColor plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component currentComponent = event.message();
        
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(currentComponent);
        lastMessages.put(player.getUniqueId(), plainMessage);

        if (plugin.getConfigManager().isLateBind()) return;

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        boolean canUseMiniMessage = player.hasPermission("chatcolor.minimessage");

        if (!canUseMiniMessage) {
            currentComponent = ColorUtil.escapeTags(currentComponent);
        }

        if (!data.hasColor()) {
            String defaultColor = getDefaultColorForPlayer(player);
            if (defaultColor.equalsIgnoreCase("NONE")) {
                event.message(currentComponent);
                return;
            }

            if (plugin.getConfigManager().isApplyToMessage()) {
                event.message(ColorUtil.applyTagToComponent(defaultColor, currentComponent));
            }
            if (plugin.getConfigManager().isApplyToName()) {
                player.displayName(ColorUtil.applyTagToComponent(defaultColor, ColorUtil.escapeTags(player.name())));
            }
            return;
        }

        if (!plugin.getConfigManager().isApplyToMessage()) return;

        Component colored = buildColoredMessage(data, currentComponent);
        event.message(colored);

        if (plugin.getConfigManager().isApplyToName()) {
            player.displayName(buildColoredMessage(data, ColorUtil.escapeTags(player.name())));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        if (plugin.isPaper()) return;

        Player player = event.getPlayer();
        String rawMessage = event.getMessage();

        lastMessages.put(player.getUniqueId(), rawMessage);

        if (plugin.getConfigManager().isLateBind()) return;

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        boolean canUseMiniMessage = player.hasPermission("chatcolor.minimessage");

        Component component = ColorUtil.colorize(rawMessage);
        if (!canUseMiniMessage) {
            component = ColorUtil.escapeTags(component);
        }

        if (!data.hasColor()) {
            String defaultColor = getDefaultColorForPlayer(player);
            if (defaultColor.equalsIgnoreCase("NONE")) return;

            if (plugin.getConfigManager().isApplyToMessage()) {
                Component colored = ColorUtil.applyTagToComponent(defaultColor, component);
                event.setMessage(ColorUtil.getLegacySerializer().serialize(colored));
            }
            if (plugin.getConfigManager().isApplyToName()) {
                Component nameComponent = ColorUtil.escapeTags(Component.text(player.getName()));
                player.setDisplayName(ColorUtil.getLegacySerializer().serialize(ColorUtil.applyTagToComponent(defaultColor, nameComponent)));
            }
            return;
        }

        if (!plugin.getConfigManager().isApplyToMessage()) return;

        Component colored = buildColoredMessage(data, component);
        event.setMessage(ColorUtil.getLegacySerializer().serialize(colored));

        if (plugin.getConfigManager().isApplyToName()) {
            Component nameComponent = ColorUtil.escapeTags(Component.text(player.getName()));
            player.setDisplayName(ColorUtil.getLegacySerializer().serialize(buildColoredMessage(data, nameComponent)));
        }
    }

    private String getDefaultColorForPlayer(Player player) {
        for (Map.Entry<String, String> entry : plugin.getConfigManager().getGroupDefaults().entrySet()) {
            if (player.hasPermission("chatcolor.group." + entry.getKey())) {
                return entry.getValue();
            }
        }
        return plugin.getConfigManager().getDefaultColor();
    }

    public static String getLastMessage(UUID uuid) {
        return lastMessages.getOrDefault(uuid, "");
    }

    private Component buildColoredMessage(PlayerColorData data, Component component) {
        if (data.getColorType().equals("PATTERN")) {
            PatternEntry pattern = plugin.getPatternManager().getPattern(data.getColorKey());
            if (pattern != null) {
                return PatternApplier.apply(component, pattern.getColors());
            }
            return component;
        }
        return ColorUtil.applyTagToComponent(data.getColorTag(), component);
    }
}
