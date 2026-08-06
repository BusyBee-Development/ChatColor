package net.busybee.chatcolor.listeners;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.utils.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final ChatColor plugin;
    private static final Map<UUID, String> lastMessages = new ConcurrentHashMap<>();
    public ChatListener(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
        lastMessages.put(player.getUniqueId(), plain);

        if (plugin.getConfigManager().isLateBind()) {
            return;
        }

        Component message = prepareInput(player, plain);

        if (plugin.getConfigManager().isApplyToMessage()) {
            message = plugin.getChatColorAPI().applyColorToComponent(player, message);
        }
        event.message(message);
    }

    public void onLegacyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String rawMessage = event.getMessage();
        lastMessages.put(player.getUniqueId(), rawMessage);

        rewriteFormatPlaceholders(player, event);

        if (plugin.getConfigManager().isLateBind()) {
            return;
        }
        Component message = prepareInput(player, rawMessage);
        if (plugin.getConfigManager().isApplyToMessage()) {
            message = plugin.getChatColorAPI().applyColorToComponent(player, message);
        }
        event.setMessage(ColorUtil.getLegacySerializer().serialize(message));
    }

    private Component prepareInput(Player player, String raw) {
        if (player.hasPermission("chatcolor.minimessage")) {
            return ColorUtil.colorizeUserInput(raw);
        }
        return Component.text(raw);
    }

    private void rewriteFormatPlaceholders(Player player, AsyncPlayerChatEvent event) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return;
        }

        String format = event.getFormat();
        boolean changed = false;

        if (format.contains("%chatcolor_message%")) {
            format = format.replace("%chatcolor_message%", "%2$s");
            changed = true;
        }
        if (format.contains("%")) {
            String papped = PlaceholderAPI.setPlaceholders(player, format);
            if (!papped.equals(format)) {
                format = papped;
                changed = true;
            }
        }
        if (changed) {
            event.setFormat(format);
        }
    }

    public static String getLastMessage(UUID uuid) {
        return lastMessages.getOrDefault(uuid, "");
    }
    public static void forget(UUID uuid) {
        lastMessages.remove(uuid);
    }
}
