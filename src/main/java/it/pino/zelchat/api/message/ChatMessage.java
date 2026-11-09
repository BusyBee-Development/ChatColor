// Compile-only stub of the ZelChat API, which is not published to any Maven repository we can
// reach. Left out of the ChatColor jar (see maven-jar-plugin in pom.xml): at runtime the JVM
// links against ZelChat's own classes. Signatures must match ZelChat 2.0 exactly.
package it.pino.zelchat.api.message;

import it.pino.zelchat.api.message.mention.ChatMention;
import it.pino.zelchat.api.message.state.MessageState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface ChatMessage {
    Player getBukkitPlayer();

    MessageState getState();

    Component getMessage();

    void setMessage(Component message);

    Collection<ChatMention> getMentions();
}
