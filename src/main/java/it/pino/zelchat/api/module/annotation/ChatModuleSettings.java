// Compile-only stub of the ZelChat API, which is not published to any Maven repository we can
// reach. Left out of the ChatColor jar (see maven-jar-plugin in pom.xml): at runtime the JVM
// links against ZelChat's own classes. Signatures must match ZelChat 2.0 exactly.
package it.pino.zelchat.api.module.annotation;

import it.pino.zelchat.api.module.priority.ModulePriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChatModuleSettings {
    String pluginOwner();

    ModulePriority priority() default ModulePriority.NORMAL;
}
