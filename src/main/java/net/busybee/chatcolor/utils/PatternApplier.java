package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class PatternApplier {

    public static Component apply(String text, List<String> colors) {
        if (colors == null || colors.isEmpty() || text == null || text.isEmpty()) {
            return Component.text(text != null ? text : "");
        }

        net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
        int colorIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                builder.append(Component.space());
                continue;
            }
            String colorTag = colors.get(colorIndex % colors.size());
            builder.append(ColorUtil.colorize(colorTag + ColorUtil.escape(String.valueOf(c)) + "<reset>"));
            colorIndex++;
        }

        return builder.build();
    }

    public static Component applyToName(String text, List<String> colors) {
        return apply(text, colors);
    }
}
