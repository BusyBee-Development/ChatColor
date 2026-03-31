package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class PatternApplier {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component apply(String text, List<String> colors) {
        if (colors == null || colors.isEmpty() || text == null || text.isEmpty()) {
            return Component.text(text != null ? text : "");
        }

        Component result = Component.empty();
        int colorIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result = result.append(Component.space());
                continue;
            }
            String colorTag = colors.get(colorIndex % colors.size());
            String safe = ColorUtil.escape(String.valueOf(c));
            result = result.append(MINI_MESSAGE.deserialize(colorTag + safe + "<reset>"));
            colorIndex++;
        }

        return result;
    }

    public static Component applyToName(String text, List<String> colors) {
        return apply(text, colors);
    }
}