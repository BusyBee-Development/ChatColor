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
import net.busybee.chatcolor.listeners.PlayerListener;
import net.busybee.chatcolor.utils.Warning;
import net.busybee.chatcolor.utils.BStatsManager;
import net.busybee.chatcolor.utils.ChatDebugger;
import net.busybee.chatcolor.utils.ColorUtil;
import net.busybee.chatcolor.utils.DisplayNameService;
import net.busybee.chatcolor.utils.FastStatsManager;
import net.busybee.chatcolor.utils.PermissionRegistrar;
import net.busybee.chatcolor.utils.SchedulerUtil;
import net.busybee.chatcolor.utils.VersionCheck;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.PrintStream;
import java.util.logging.Filter;

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
    private DisplayNameService displayNameService;
    private PermissionRegistrar permissionRegistrar;
    private ChatListener chatListener;
    private ChatDebugger chatDebugger;
    private EventPriority activePriority = EventPriority.NORMAL;
    private volatile boolean legacyHook;
    private BStatsManager bStatsManager;
    private FastStatsManager fastStatsManager;

    public ConfigManager getConfigManager() { return configManager; }
    public ColorManager getColorManager() { return colorManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public PatternManager getPatternManager() { return patternManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public ChatColorAPI getChatColorAPI() { return chatColorAPI; }
    public DisplayNameService getDisplayNameService() { return displayNameService; }
    public PermissionRegistrar getPermissionRegistrar() { return permissionRegistrar; }
    public EventPriority getActivePriority() { return activePriority; }
    public ChatDebugger getChatDebugger() { return chatDebugger; }
    public boolean isLegacyHook() { return legacyHook; }
    public BStatsManager getBStatsManager() { return bStatsManager; }
    public FastStatsManager getFastStatsManager() { return fastStatsManager; }

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
        this.displayNameService = new DisplayNameService(this);
        this.permissionRegistrar = new PermissionRegistrar(this);

        FastInvManager.register(this);

        this.configManager.load();
        this.colorManager.load();
        this.messageManager.load();
        this.guiManager.load();
        this.patternManager.load();
        this.playerDataManager.load();

        refreshPermissions();

        this.bStatsManager = new BStatsManager(this);
        this.fastStatsManager = new FastStatsManager(this);
        this.fastStatsManager.onEnable();

        registerListeners();
        registerCommands();
        registerHooks();
        setupConsoleFilter();
        Warning.send(this);

        getLogger().info("ChatColor enabled successfully by BusyBee.");
    }

    @Override
    public void onDisable() {
        try {
            Bukkit.getLogger().setFilter(null);
            java.util.logging.Logger.getLogger("Minecraft").setFilter(null);
        } catch (Exception ignored) {}

        if (originalOut != null) {
            System.setOut(originalOut);
            originalOut = null;
        }
        if (originalErr != null) {
            System.setErr(originalErr);
            originalErr = null;
        }
        if (this.chatDebugger != null) {
            this.chatDebugger.stop();
        }
        if (this.fastStatsManager != null) {
            this.fastStatsManager.onDisable();
        }
        if (this.permissionRegistrar != null) {
            this.permissionRegistrar.clear();
        }
        if (this.colorManager != null) {
            this.colorManager.flush();
        }
        if (this.playerDataManager != null) {
            this.playerDataManager.saveAll();
        }
        getLogger().info("ChatColor disabled. Player data saved.");
    }

    private void registerListeners() {
        this.chatListener = new ChatListener(this);
        this.chatDebugger = new ChatDebugger(this);

        Bukkit.getPluginManager().registerEvents(new VersionCheck(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        SchedulerUtil.runDelayedSync(this, this::bindChatListener, 1L);
    }

    public void bindChatListener() {
        if (!isEnabled() || chatListener == null) {
            return;
        }

        unbindChatListener();

        boolean legacy = resolveLegacyChatHook();
        this.legacyHook = legacy;
        resolveActivePriority(legacy);

        if (legacy) {
            Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, activePriority, (listener, event) -> {
                if (event instanceof AsyncPlayerChatEvent chatEvent) {
                    ((ChatListener) listener).onLegacyChat(chatEvent);
                }
            }, this, true);
        } else {
            Bukkit.getPluginManager().registerEvent(AsyncChatEvent.class, chatListener, activePriority, (listener, event) -> {
                if (event instanceof AsyncChatEvent chatEvent) {
                    ((ChatListener) listener).onChat(chatEvent);
                }
            }, this, true);
        }

        getLogger().info("Chat hook: " + (legacy ? "LEGACY (AsyncPlayerChatEvent)" : "MODERN (AsyncChatEvent)")
                + ", priority " + activePriority.name());

        if (chatDebugger != null) {
            chatDebugger.rebind();
            if (chatDebugger.isActive()) {
                chatDebugger.dumpPipeline();
            }
        }

        if (legacy && isPaper() && !ColorUtil.platformLegacySupportsHex()) {
            getLogger().warning("======================================================");
            getLogger().warning("[ChatColor] Gradients will be approximated in chat.");
            getLogger().warning("Another plugin owns your chat format, so Paper renders every message");
            getLogger().warning("through its legacy serializer - and this server's build of that");
            getLogger().warning("serializer has no hex support, so #FF7F00 arrives as the nearest of");
            getLogger().warning("the 16 named colours (gold). Solid colours are unaffected.");
            getLogger().warning("For exact gradients, set settings.chat-hook to MODERN and let");
            getLogger().warning("ChatColor own the format instead of that plugin.");
            getLogger().warning("======================================================");
        }
    }

    private void unbindChatListener() {
        if (isPaper()) {
            AsyncChatEvent.getHandlerList().unregister(this);
        }
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

    private boolean resolveLegacyChatHook() {
        if (!isPaper()) {
            return true;
        }

        String configured = configManager.getChatHook();
        if ("MODERN".equalsIgnoreCase(configured)) return false;
        if ("LEGACY".equalsIgnoreCase(configured)) return true;

        if (configured != null && !"AUTO".equalsIgnoreCase(configured)) {
            getLogger().warning("Invalid chat-hook in config.yml: " + configured + ", using AUTO.");
        }

        return false;
    }

    private EventPriority autoDetectPriority(boolean legacy) {
        if (!legacy) {
            return EventPriority.HIGHEST;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LPC") ||
            Bukkit.getPluginManager().isPluginEnabled("DeluxeChat") ||
            Bukkit.getPluginManager().isPluginEnabled("EssentialsChat")) {
            return EventPriority.HIGHEST;
        }
        return EventPriority.NORMAL;
    }

    private void registerCommands() {
        ColorCommand colorCommand = new ColorCommand(this);
        getCommand("color").setExecutor(colorCommand);
        getCommand("color").setTabCompleter(colorCommand);
    }

    private void registerHooks() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
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

        refreshPermissions();
        registerListenersOnReload();
        this.displayNameService.refreshAll();
    }

    public void refreshPermissions() {
        if (this.permissionRegistrar != null) {
            this.permissionRegistrar.refresh();
        }
    }

    private void registerListenersOnReload() {
        SchedulerUtil.runSync(this, this::bindChatListener);
    }

    private void resolveActivePriority(boolean legacy) {
        String configPriority = configManager.getEventPriority();
        if (configPriority != null && !configPriority.equalsIgnoreCase("DEFAULT")) {
            try {
                activePriority = EventPriority.valueOf(configPriority.toUpperCase());
                return;
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid event-priority in config.yml: " + configPriority + ", using auto-detection.");
            }
        }
        activePriority = autoDetectPriority(legacy);
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
