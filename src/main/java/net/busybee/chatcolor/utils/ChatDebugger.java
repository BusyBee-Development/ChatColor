package net.busybee.chatcolor.utils;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.data.PlayerColorData;
import net.busybee.chatcolor.listeners.ChatListener;
import net.busybee.chatcolor.models.PatternEntry;
import net.busybee.chatcolor.models.SelectableEntry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredListener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat pipeline diagnostics behind {@code /color debug}, for "my colours are not showing up".
 *
 * <p>Dormant until someone runs the command; while it is off, every entry point here costs one
 * volatile read. It exists to answer four questions in one chat message:
 *
 * <ol>
 *   <li>Does ChatColor even resolve a colour for this player, or does its own permission check
 *       fail? ({@link #chatIn})</li>
 *   <li>Which plugins are on the chat event, in what order, and is ChatColor really behind the
 *       formatter it thinks it is behind? ({@link #dumpPipeline})</li>
 *   <li>Does our renderer survive to the end of the event, or does something replace it?
 *       ({@link #monitor})</li>
 *   <li>Does the renderer ever actually run, or is a plugin bypassing rendering and delivering
 *       the message itself? ({@link #rendered})</li>
 * </ol>
 *
 * <p>Output is escaped by {@link #safe(String)} so that the {@code clean-console} filter, which
 * strips both legacy codes and {@code <tags>}, cannot eat the very thing being inspected.
 */
public final class ChatDebugger implements Listener {

    private static final String[] ESSENTIALS_NODES = {
            "essentials.chat.color",
            "essentials.chat.format",
            "essentials.chat.magic",
            "essentials.chat.rgb",
    };

    private final ChatColor plugin;
    private final Set<UUID> watched = ConcurrentHashMap.newKeySet();
    private volatile boolean watchAll;

    public ChatDebugger(ChatColor plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return watchAll || !watched.isEmpty();
    }

    public boolean isWatched(Player player) {
        if (watchAll) return true;
        return player != null && watched.contains(player.getUniqueId());
    }

    public boolean toggleAll() {
        watchAll = !watchAll;
        rebind();
        return watchAll;
    }

    public boolean togglePlayer(UUID uuid) {
        boolean enabled = !watched.remove(uuid);
        if (enabled) {
            watched.add(uuid);
        }
        rebind();
        return enabled;
    }

    public void stop() {
        watchAll = false;
        watched.clear();
        rebind();
    }

    // ------------------------------------------------------------------ pipeline

    public void rebind() {
        unregister();
        if (!isActive()) {
            return;
        }

        if (plugin.isLegacyHook()) {
            Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, this, EventPriority.MONITOR,
                    (listener, event) -> {
                        if (event instanceof AsyncPlayerChatEvent chat) monitorLegacy(chat);
                    }, plugin, true);
        } else {
            Bukkit.getPluginManager().registerEvent(AsyncChatEvent.class, this, EventPriority.MONITOR,
                    (listener, event) -> {
                        if (event instanceof AsyncChatEvent chat) monitor(chat);
                    }, plugin, true);
        }
    }

    private void unregister() {
        if (plugin.isPaper()) {
            AsyncChatEvent.getHandlerList().unregister(this);
        }
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

    public void dumpPipeline() {
        log("===== chat pipeline =====");
        log("paper=" + plugin.isPaper()
                + " hook=" + (plugin.isLegacyHook() ? "LEGACY (AsyncPlayerChatEvent)" : "MODERN (AsyncChatEvent)")
                + " priority=" + plugin.getActivePriority().name()
                + " mode=" + colourMode());
        log("config: apply-to-message=" + plugin.getConfigManager().isApplyToMessage()
                + " apply-to-name=" + plugin.getConfigManager().isApplyToName()
                + " late-bind=" + plugin.getConfigManager().isLateBind()
                + " chat-hook=" + plugin.getConfigManager().getChatHook()
                + " event-priority=" + plugin.getConfigManager().getEventPriority()
                + " message-mode=" + plugin.getConfigManager().getMessageMode());

        if (plugin.isPaper()) {
            dumpHandlers("AsyncChatEvent", AsyncChatEvent.getHandlerList());
        }
        dumpHandlers("AsyncPlayerChatEvent", AsyncPlayerChatEvent.getHandlerList());
        log("=========================");
    }

    private void dumpHandlers(String label, HandlerList handlers) {
        RegisteredListener[] listeners = handlers.getRegisteredListeners();
        if (listeners.length == 0) {
            log(label + ": nothing listening");
            return;
        }
        log(label + ": " + listeners.length + " listener(s), in execution order");
        for (RegisteredListener registered : listeners) {
            log("  " + pad(registered.getPriority().name()) + " " + registered.getPlugin().getName()
                    + "  (" + registered.getListener().getClass().getName() + ")");
        }
    }

    // ------------------------------------------------------------------ per message

    public void chatIn(Player player, String raw, String mode) {
        log("--- " + player.getName() + " [" + mode + "] \"" + safe(raw) + "\"");

        PlayerColorData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        log("  stored:   type=" + data.getColorType() + " key=" + data.getColorKey()
                + " tag=" + safe(data.getColorTag()));

        PatternEntry pattern = plugin.getChatColorAPI().resolveActivePattern(player);
        String tag = plugin.getChatColorAPI().resolveActiveTag(player);
        log("  resolved: pattern=" + (pattern == null ? "none" : pattern.getKey())
                + " tag=" + (tag == null ? "NONE <- nothing will be coloured" : safe(tag)));

        logEntryPermission(player, data);
        for (String node : ESSENTIALS_NODES) {
            log("  " + node + " = " + player.hasPermission(node)
                    + (player.isPermissionSet(node) ? "" : " (unset, using default)"));
        }
    }

    private void logEntryPermission(Player player, PlayerColorData data) {
        String key = data.getColorKey();
        if (key == null || !data.hasColor()) {
            log("  entry:    no colour selected, group/default colour applies");
            return;
        }

        SelectableEntry entry = switch (String.valueOf(data.getColorType())) {
            case "PATTERN" -> plugin.getPatternManager().getPattern(key);
            case "GRADIENT" -> plugin.getColorManager().getGradient(key);
            default -> plugin.getColorManager().getColor(key);
        };

        if (entry == null) {
            log("  entry:    \"" + key + "\" is no longer defined, falling back to the stored tag");
            return;
        }
        if (entry.isPublic()) {
            log("  entry:    \"" + key + "\" needs no permission");
            return;
        }

        String node = entry.getPermission();
        log("  entry:    \"" + key + "\" needs " + node
                + " -> hasPermission=" + player.hasPermission(node)
                + " isPermissionSet=" + player.isPermissionSet(node));
    }

    public void beforeWrap(ChatRenderer current) {
        log("  renderer in place before us: " + current.getClass().getName());
    }

    /** Called on the modern hook in direct mode, where we colour the message instead of rendering. */
    public void wroteMessage(Component in, Component out) {
        log("  WROTE message (direct mode, we do not render)");
        log("    in : " + safe(ColorUtil.toMiniMessage(in)));
        log("    out: " + safe(ColorUtil.toMiniMessage(out)));
    }

    /** Called from inside the renderer, once per message rather than once per viewer. */
    public void rendered(Component in, Component out) {
        log("  RENDERER RAN");
        log("    in : " + safe(ColorUtil.toMiniMessage(in)));
        log("    out: " + safe(ColorUtil.toMiniMessage(out)));
    }

    public void wroteLegacyMessage(String serialized) {
        log("  WROTE message: " + safe(serialized));
    }

    private void monitor(AsyncChatEvent event) {
        if (!isWatched(event.getPlayer())) return;

        ChatRenderer renderer = event.renderer();
        boolean direct = plugin.isDirectWrite();

        log("  MONITOR:  cancelled=" + event.isCancelled()
                + " viewers=" + event.viewers().size()
                + (direct ? "" : " ourRendererStillInstalled=" + ChatListener.isOurRenderer(renderer)));
        log("    renderer: " + renderer.getClass().getName());
        log("    message : " + safe(ColorUtil.toMiniMessage(event.message())));
        if (direct) {
            log("    (the colour must be visible in 'message' above; if it is not, something after");
            log("     us overwrote the message)");
        } else {
            log("    (if no RENDERER RAN line follows, something delivered the message itself)");
        }
    }

    private void monitorLegacy(AsyncPlayerChatEvent event) {
        if (!isWatched(event.getPlayer())) return;

        log("  MONITOR:  cancelled=" + event.isCancelled()
                + " recipients=" + event.getRecipients().size());
        log("    format  : " + safe(event.getFormat()));
        log("    message : " + safe(event.getMessage()));
    }

    // ------------------------------------------------------------------ output

    public void log(String message) {
        plugin.getLogger().info("[DEBUG] " + message);
    }

    private static String safe(String text) {
        if (text == null) return "null";
        return text.replace("§", "(S)")
                .replace('<', '{')
                .replace('>', '}');
    }

    private String colourMode() {
        if (plugin.isLegacyHook()) return "LEGACY (event.setMessage)";
        return plugin.isDirectWrite() ? "DIRECT (event.message)" : "RENDERER";
    }

    private static String pad(String priority) {
        return (priority + "        ").substring(0, 8);
    }
}
