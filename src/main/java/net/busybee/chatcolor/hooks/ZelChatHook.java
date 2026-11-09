package net.busybee.chatcolor.hooks;

import it.pino.zelchat.api.ZelChatAPI;
import it.pino.zelchat.api.message.ChatMessage;
import it.pino.zelchat.api.message.mention.ChatMention;
import it.pino.zelchat.api.message.state.MessageState;
import it.pino.zelchat.api.module.ChatModule;
import it.pino.zelchat.api.module.annotation.ChatModuleSettings;
import it.pino.zelchat.api.module.priority.ModulePriority;
import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.utils.ChatDebugger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Colours chat from inside ZelChat.
 *
 * <p>ZelChat rebuilds every message from its plain text, so whatever ChatColor writes to the chat
 * event is thrown away, and it then installs a renderer that ignores the message it is handed.
 * The only place left to colour is ZelChat's own module chain. Our module runs after ZelChat's
 * built-in ones and before ZelChat swaps {@code [item]}, {@code [inv]}, {@code [ec]} and mentioned
 * names for their interactive components, so {@code {message}} in a ZelChat format arrives
 * coloured and those features keep working. We keep those tokens whole while colouring, because a
 * gradient or pattern that split them into separate letters would hide them from ZelChat.
 *
 * <p>Only touch this class (even a static method) when ZelChat is enabled: loading it links
 * against ZelChat's API, and ChatColor fails to enable if that API is missing.
 */
public final class ZelChatHook {

    /** ZelChat's shipped defaults, used when its settings.yml can't be read. */
    private static final List<String> DEFAULT_RENDER_PREFIXES = List.of(
            "[item]", "[i]", "[hand]", "[inv]", "[inventory]", "[enderchest]", "[ec]", "[ender]");

    private static final String[] RENDER_PREFIX_PATHS = {
            "Features.item-show.prefix",
            "Features.inventory-show.prefix",
            "Features.enderchest-show.prefix",
    };

    private final ChatColor plugin;
    private final Module module = new Module();
    private volatile List<String> renderPrefixes = DEFAULT_RENDER_PREFIXES;

    public ZelChatHook(ChatColor plugin) {
        this.plugin = plugin;
    }

    public boolean register() {
        try {
            loadRenderPrefixes();
            ZelChatAPI.get().getModuleManager().register(plugin, module);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not hook into ZelChat (" + t.getClass().getSimpleName() + ": "
                    + t.getMessage() + "). Chat colours will not apply to ZelChat's {message}.");
            return false;
        }
    }

    public void unregister() {
        try {
            ZelChatAPI.get().getModuleManager().unregister(plugin, module);
        } catch (Throwable ignored) {
            // ZelChat already went down and dropped its modules.
        }
    }

    /** Re-reads ZelChat's [item]/[inv]/[ec] prefixes, so a /color reload picks up edits there. */
    public void loadRenderPrefixes() {
        Plugin zelChat = Bukkit.getPluginManager().getPlugin("ZelChat");
        File settings = zelChat == null ? null : findSettings(zelChat.getDataFolder());
        if (settings == null) {
            renderPrefixes = DEFAULT_RENDER_PREFIXES;
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(settings);
        Set<String> prefixes = new LinkedHashSet<>();
        for (String path : RENDER_PREFIX_PATHS) {
            prefixes.addAll(config.getStringList(path));
        }
        renderPrefixes = prefixes.isEmpty() ? DEFAULT_RENDER_PREFIXES : List.copyOf(prefixes);
    }

    private static File findSettings(File dataFolder) {
        for (File candidate : new File[] {
                new File(dataFolder, "settings.yml"),
                new File(dataFolder, "configs/settings.yml")}) {
            if (candidate.isFile()) return candidate;
        }
        return null;
    }

    private void colour(ChatMessage message) {
        MessageState state = message.getState();
        if (state != MessageState.PROCESSING && state != MessageState.READY) {
            return;
        }
        if (!plugin.getConfigManager().isApplyToMessage() || plugin.getConfigManager().isLateBind()) {
            return;
        }

        Player player = message.getBukkitPlayer();
        List<String> keep = new ArrayList<>(renderPrefixes);
        for (ChatMention mention : message.getMentions()) {
            keep.add(mention.getName());
        }

        Component in = message.getMessage();
        Component out = plugin.getChatColorAPI().applyColorToComponent(player, in, keep);
        message.setMessage(out);

        ChatDebugger debug = plugin.getChatDebugger();
        if (debug != null && debug.isWatched(player)) {
            debug.zelChatColoured(in, out, keep);
        }
    }

    // pluginOwner must match plugin.yml's name: ZelChat drops the module when that plugin disables.
    @ChatModuleSettings(pluginOwner = "ChatColor", priority = ModulePriority.HIGHEST)
    private final class Module implements ChatModule {
        @Override
        public void handleChatMessage(ChatMessage message) {
            colour(message);
        }
    }
}
