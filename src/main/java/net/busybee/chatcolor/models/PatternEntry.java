package net.busybee.chatcolor.models;

import java.util.List;

public class PatternEntry implements SelectableEntry {

    private final String key;
    private final String displayName;
    private final String permission;
    private final String iconMaterial;
    private final List<String> colors;

    public PatternEntry(String key, String displayName, String permission, String iconMaterial, List<String> colors) {
        this.key = key;
        this.displayName = displayName;
        this.permission = permission;
        this.iconMaterial = iconMaterial;
        this.colors = colors;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getPermission() { return permission; }
    public String getIconMaterial() { return iconMaterial; }
    public List<String> getColors() { return colors; }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public String getEntryType() {
        return "PATTERN";
    }
}
