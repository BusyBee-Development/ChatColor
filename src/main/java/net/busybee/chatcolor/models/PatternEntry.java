package net.busybee.chatcolor.models;

import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class PatternEntry implements SelectableEntry {

    private final String key;
    private final String displayName;
    private final String permission;
    private final String iconMaterial;
    private final List<String> colors;
    private final PermissionDefault permissionDefault;

    public PatternEntry(String key, String displayName, String permission, String iconMaterial, List<String> colors) {
        this(key, displayName, permission, iconMaterial, colors, PermissionDefault.OP);
    }

    public PatternEntry(String key, String displayName, String permission, String iconMaterial, List<String> colors,
                        PermissionDefault permissionDefault) {
        this.key = key;
        this.displayName = displayName;
        this.permission = (permission == null || permission.isBlank()) ? null : permission;
        this.iconMaterial = iconMaterial;
        this.colors = colors;
        this.permissionDefault = permissionDefault == null ? PermissionDefault.OP : permissionDefault;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getPermission() { return permission; }
    public String getIconMaterial() { return iconMaterial; }
    public List<String> getColors() { return colors; }
    public PermissionDefault getPermissionDefault() { return permissionDefault; }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public String getEntryType() {
        return "PATTERN";
    }
}
