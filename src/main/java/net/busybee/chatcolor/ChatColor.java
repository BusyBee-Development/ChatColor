package net.busybee.chatcolor;

import net.busybee.chatcolor.api.ChatColorAPI;
import net.busybee.chatcolor.commands.ColorCommand;
import net.busybee.chatcolor.config.ConfigManager;
import net.busybee.chatcolor.config.ColorManager;
import net.busybee.chatcolor.config.MessageManager;
import net.busybee.chatcolor.config.PatternManager;
import net.busybee.chatcolor.data.PlayerDataManager;
import net.busybee.chatcolor.hooks.PlaceholderAPIHook;
import net.busybee.chatcolor.inventory.gui.GUIListener;
import net.busybee.chatcolor.inventory.gui.GUIManager;
import net.busybee.chatcolor.listeners.ChatListener;
import net.busybee.chatcolor.utils.VersionCheck;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Getter
public class ChatColor extends JavaPlugin {

    private static ChatColor instance;

    private ConfigManager configManager;
    private ColorManager colorManager;
    private MessageManager messageManager;
    private PatternManager patternManager;
    private PlayerDataManager playerDataManager;
    private GUIManager guiManager;
    private ChatColorAPI chatColorAPI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.colorManager = new ColorManager(this);
        this.messageManager = new MessageManager(this);
        this.patternManager = new PatternManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.guiManager = new GUIManager();
        this.chatColorAPI = new ChatColorAPI(this);

        this.colorManager.load();
        this.configManager.load();
        this.messageManager.load();
        this.patternManager.load();
        this.playerDataManager.load();

        new Metrics(this, 30495);

        registerListeners();
        registerCommands();
        registerHooks();

        getLogger().info("ChatColor enabled successfully by BusyBee.");
    }

    @Override
    public void onDisable() {
        if (this.playerDataManager != null) {
            this.playerDataManager.saveAll();
        }
        getLogger().info("ChatColor disabled. Player data saved.");
    }

    private void registerListeners() {
        EventPriority priority;
        try {
            priority = EventPriority.valueOf(configManager.getEventPriority());
        } catch (IllegalArgumentException e) {
            priority = EventPriority.LOWEST;
            getLogger().warning("Invalid event-priority in config.yml, defaulting to LOWEST");
        }

        ChatListener chatListener = new ChatListener(this);
        if (isPaper()) {
            Bukkit.getPluginManager().registerEvent(AsyncChatEvent.class, chatListener, priority, (listener, event) -> {
                if (event instanceof AsyncChatEvent chatEvent) {
                    ((ChatListener) listener).onChat(chatEvent);
                }
            }, this, true);
        }

        Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, priority, (listener, event) -> {
            if (event instanceof AsyncPlayerChatEvent chatEvent) {
                ((ChatListener) listener).onLegacyChat(chatEvent);
            }
        }, this, true);

        Bukkit.getPluginManager().registerEvents(new GUIListener(this.guiManager), this);
        Bukkit.getPluginManager().registerEvents(new VersionCheck(this), this);
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
    }

    public void reload() {
        reloadConfig();
        this.colorManager.load();
        this.configManager.load();
        this.messageManager.load();
        this.patternManager.load();
        this.playerDataManager.saveAll();
        this.playerDataManager.load();
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
