package net.busybee.chatcolor.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.busybee.chatcolor.ChatColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation") // getDisplayName/setDisplayName: needed for Spigot
public final class DisplayNameService {

    private final ChatColor plugin;
    private final Map<UUID, String> applied = new ConcurrentHashMap<>();

    public DisplayNameService(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void refresh(Player player) {
        if (player == null || !player.isOnline()) return;

        SchedulerUtil.runTaskForEntity(plugin, player, () -> {
            if (!player.isOnline()) return;
            apply(player, resolve(player));
        });
    }

    public void refresh(UUID uuid) {
        if (uuid == null) return;
        refresh(Bukkit.getPlayer(uuid));
    }

    public void refreshAll() {
        SchedulerUtil.runSync(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                refresh(player);
            }
        });
    }

    public void forget(UUID uuid) {
        applied.remove(uuid);
    }

    private Component resolve(Player player) {
        if (!plugin.getConfigManager().isApplyToName()) return null;

        Component plain = Component.text(player.getName());
        Component colored = plugin.getChatColorAPI().applyColorToComponent(player, plain);
        return colored.equals(plain) ? null : colored;
    }

    private void apply(Player player, Component colored) {
        UUID uuid = player.getUniqueId();

        if (colored == null) {
            String ours = applied.remove(uuid);
            if (ours != null && ours.equals(player.getDisplayName())) {
                setDisplayName(player, Component.text(player.getName()));
            }
            return;
        }

        setDisplayName(player, colored);
        applied.put(uuid, player.getDisplayName());
    }

    private void setDisplayName(Player player, Component name) {
        if (plugin.isPaper()) {
            player.displayName(name);
        } else {
            player.setDisplayName(ColorUtil.toLegacy(name));
        }
    }
}
