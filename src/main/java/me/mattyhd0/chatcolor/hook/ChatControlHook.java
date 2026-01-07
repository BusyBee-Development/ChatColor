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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
                String cleanMessage = ChatColor.stripColor(originalMessage);
                String coloredMessage = pattern.getText(cleanMessage);

                SimpleComponent currentFormat = event.getFormat();

                // 1. Try to replace for Admin/Global/HelpOp (using the <white> tag from your YML)
                String miniMessageSep = ": <white>";
                SimpleComponent miniColored = SimpleComponent.fromSection(miniMessageSep + coloredMessage);
                SimpleComponent updatedFormat = currentFormat.replaceLiteral(miniMessageSep + originalMessage, miniColored);

                // 2. If no change, try a version with legacy color tags if they exist (Fallback)
                if (updatedFormat.equals(currentFormat)) {
                    String legacySep = ": §f";
                    SimpleComponent legacyColored = SimpleComponent.fromSection(legacySep + coloredMessage);
                    updatedFormat = currentFormat.replaceLiteral(legacySep + originalMessage, legacyColored);
                }

                // 3. If no change, try the Standard separator (": ")
                if (updatedFormat.equals(currentFormat)) {
                    String stdSep = ": ";
                    SimpleComponent stdColored = SimpleComponent.fromSection(stdSep + coloredMessage);
                    updatedFormat = currentFormat.replaceLiteral(stdSep + originalMessage, stdColored);
                }

                // 4. Final Fallback: If everything else fails, apply color directly to the message
                if (updatedFormat.equals(currentFormat)) {
                    updatedFormat = currentFormat.replaceLiteral(originalMessage, SimpleComponent.fromSection(coloredMessage));
                }

                event.setFormat(updatedFormat);
            }
        }
    }
}