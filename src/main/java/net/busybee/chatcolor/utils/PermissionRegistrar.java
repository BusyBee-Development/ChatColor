package net.busybee.chatcolor.utils;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.models.SelectableEntry;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PermissionRegistrar {

    private static final String NAMESPACE = "chatcolor.";
    private final ChatColor plugin;
    private final Set<String> ownedNodes = new LinkedHashSet<>();
    private final Map<String, Set<String>> attachedChildren = new LinkedHashMap<>();
    public PermissionRegistrar(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void refresh() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        clear();

        List<SelectableEntry> entries = new ArrayList<>();
        entries.addAll(plugin.getColorManager().getColorList());
        entries.addAll(plugin.getColorManager().getGradientList());
        entries.addAll(plugin.getPatternManager().getPatternList());

        int registered = 0;
        int open = 0;
        for (SelectableEntry entry : entries) {
            if (entry.isPublic()) {
                open++;
                continue;
            }
            if (register(pluginManager, entry)) {
                registered++;
            }
        }

        plugin.getLogger().info("Registered " + registered + " colour permission(s)"
                + (open > 0 ? ", " + open + " open to everyone" : "") + ".");
    }

    public void clear() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for (Map.Entry<String, Set<String>> attached : attachedChildren.entrySet()) {
            Permission wildcard = pluginManager.getPermission(attached.getKey());
            if (wildcard == null) continue;
            wildcard.getChildren().keySet().removeAll(attached.getValue());
            pluginManager.recalculatePermissionDefaults(wildcard);
            wildcard.recalculatePermissibles();
        }
        attachedChildren.clear();

        for (String node : ownedNodes) {
            pluginManager.removePermission(node);
        }
        ownedNodes.clear();
    }

    private boolean register(PluginManager pluginManager, SelectableEntry entry) {
        String node = entry.getPermission();
        Permission permission = pluginManager.getPermission(node);

        if (permission == null) {
            permission = new Permission(node, describe(entry), defaultOf(entry));
            try {
                pluginManager.addPermission(permission);
            } catch (IllegalArgumentException alreadyRegistered) {
                permission = pluginManager.getPermission(node);
                if (permission == null) return false;
            }
            ownedNodes.add(node);
            pluginManager.recalculatePermissionDefaults(permission);
        }

        attachWildcards(pluginManager, permission);
        return true;
    }

    private void attachWildcards(PluginManager pluginManager, Permission permission) {
        String node = permission.getName();
        if (!node.startsWith(NAMESPACE)) return;

        String[] parts = node.split("\\.");
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            prefix.append(parts[i]).append('.');
            String wildcard = prefix + "*";

            if (pluginManager.getPermission(wildcard) == null) {
                ownedNodes.add(wildcard);
            }
            attachedChildren.computeIfAbsent(wildcard, k -> new LinkedHashSet<>()).add(node);
            permission.addParent(wildcard, true);
        }
    }

    private PermissionDefault defaultOf(SelectableEntry entry) {
        PermissionDefault permissionDefault = entry.getPermissionDefault();
        return permissionDefault == null ? PermissionDefault.OP : permissionDefault;
    }

    private String describe(SelectableEntry entry) {
        String name = ColorUtil.stripAll(entry.getDisplayName());
        return "ChatColor " + entry.getEntryType().toLowerCase() + ": " + name;
    }
}
