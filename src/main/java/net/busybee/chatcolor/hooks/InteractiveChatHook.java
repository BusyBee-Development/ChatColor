package net.busybee.chatcolor.hooks;

import net.busybee.chatcolor.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Tells the colouring which parts of a message InteractiveChat still has to find.
 *
 * <p>InteractiveChat looks for its keywords ({@code [item]}, custom ones like {@code @ping}) and
 * mentions ({@code @name}) in the chat event, swaps them for a hidden tag such as
 * {@code <chat=UUID:[item]:>}, and swaps that tag for the item or hover text as the message goes
 * out. A gradient or pattern colours one letter at a time, which splits both the keyword and the
 * tag into separate pieces, so InteractiveChat never finds them. We keep all of them whole.
 *
 * <p>InteractiveChat's API is read by reflection, so ChatColor needs nothing from it to build or
 * to run without it.
 */
public final class InteractiveChatHook {

    /** InteractiveChat's hidden sender tag (its Registry.ID_PATTERN, without the groups). */
    private static final Pattern SENDER_TAG = Pattern.compile(
            "<(?:cmd|chat)=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(?::.*?:)?>");

    private final ChatColor plugin;
    private final AtomicBoolean warned = new AtomicBoolean(false);
    private Method placeholderList;
    private Method rawKeyword;
    private volatile String mentionPrefix = "@";
    private volatile boolean mentions = true;
    private volatile boolean mentionHere = true;
    private volatile boolean mentionEveryone = true;

    public InteractiveChatHook(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void register() {
        try {
            ClassLoader loader = Bukkit.getPluginManager().getPlugin("InteractiveChat").getClass().getClassLoader();
            Class<?> api = Class.forName("com.loohp.interactivechat.api.InteractiveChatAPI", true, loader);
            Class<?> placeholder = Class.forName("com.loohp.interactivechat.objectholders.ICPlaceholder", true, loader);
            this.placeholderList = api.getMethod("getICPlaceholderList");
            this.rawKeyword = placeholder.getMethod("getRawKeyword");
        } catch (Throwable t) {
            // Still keep the tags and mentions whole, which covers most of it.
            plugin.getLogger().warning("Could not read InteractiveChat's keywords (" + t.getClass().getSimpleName()
                    + "). Mentions keep working with gradients, custom keywords may not.");
        }
        loadSettings();
    }

    /** Re-reads InteractiveChat's mention settings, so a /color reload picks up edits there. */
    public void loadSettings() {
        Plugin ic = Bukkit.getPluginManager().getPlugin("InteractiveChat");
        File file = ic == null ? null : new File(ic.getDataFolder(), "config.yml");
        if (file == null || !file.isFile()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        mentionPrefix = config.getString("Chat.MentionPrefix", "@");
        mentions = config.getBoolean("Chat.AllowMention", true);
        mentionHere = !config.getBoolean("Chat.DisableHere", false);
        mentionEveryone = !config.getBoolean("Chat.DisableEveryone", false);
    }

    /**
     * Everything to keep whole: the hidden tag, every InteractiveChat keyword, and every mention
     * InteractiveChat would pick up (it matches those ignoring case, so do we).
     */
    public List<Pattern> keepPatterns() {
        List<Pattern> out = new ArrayList<>();
        out.add(SENDER_TAG);
        addKeywords(out);
        String prefix = mentionPrefix;
        if (mentions && prefix != null && !prefix.isEmpty()) {
            List<String> names = new ArrayList<>();
            if (mentionHere) names.add("here");
            if (mentionEveryone) names.add("everyone");
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            // Longest first, so "@Steve2" isn't cut short by "@Steve".
            names.sort(Comparator.comparingInt(String::length).reversed());
            for (String name : names) {
                out.add(Pattern.compile(Pattern.quote(prefix + name), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            }
        }
        return out;
    }

    private void addKeywords(List<Pattern> out) {
        if (placeholderList == null) {
            return;
        }
        try {
            for (Object placeholder : (Collection<?>) placeholderList.invoke(null)) {
                if (rawKeyword.invoke(placeholder) instanceof Pattern keyword) {
                    out.add(keyword);
                }
            }
        } catch (Throwable t) {
            if (warned.compareAndSet(false, true)) {
                plugin.getLogger().warning("Could not read InteractiveChat's keywords (" + t.getClass().getSimpleName()
                        + "). Custom keywords may not work with gradients.");
            }
        }
    }
}
