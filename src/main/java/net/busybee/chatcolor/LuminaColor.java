package net.busybee.chatcolor;

import net.busybee.chatcolor.api.LuminaColorAPI;
import net.busybee.chatcolor.commands.ColorCommand;
import net.busybee.chatcolor.config.ConfigManager;
import net.busybee.chatcolor.config.MessageManager;
import net.busybee.chatcolor.config.PatternManager;
import net.busybee.chatcolor.data.PlayerDataManager;
import net.busybee.chatcolor.hooks.PlaceholderAPIHook;
import net.busybee.chatcolor.inventory.gui.GUIListener;
import net.busybee.chatcolor.inventory.gui.GUIManager;
import net.busybee.chatcolor.listeners.ChatListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class LuminaColor extends JavaPlugin {

    private static LuminaColor instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private PatternManager patternManager;
    private PlayerDataManager playerDataManager;
    private GUIManager guiManager;
    private LuminaColorAPI luminaColorAPI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.patternManager = new PatternManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.guiManager = new GUIManager();
        this.luminaColorAPI = new LuminaColorAPI(this);

        this.configManager.load();
        this.messageManager.load();
        this.patternManager.load();
        this.playerDataManager.load();

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
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this.guiManager), this);
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
        this.configManager.load();
        this.messageManager.load();
        this.patternManager.load();
        this.playerDataManager.saveAll();
        this.playerDataManager.load();
    }

    public static LuminaColor getInstance() {
        return instance;
    }
}