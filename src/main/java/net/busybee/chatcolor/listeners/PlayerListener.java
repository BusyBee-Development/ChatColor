package net.busybee.chatcolor.listeners;

import net.busybee.chatcolor.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final ChatColor plugin;
    public PlayerListener(ChatColor plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getDisplayNameService().refresh(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDisplayNameService().forget(event.getPlayer().getUniqueId());
        ChatListener.forget(event.getPlayer().getUniqueId());
    }
}
