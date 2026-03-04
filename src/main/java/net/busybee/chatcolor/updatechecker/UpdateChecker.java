package net.busybee.chatcolor.updatechecker;

import org.bukkit.plugin.Plugin;

public class UpdateChecker {

    private String version;
    private ModrinthVersion modrinthVersion;

    public UpdateChecker(Plugin plugin, String slug){

        version = plugin.getDescription().getVersion();
        modrinthVersion = ModrinthAPI.getLatestVersion(slug);

    }

    public boolean isRunningLatestVersion() {
        return compareVersions(version, modrinthVersion.getVersionNumber()) >= 0;
    }

    private int compareVersions(String v1, String v2) {
        String[] v1Parts = v1.split("\\.");
        String[] v2Parts = v2.split("\\.");
        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            String p1 = i < v1Parts.length ? v1Parts[i] : "0";
            String p2 = i < v2Parts.length ? v2Parts[i] : "0";

            try {
                int i1 = Integer.parseInt(p1);
                int i2 = Integer.parseInt(p2);
                if (i1 != i2) {
                    return Integer.compare(i1, i2);
                }
            } catch (NumberFormatException e) {
                int cmp = p1.compareTo(p2);
                if (cmp != 0) return cmp;
            }
        }
        return 0;
    }

    public String getVersion() {
        return version;
    }
    
    public String getLatestVersion() {
        return modrinthVersion.getVersionNumber();
    }

    public ModrinthVersion getModrinthVersion() {
        return modrinthVersion;
    }

    public boolean requestIsValid() {
        return modrinthVersion != null;
    }

}
