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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final ChatColor plugin;
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
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

        if (!data.hasColor()) return;
        if (!plugin.getConfigManager().isApplyToMessage()) return;

        Component colored = buildColoredMessage(data, rawMessage);
        event.message(colored);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        if (plugin.isPaper()) return;

        Player player = event.getPlayer();
        String rawMessage = event.getMessage();

        lastMessages.put(player.getUniqueId(), rawMessage);

        if (plugin.getConfigManager().isLateBind()) return;

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        if (!data.hasColor()) return;
        if (!plugin.getConfigManager().isApplyToMessage()) return;

        Component colored = buildColoredMessage(data, rawMessage);
        String legacyColored = LEGACY.serialize(colored);
        event.setMessage(legacyColored);
    }

    public static String getLastMessage(UUID uuid) {
        return lastMessages.getOrDefault(uuid, "");
    }

    private Component buildColoredMessage(PlayerColorData data, String rawText) {
        if (data.getColorType().equals("PATTERN")) {
            PatternEntry pattern = plugin.getPatternManager().getPattern(data.getColorKey());
            if (pattern != null) {
                return PatternApplier.apply(rawText, pattern.getColors());
            }
            return Component.text(rawText);
        }
        return ColorUtil.applyTagToText(data.getColorTag(), rawText);
    }
}
