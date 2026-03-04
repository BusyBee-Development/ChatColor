package net.busybee.chatcolor.hook;

import net.busybee.chatcolor.CPlayer;
import net.busybee.chatcolor.ChatColorPlugin;
import net.busybee.chatcolor.pattern.api.BasePattern;
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

                String miniMessageSep = ": <white>";
                SimpleComponent miniColored = SimpleComponent.fromSection(miniMessageSep + coloredMessage);
                SimpleComponent updatedFormat = currentFormat.replaceLiteral(miniMessageSep + originalMessage, miniColored);

                if (updatedFormat.equals(currentFormat)) {
                    String legacySep = ": §f";
                    SimpleComponent legacyColored = SimpleComponent.fromSection(legacySep + coloredMessage);
                    updatedFormat = currentFormat.replaceLiteral(legacySep + originalMessage, legacyColored);
                }

                if (updatedFormat.equals(currentFormat)) {
                    String stdSep = ": ";
                    SimpleComponent stdColored = SimpleComponent.fromSection(stdSep + coloredMessage);
                    updatedFormat = currentFormat.replaceLiteral(stdSep + originalMessage, stdColored);
                }

                if (updatedFormat.equals(currentFormat)) {
                    updatedFormat = currentFormat.replaceLiteral(originalMessage, SimpleComponent.fromSection(coloredMessage));
                }

                event.setFormat(updatedFormat);
            }
        }
    }
}
