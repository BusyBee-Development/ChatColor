package net.busybee.chatcolor.models;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

public interface SelectableEntry {

    String getKey();
    String getDisplayName();
    String getPermission();
    PermissionDefault getPermissionDefault();
    String getIconMaterial();
    String getTag();
    String getEntryType();

    default boolean isPublic() {
        String permission = getPermission();
        return permission == null || permission.isBlank();
    }

    default boolean isAllowed(Permissible who) {
        return isPublic() || who.hasPermission(getPermission());
    }
}
