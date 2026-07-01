package net.busybee.chatcolor;

import net.busybee.chatcolor.api.ChatColorAPI;
import net.busybee.chatcolor.commands.ColorCommand;
import net.busybee.chatcolor.config.ConfigManager;
import net.busybee.chatcolor.config.ColorManager;
import net.busybee.chatcolor.config.MessageManager;
import net.busybee.chatcolor.config.GuiManager;
import net.busybee.chatcolor.config.PatternManager;
import net.busybee.chatcolor.data.PlayerDataManager;
import net.busybee.chatcolor.hooks.PlaceholderAPIHook;
import fr.mrmicky.fastinv.FastInvManager;
import net.busybee.chatcolor.listeners.ChatListener;
import net.busybee.chatcolor.utils.BStatsManager;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.FastStatsManager;
import net.busybee.chatcolor.utils.VersionCheck;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.PrintStream;
import java.util.logging.Filter;

@Getter
public class ChatColor extends JavaPlugin {

    private static ChatColor instance;
    private static PrintStream originalOut;
    private static PrintStream originalErr;

    private ConfigManager configManager;
    private ColorManager colorManager;
    private MessageManager messageManager;
    private GuiManager guiManager;
    private PatternManager patternManager;
    private PlayerDataManager playerDataManager;
    private ChatColorAPI chatColorAPI;

    private EventPriority activePriority;

    private BStatsManager bStatsManager;
    private FastStatsManager fastStatsManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.colorManager = new ColorManager(this);
        this.messageManager = new MessageManager(this);
        this.guiManager = new GuiManager(this);
        this.patternManager = new PatternManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.chatColorAPI = new ChatColorAPI(this);

        FastInvManager.register(this);

        this.configManager.load();
        this.colorManager.load();
        this.messageManager.load();
        this.guiManager.load();
        this.patternManager.load();
        this.playerDataManager.load();

        this.bStatsManager = new BStatsManager(this);
        this.fastStatsManager = new FastStatsManager(this);
        this.fastStatsManager.onEnable();

        registerListeners();
        registerCommands();
        registerHooks();
        setupConsoleFilter();

        getLogger().info("ChatColor enabled successfully by BusyBee.");
    }

    @Override
    public void onDisable() {
        if (originalOut != null) {
            System.setOut(originalOut);
            originalOut = null;
        }
        if (originalErr != null) {
            System.setErr(originalErr);
            originalErr = null;
        }
        if (this.fastStatsManager != null) {
            this.fastStatsManager.onDisable();
        }
        if (this.playerDataManager != null) {
            this.playerDataManager.saveAll();
        }
        getLogger().info("ChatColor disabled. Player data saved.");
    }

    private void registerListeners() {
        activePriority = EventPriority.HIGHEST;

        String configPriority = configManager.getEventPriority();
        if (configPriority != null && !configPriority.equalsIgnoreCase("DEFAULT")) {
            try {
                activePriority = EventPriority.valueOf(configPriority.toUpperCase());
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid event-priority in config.yml: " + configPriority + ", using auto-detection.");
                activePriority = autoDetectPriority();
            }
        } else {
            activePriority = autoDetectPriority();
        }

        getLogger().info("Chat listener registered with priority: " + activePriority.name());

        ChatListener chatListener = new ChatListener(this);
        if (isPaper()) {
             Bukkit.getPluginManager().registerEvent(AsyncChatEvent.class, chatListener, activePriority, (listener, event) -> {
                if (event instanceof AsyncChatEvent chatEvent) {
                    ((ChatListener) listener).onChat(chatEvent);
                }
            }, this, true);
        }

        Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, activePriority, (listener, event) -> {
            if (event instanceof AsyncPlayerChatEvent chatEvent) {
                ((ChatListener) listener).onLegacyChat(chatEvent);
            }
        }, this, true);

        Bukkit.getPluginManager().registerEvents(new VersionCheck(this), this);
    }

    private EventPriority autoDetectPriority() {
        if (Bukkit.getPluginManager().isPluginEnabled("EssentialsChat") || 
            Bukkit.getPluginManager().isPluginEnabled("LPC") ||
            Bukkit.getPluginManager().isPluginEnabled("ChatControl") ||
            Bukkit.getPluginManager().isPluginEnabled("DeluxeChat") ||
            Bukkit.getPluginManager().isPluginEnabled("EssentialsC")) {
            getLogger().info("Detected compatible chat plugin. Using MONITOR priority.");
            return EventPriority.MONITOR;
        }
        return EventPriority.HIGHEST;
    }

    private void registerCommands() {
        ColorCommand colorCommand = new ColorCommand(this);
        getCommand("color").setExecutor(colorCommand);
        getCommand("color").setTabCompleter(colorCommand);
    }

    private void registerHooks() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI hook registered.");
        }
        new net.busybee.chatcolor.hooks.IntegrationChecker(this).check();
    }

    private void setupConsoleFilter() {
        if (configManager.isCleanConsole()) {
            if (originalOut == null) {
                originalOut = System.out;
                System.setOut(new net.busybee.chatcolor.utils.ConsoleFilterStream(originalOut));
            }
            if (originalErr == null) {
                originalErr = System.err;
                System.setErr(new net.busybee.chatcolor.utils.ConsoleFilterStream(originalErr));
            }

            Filter filter = record -> {
                if (record.getMessage() != null) {
                    record.setMessage(ColorUtil.stripAll(record.getMessage()));
                }
                return true;
            };
            Bukkit.getLogger().setFilter(filter);
            java.util.logging.Logger.getLogger("Minecraft").setFilter(filter);
        } else {
            if (originalOut != null) {
                System.setOut(originalOut);
                originalOut = null;
            }
            if (originalErr != null) {
                System.setErr(originalErr);
                originalErr = null;
            }
            Bukkit.getLogger().setFilter(null);
            java.util.logging.Logger.getLogger("Minecraft").setFilter(null);
        }
    }

    public void reload() {
        reloadConfig();
        this.configManager.load();
        this.colorManager.load();
        this.messageManager.load();
        this.guiManager.load();
        this.patternManager.load();
        this.playerDataManager.saveAll();
        this.playerDataManager.load();
        setupConsoleFilter();
    }

    public boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static ChatColor getInstance() {
        return instance;
    }
}
