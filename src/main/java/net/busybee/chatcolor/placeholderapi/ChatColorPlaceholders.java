package net.busybee.chatcolor.placeholderapi;

import net.busybee.chatcolor.CPlayer;
import org.bukkit.entity.Player;
import net.busybee.chatcolor.ChatColorPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.plugin.Plugin;

public class ChatColorPlaceholders extends PlaceholderExpansion
{
    private Plugin plugin;
    
    public ChatColorPlaceholders() {
        this.plugin = ChatColorPlugin.getInstance();
    }
    
    public boolean canRegister() {
        return true;
    }
    
    public String getAuthor() {
        return "MattyHD0";
    }
    
    public String getIdentifier() {
        return "chatcolor";
    }
    
    public String getRequiredPlugin() {
        return "ChatColor";
    }
    
    public String getVersion() {
        return "1.0";
    }
    
    public String onPlaceholderRequest(Player player, String identifier) {

        switch (identifier){
            case "last_message": {
                CPlayer cPlayer = ChatColorPlugin.getInstance().getDataMap().get(player.getUniqueId());
                return cPlayer == null ? "" : cPlayer.getLastMessages();
            }
            case "code": {
                CPlayer cPlayer = ChatColorPlugin.getInstance().getDataMap().get(player.getUniqueId());
                if (cPlayer == null || cPlayer.getPattern() == null) {
                    return "";
                }
                return cPlayer.getPattern().getRawColorCode();
            }
            case "pattern_name": {
                CPlayer cPlayer = ChatColorPlugin.getInstance().getDataMap().get(player.getUniqueId());
                if(cPlayer == null) {
                    return "";
                }else{
                    return cPlayer.getPattern() == null ? "" : cPlayer.getPattern().getName(false);
                }
            }
            case "pattern_name_formatted":
                CPlayer cPlayer = ChatColorPlugin.getInstance().getDataMap().get(player.getUniqueId());
                if(cPlayer == null) {
                    return "";
                }else{
                    return cPlayer.getPattern() == null ? "" : cPlayer.getPattern().getName(true);
                }
            default:
                return "";
        }

    }

}
