package net.busybee.chatcolor.listeners;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.api.ChatColorAPI;
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
    private static final Map<UUID, MessageSeed> messageSeeds = new ConcurrentHashMap<>();
    /**
     * One chat message reaches us up to three times (legacy event, modern event, ZelChat module),
     * so a repeat of the same text this soon is the same message and keeps its seed.
     */
    private static final long SAME_MESSAGE_MS = 2000;
    private static final AtomicBoolean warnedPapiFailure = new AtomicBoolean(false);
    public ChatListener(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
        long seed = record(player.getUniqueId(), plain);

        ChatDebugger debug = plugin.getChatDebugger();
        boolean watched = debug != null && debug.isWatched(player);
        if (watched) {
            debug.chatIn(player, plain, "MODERN");
        }

        if (plugin.getConfigManager().isLateBind()) {
            if (watched) debug.log("  late-bind is on, ChatColor stops here");
            return;
        }

        // ZelChat rebuilds the message from plain text and renders it itself, so anything we do
        // to the event is lost. ChatColor colours it from inside ZelChat instead.
        if (plugin.getZelChatHook() != null) {
            if (watched) debug.log("  ZelChat formats this message, ChatColor colours it inside ZelChat (see ZELCHAT line)");
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

        // Some formatters build their output from a copy of the message they took earlier and
        // never look at what the renderer chain hands them, so wrapping their renderer would
        // colour nothing. Colour the event message instead and let them format the coloured
        // copy. See ChatColor#isDirectWrite.
        if (plugin.isDirectWrite()) {
            Component colored = plugin.getChatColorAPI().applyColorToComponent(player, event.message(), seed);
            if (watched) {
                debug.wroteMessage(event.message(), colored);
            }
            event.message(colored);
            return;
        }

        event.renderer(new ColorRenderer(event.renderer(), seed, watched ? debug : null));
    }

    /** True when {@code renderer} is the one ChatColor installed and nothing has replaced it. */
    public static boolean isOurRenderer(ChatRenderer renderer) {
        return renderer instanceof ColorRenderer;
    }

    private final class ColorRenderer implements ChatRenderer {

        private final ChatRenderer delegate;
        private final long seed;
        private final ChatDebugger debug;
        private Component cachedInput;
        private Component cachedOutput;

        private ColorRenderer(ChatRenderer delegate, long seed, ChatDebugger debug) {
            this.delegate = delegate;
            this.seed = seed;
            this.debug = debug;
        }

        @Override
        public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
            return delegate.render(source, sourceDisplayName, colorize(source, message), viewer);
        }

        private synchronized Component colorize(Player source, Component message) {
            if (cachedInput != message) {
                cachedInput = message;
                cachedOutput = plugin.getChatColorAPI().applyColorToComponent(source, message, seed);
                if (debug != null) {
                    debug.rendered(message, cachedOutput);
                }
            }
            return cachedOutput;
        }
    }

    /**
     * Paper fires the legacy chat event before the modern one, and formatters on the legacy event
     * (ChatManager, DeluxeChat) resolve %chatcolor_message% there. Without this the placeholder
     * would still hold the previous message when they read it.
     */
    public void recordLegacy(AsyncPlayerChatEvent event) {
        record(event.getPlayer().getUniqueId(), event.getMessage());
    }

    public void onLegacyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String rawMessage = event.getMessage();
        long seed = record(player.getUniqueId(), rawMessage);

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
            message = plugin.getChatColorAPI().applyColorToComponent(player, message, seed);
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
        return Component.text(ColorUtil.stripSection(raw));
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
            try {
                String papped = PlaceholderAPI.setPlaceholders(player, format);
                if (!papped.equals(format)) {
                    format = papped;
                    changed = true;
                }
            } catch (Exception e) {
                if (warnedPapiFailure.compareAndSet(false, true)) {
                    plugin.getLogger().warning("PlaceholderAPI.setPlaceholders() threw " + e.getClass().getSimpleName()
                            + " - skipping placeholder replacement in chat format. This usually means two copies of"
                            + " PlaceholderAPI are loaded (check your plugins folder for a duplicate jar, e.g."
                            + " \"PlaceholderAPI-x.y.z (1).jar\"). Further occurrences will be suppressed.");
                }
            }
        }
        if (changed) {
            event.setFormat(format);
        }
    }

    /**
     * Remembers {@code text} as the player's latest message and returns the seed for its random
     * pattern colors. Called again for the same message from another event, it returns the same
     * seed, so every path colors that message identically.
     */
    public static long record(UUID uuid, String text) {
        lastMessages.put(uuid, text);
        long now = System.currentTimeMillis();
        MessageSeed seed = messageSeeds.compute(uuid, (id, old) ->
                old != null && old.text.equals(text) && now - old.at < SAME_MESSAGE_MS
                        ? old
                        : new MessageSeed(text, ThreadLocalRandom.current().nextLong(), now));
        return seed.seed;
    }

    /** The seed of the player's latest message, or their fixed seed when they haven't chatted. */
    public static long getMessageSeed(UUID uuid) {
        MessageSeed seed = messageSeeds.get(uuid);
        return seed != null ? seed.seed : ChatColorAPI.fixedSeed(uuid);
    }

    private record MessageSeed(String text, long seed, long at) {
    }

    public static String getLastMessage(UUID uuid) {
        return lastMessages.getOrDefault(uuid, "");
    }
    public static void forget(UUID uuid) {
        lastMessages.remove(uuid);
        messageSeeds.remove(uuid);
    }
}
