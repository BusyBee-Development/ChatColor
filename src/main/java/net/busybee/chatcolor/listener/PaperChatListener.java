package net.busybee.chatcolor.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.busybee.chatcolor.CPlayer;
import net.busybee.chatcolor.ChatColorPlugin;
import net.busybee.chatcolor.MyChatColor;
import net.busybee.chatcolor.configuration.SimpleYMLConfiguration;
import net.busybee.chatcolor.pattern.api.BasePattern;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class PaperChatListener implements Listener {

    private final ChatColorPlugin plugin;

    public PaperChatListener(ChatColorPlugin plugin) {
        this.plugin = plugin;
    }

    public void onChat(AsyncChatEvent event) {
        SimpleYMLConfiguration config = plugin.getConfigurationManager().getConfig();
        Player player = event.getPlayer();
        CPlayer cPlayer = plugin.getDataMap().get(player.getUniqueId());

        if (config.getBoolean("config.translate-chat-colors")) {
            String messageStr = PlainTextComponentSerializer.plainText().serialize(event.message());
            String translatedMessage = MyChatColor.translateAlternateColorCodes(messageStr, player);
            event.message(MyChatColor.parseMiniMessageToComponent(translatedMessage));
        }

        if (cPlayer != null) {
            BasePattern pattern = cPlayer.getPattern();
            if (pattern != null) {
                boolean showPatternIfHasPerm = config.getBoolean("config.show-pattern-only-if-has-permissions");
                String messageStr = PlainTextComponentSerializer.plainText().serialize(event.message());
                String coloredMessage = pattern.getText(messageStr);

                if (!showPatternIfHasPerm || cPlayer.canUsePattern(pattern)) {
                    event.message(MyChatColor.parseMiniMessageToComponent(coloredMessage));
                }
            }
            cPlayer.setLastMessages(PlainTextComponentSerializer.plainText().serialize(event.message()));
        }
    }
}
