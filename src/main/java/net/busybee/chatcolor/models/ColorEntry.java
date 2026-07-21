package net.busybee.chatcolor.models;

public class ColorEntry implements SelectableEntry {

    private final String key;
    private final String displayName;
    private final String tag;
    private final String permission;
    private final String iconMaterial;

    public ColorEntry(String key, String displayName, String tag, String permission, String iconMaterial) {
        this.key = key;
        this.displayName = displayName;
        this.tag = tag;
        this.permission = permission;
        this.iconMaterial = iconMaterial;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getTag() { return tag; }
    public String getPermission() { return permission; }
    public String getIconMaterial() { return iconMaterial; }

    @Override
    public String getEntryType() {
        return "SOLID";
    }
}
