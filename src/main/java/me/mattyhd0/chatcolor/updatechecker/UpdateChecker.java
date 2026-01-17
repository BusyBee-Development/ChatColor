package me.mattyhd0.chatcolor.updatechecker;

import org.bukkit.plugin.Plugin;

public class UpdateChecker {

    private String version;
    private ModrinthVersion modrinthVersion;

    public UpdateChecker(Plugin plugin, String slug){

        version = plugin.getDescription().getVersion();
        modrinthVersion = ModrinthAPI.getLatestVersion(slug);

    }

    public boolean isRunningLatestVersion() {
        return version.equals(modrinthVersion.getVersionNumber());
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
