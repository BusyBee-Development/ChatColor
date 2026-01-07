package me.mattyhd0.chatcolor.hook;

import me.mattyhd0.chatcolor.CPlayer;
import me.mattyhd0.chatcolor.ChatColorPlugin;
import me.mattyhd0.chatcolor.pattern.api.BasePattern;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.mineacademy.chatcontrol.api.ChannelPostChatEvent;
import org.mineacademy.chatcontrol.lib.model.SimpleComponent;

public class ChatControlHook implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChatControlMessage(ChannelPostChatEvent event) {

        if (!(event.getSender() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getSender();
        CPlayer cPlayer = ChatColorPlugin.getInstance().getDataMap().get(player.getUniqueId());

        if (cPlayer != null) {
            BasePattern pattern = cPlayer.getPattern();

            if (pattern != null && cPlayer.canUsePattern(pattern)) {

                String originalMessage = event.getMessage();
                String coloredMessage = pattern.getText(ChatColor.stripColor(originalMessage));

                // Convert the colored message to SimpleComponent
                SimpleComponent coloredComponent = SimpleComponent.fromSection(coloredMessage);

                // Simply replace the literal message text - this will replace all occurrences
                // but in practice, the message text usually only appears once in the actual message part
                SimpleComponent updatedFormat = event.getFormat().replaceLiteral(originalMessage, coloredComponent);
                event.setFormat(updatedFormat);
            }
        }
    }
}
