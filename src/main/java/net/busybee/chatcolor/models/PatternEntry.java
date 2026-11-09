package net.busybee.chatcolor.models;

import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class PatternEntry implements SelectableEntry {

    /** How a pattern hands out its colors, set by 'mode' in patterns.yml. */
    public enum Mode {
        /** Each letter takes the next color in the list, looping. Spaces are skipped. */
        CYCLE,
        /** Each letter takes a random color from the list, never the same one twice in a row. */
        RANDOM,
        /** Each message fades between two colors picked at random from the list. */
        RANDOM_GRADIENT
    }

    private final String key;
    private final String displayName;
    private final String permission;
    private final String iconMaterial;
    private final List<String> colors;
    private final PermissionDefault permissionDefault;
    private final String hdbId;
    private final Mode mode;

    public PatternEntry(String key, String displayName, String permission, String iconMaterial, List<String> colors) {
        this(key, displayName, permission, iconMaterial, colors, PermissionDefault.OP, null);
    }

    public PatternEntry(String key, String displayName, String permission, String iconMaterial, List<String> colors,
                        PermissionDefault permissionDefault) {
        this(key, displayName, permission, iconMaterial, colors, permissionDefault, null);
    }

    public PatternEntry(String key, String displayName, String permission, String iconMaterial, List<String> colors,
                        PermissionDefault permissionDefault, String hdbId) {
        this(key, displayName, permission, iconMaterial, colors, permissionDefault, hdbId, Mode.CYCLE);
    }

    public PatternEntry(String key, String displayName, String permission, String iconMaterial, List<String> colors,
                        PermissionDefault permissionDefault, String hdbId, Mode mode) {
        this.key = key;
        this.displayName = displayName;
        this.permission = (permission == null || permission.isBlank()) ? null : permission;
        this.iconMaterial = iconMaterial;
        this.colors = colors;
        this.permissionDefault = permissionDefault == null ? PermissionDefault.OP : permissionDefault;
        this.hdbId = (hdbId == null || hdbId.isBlank()) ? null : hdbId.trim();
        this.mode = mode == null ? Mode.CYCLE : mode;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getPermission() { return permission; }
    public String getIconMaterial() { return iconMaterial; }
    public String getHdbId() { return hdbId; }
    public List<String> getColors() { return colors; }
    public PermissionDefault getPermissionDefault() { return permissionDefault; }
    public Mode getMode() { return mode; }
    /** True when the colors change from message to message, so callers need a seed. */
    public boolean isRandom() { return mode != Mode.CYCLE; }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public String getEntryType() {
        return "PATTERN";
    }
}
