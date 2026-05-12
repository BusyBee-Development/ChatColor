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
        Component originalMessage = event.message();
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(originalMessage);

        lastMessages.put(player.getUniqueId(), rawMessage);

        if (plugin.getConfigManager().isLateBind()) return;

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        String messageToColor = ColorUtil.stripLegacy(rawMessage);
        boolean canUseMiniMessage = player.hasPermission("chatcolor.minimessage");

        if (!data.hasColor()) {
            String defaultColor = getDefaultColorForPlayer(player);
            if (defaultColor.equalsIgnoreCase("NONE")) {
                if (!canUseMiniMessage) {
                    event.message(Component.text(messageToColor));
                }
                return;
            }

            if (plugin.getConfigManager().isApplyToMessage()) {
                event.message(ColorUtil.applyTagToText(defaultColor, messageToColor, !canUseMiniMessage));
            }
            if (plugin.getConfigManager().isApplyToName()) {
                player.displayName(ColorUtil.applyTagToText(defaultColor, ColorUtil.stripLegacy(PlainTextComponentSerializer.plainText().serialize(player.name())), true));
            }
            return;
        }

        if (!plugin.getConfigManager().isApplyToMessage()) return;

        Component colored = buildColoredMessage(data, messageToColor, !canUseMiniMessage);
        event.message(colored);

        if (plugin.getConfigManager().isApplyToName()) {
            player.displayName(buildColoredMessage(data, ColorUtil.stripLegacy(PlainTextComponentSerializer.plainText().serialize(player.name())), true));
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

        String messageToColor = ColorUtil.stripLegacy(rawMessage);
        boolean canUseMiniMessage = player.hasPermission("chatcolor.minimessage");

        if (!data.hasColor()) {
            String defaultColor = getDefaultColorForPlayer(player);
            if (defaultColor.equalsIgnoreCase("NONE")) return;

            if (plugin.getConfigManager().isApplyToMessage()) {
                event.setMessage(ColorUtil.getLegacySerializer().serialize(ColorUtil.applyTagToText(defaultColor, messageToColor, !canUseMiniMessage)));
            }
            if (plugin.getConfigManager().isApplyToName()) {
                player.setDisplayName(ColorUtil.getLegacySerializer().serialize(ColorUtil.applyTagToText(defaultColor, ColorUtil.stripLegacy(player.getName()), true)));
            }
            return;
        }

        if (!plugin.getConfigManager().isApplyToMessage()) return;

        Component colored = buildColoredMessage(data, messageToColor, !canUseMiniMessage);
        String legacyColored = ColorUtil.getLegacySerializer().serialize(colored);
        event.setMessage(legacyColored);

        if (plugin.getConfigManager().isApplyToName()) {
            player.setDisplayName(ColorUtil.getLegacySerializer().serialize(buildColoredMessage(data, ColorUtil.stripLegacy(player.getName()), true)));
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

    private Component buildColoredMessage(PlayerColorData data, String rawText, boolean escape) {
        if (data.getColorType().equals("PATTERN")) {
            PatternEntry pattern = plugin.getPatternManager().getPattern(data.getColorKey());
            if (pattern != null) {
                return PatternApplier.apply(rawText, pattern.getColors(), escape);
            }
            return Component.text(rawText);
        }
        return ColorUtil.applyTagToText(data.getColorTag(), rawText, escape);
    }
}
