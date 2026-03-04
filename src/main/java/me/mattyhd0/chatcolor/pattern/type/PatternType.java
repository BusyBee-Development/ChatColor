package me.mattyhd0.chatcolor.pattern.type;

import me.mattyhd0.chatcolor.pattern.*;
import me.mattyhd0.chatcolor.pattern.api.BasePattern;
import me.mattyhd0.chatcolor.pattern.format.TextFormatOptions;
import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Constructor;

public enum PatternType {

    SINGLE(SinglePattern.class),
    LINEAR(LinearPattern.class),
    LINEAR_IGNORE_SPACES(LinearIgnoreSpacesPattern.class),
    RANDOM(RandomPattern.class),
    GRADIENT(GradientPattern.class),
    GRADIENT_RANDOM(RandomGradientPattern.class);

    private Class clazz;
    PatternType(Class<? extends BasePattern> clazz){
        this.clazz = clazz;
    }

    public <T extends BasePattern > T buildPattern(String name, String category, String permission, TextFormatOptions formatOptions, ChatColor... colors) {

        try {
            Constructor<T>[] constructors = clazz.getDeclaredConstructors();
            Constructor<T> constructor = constructors[0];
            T pattern = constructor.newInstance(name, permission, formatOptions, colors);
            pattern.setCategory(category);
            return pattern;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
