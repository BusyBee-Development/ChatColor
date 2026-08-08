package net.busybee.chatcolor.listeners;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.utils.ChatDebugger;
import net.busybee.chatcolor.utils.ColorUtil;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
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

        ChatDebugger debug = plugin.getChatDebugger();
        boolean watched = debug != null && debug.isWatched(player);
        if (watched) {
            debug.chatIn(player, plain, "MODERN");
        }

        if (plugin.getConfigManager().isLateBind()) {
            if (watched) debug.log("  late-bind is on, ChatColor stops here");
            return;
        }

        if (player.hasPermission("chatcolor.minimessage")) {
            event.message(ColorUtil.colorizeUserInput(plain));
        }

        if (!plugin.getConfigManager().isApplyToMessage()) {
            if (watched) debug.log("  apply-to-message is off, ChatColor stops here");
            return;
        }

        if (watched) {
            debug.beforeWrap(event.renderer());
        }
        event.renderer(new ColorRenderer(event.renderer(), watched ? debug : null));
    }

    /** True when {@code renderer} is the one ChatColor installed and nothing has replaced it. */
    public static boolean isOurRenderer(ChatRenderer renderer) {
        return renderer instanceof ColorRenderer;
    }

    private final class ColorRenderer implements ChatRenderer {

        private final ChatRenderer delegate;
        private final ChatDebugger debug;
        private Component cachedInput;
        private Component cachedOutput;

        private ColorRenderer(ChatRenderer delegate, ChatDebugger debug) {
            this.delegate = delegate;
            this.debug = debug;
        }

        @Override
        public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
            return delegate.render(source, sourceDisplayName, colorize(source, message), viewer);
        }

        private synchronized Component colorize(Player source, Component message) {
            if (cachedInput != message) {
                cachedInput = message;
                cachedOutput = plugin.getChatColorAPI().applyColorToComponent(source, message);
                if (debug != null) {
                    debug.rendered(message, cachedOutput);
                }
            }
            return cachedOutput;
        }
    }

    public void onLegacyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String rawMessage = event.getMessage();
        lastMessages.put(player.getUniqueId(), rawMessage);

        ChatDebugger debug = plugin.getChatDebugger();
        boolean watched = debug != null && debug.isWatched(player);
        if (watched) {
            debug.chatIn(player, rawMessage, "LEGACY");
        }

        rewriteFormatPlaceholders(player, event);

        if (plugin.getConfigManager().isLateBind()) {
            if (watched) debug.log("  late-bind is on, ChatColor stops here");
            return;
        }
        Component message = prepareInput(player, rawMessage);
        if (plugin.getConfigManager().isApplyToMessage()) {
            message = plugin.getChatColorAPI().applyColorToComponent(player, message);
        }
        String serialized = ColorUtil.getLegacySerializer().serialize(message);
        if (watched) {
            debug.wroteLegacyMessage(serialized);
        }
        event.setMessage(serialized);
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
