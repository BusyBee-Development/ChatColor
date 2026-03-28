package net.busybee.chatcolor.pattern.type;

import net.busybee.chatcolor.pattern.*;
import net.busybee.chatcolor.pattern.api.BasePattern;
import net.busybee.chatcolor.pattern.format.TextFormatOptions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;

public enum PatternType {

    SINGLE(SinglePattern.class),
    LINEAR(LinearPattern.class),
    LINEAR_IGNORE_SPACES(LinearIgnoreSpacesPattern.class),
    RANDOM(RandomPattern.class),
    GRADIENT(GradientPattern.class),
    GRADIENT_RANDOM(RandomGradientPattern.class),
    SINE_WAVE(SineWavePattern.class) {
        @Override
        public <T extends BasePattern> T buildPattern(String name, String category, String permission, TextFormatOptions formatOptions, ConfigurationSection config, ChatColor... colors) {
            double freq = config.getDouble("frequency", 0.5);
            double phase = config.getDouble("phase", 0.0);
            return (T) new SineWavePattern(name, category, permission, formatOptions, freq, phase, colors);
        }
    };

    private Class clazz;
    PatternType(Class<? extends BasePattern> clazz){
        this.clazz = clazz;
    }

    public <T extends BasePattern > T buildPattern(String name, String category, String permission, TextFormatOptions formatOptions, ConfigurationSection config, ChatColor... colors) {

        try {
            Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(String.class, String.class, TextFormatOptions.class, ChatColor[].class);
            T pattern = constructor.newInstance(name, permission, formatOptions, colors);
            pattern.setCategory(category);
            return pattern;
        } catch (Exception e){
            try {
                // Fallback for older constructors if they don't have the array as last param or something
                Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
                Constructor<T> constructor = constructors[0];
                T pattern = constructor.newInstance(name, permission, formatOptions, colors);
                pattern.setCategory(category);
                return pattern;
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }

    }
}
