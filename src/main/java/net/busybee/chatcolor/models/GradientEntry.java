package net.busybee.chatcolor.models;

import org.bukkit.permissions.PermissionDefault;

public class GradientEntry implements SelectableEntry {

    private final String key;
    private final String displayName;
    private final String tag;
    private final String permission;
    private final String iconMaterial;
    private final PermissionDefault permissionDefault;

    public GradientEntry(String key, String displayName, String tag, String permission, String iconMaterial) {
        this(key, displayName, tag, permission, iconMaterial, PermissionDefault.OP);
    }

    public GradientEntry(String key, String displayName, String tag, String permission, String iconMaterial,
                         PermissionDefault permissionDefault) {
        this.key = key;
        this.displayName = displayName;
        this.tag = tag;
        this.permission = (permission == null || permission.isBlank()) ? null : permission;
        this.iconMaterial = iconMaterial;
        this.permissionDefault = permissionDefault == null ? PermissionDefault.OP : permissionDefault;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getTag() { return tag; }
    public String getPermission() { return permission; }
    public String getIconMaterial() { return iconMaterial; }
    public PermissionDefault getPermissionDefault() { return permissionDefault; }

    @Override
    public String getEntryType() {
        return "GRADIENT";
    }
}
