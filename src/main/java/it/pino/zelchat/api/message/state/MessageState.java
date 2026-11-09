// Compile-only stub of the ZelChat API, which is not published to any Maven repository we can
// reach. Left out of the ChatColor jar (see maven-jar-plugin in pom.xml): at runtime the JVM
// links against ZelChat's own classes. Signatures must match ZelChat 2.0 exactly.
package it.pino.zelchat.api.message.state;

public enum MessageState {
    PROCESSING, READY, CANCELLED, FILTERED, FILTERED_CANCELLED
}
