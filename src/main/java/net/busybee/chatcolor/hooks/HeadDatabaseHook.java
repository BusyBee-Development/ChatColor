package net.busybee.chatcolor.hooks;

import net.busybee.chatcolor.ChatColor;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HeadDatabaseHook {

    private final ChatColor plugin;
    private final HeadDatabaseAPI api;
    private final Set<String> warnedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public HeadDatabaseHook(ChatColor plugin) {
        this.plugin = plugin;
        this.api = new HeadDatabaseAPI();
    }

    public ItemStack resolveHead(String id) {
        if (id == null || id.isBlank()) return null;

        try {
            ItemStack head = api.getItemHead(id.trim());
            if (head == null && warnedIds.add(id)) {
                plugin.getLogger().warning("[ChatColor] HeadDatabase id '" + id
                        + "' could not be resolved; falling back to the entry's configured icon material.");
            }
            return head;
        } catch (Exception e) {
            if (warnedIds.add(id)) {
                plugin.getLogger().warning("[ChatColor] Failed to resolve HeadDatabase id '" + id + "': "
                        + e.getMessage() + "; falling back to the entry's configured icon material.");
            }
            return null;
        }
    }
}
