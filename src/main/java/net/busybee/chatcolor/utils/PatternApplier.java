package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;

import java.util.List;

public class PatternApplier {

    public static Component apply(String text, List<String> colors) {
        return apply(text, colors, true);
    }

    public static Component apply(String text, List<String> colors, boolean escape) {
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
            sb.append(colorTag).append(escape ? ColorUtil.escape(String.valueOf(c)) : String.valueOf(c));
            colorIndex++;
        }

        return ColorUtil.colorize(sb.toString());
    }

    public static Component apply(Component component, List<String> colors) {
        if (colors == null || colors.isEmpty() || component == null) return component;

        java.util.concurrent.atomic.AtomicInteger colorIndex = new java.util.concurrent.atomic.AtomicInteger(0);
        return component.replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                .match(".|\\s")
                .replacement((matchResult, builder) -> {
                    String c = matchResult.group();
                    if (c.equals(" ")) return Component.text(" ");
                    String colorTag = colors.get(colorIndex.getAndIncrement() % colors.size());
                    return ColorUtil.applyTagToText(colorTag, c);
                })
                .build());
    }

    public static Component applyToName(String text, List<String> colors) {
        return apply(text, colors);
    }
}
