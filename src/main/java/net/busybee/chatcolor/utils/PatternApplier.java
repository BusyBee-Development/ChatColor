package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class PatternApplier {

    public static Component apply(String text, List<String> colors) {
        if (colors == null || colors.isEmpty() || text == null || text.isEmpty()) {
            return Component.text(text != null ? text : "");
        }

        StringBuilder sb = new StringBuilder();
        int colorIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                sb.append(" ");
                continue;
            }
            String colorTag = colors.get(colorIndex % colors.size());
            sb.append(colorTag).append(ColorUtil.escape(String.valueOf(c))).append("<reset>");
            colorIndex++;
        }

        return ColorUtil.colorize(sb.toString());
    }

    public static Component applyToName(String text, List<String> colors) {
        return apply(text, colors);
    }
}
