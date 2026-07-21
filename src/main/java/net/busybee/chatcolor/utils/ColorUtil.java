package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static Component colorize(String text) {
        if (text == null) return Component.empty();
        return MINI_MESSAGE.deserialize(text);
    }

    public static Component applyTagToText(String tag, String rawText) {
        return applyTagToText(tag, rawText, true);
    }

    public static Component applyTagToText(String tag, String rawText, boolean escape) {
        if (tag == null || tag.isBlank() || rawText == null) return Component.text(rawText != null ? rawText : "");
        
        String processedTag = tag;
        if (!tag.startsWith("<") || !tag.endsWith(">")) {
            processedTag = translateLegacyToMiniMessage(tag);
            if (!processedTag.startsWith("<")) {
                processedTag = "<" + processedTag + ">";
            }
        }

        String processedText = translateLegacyToMiniMessage(rawText);
        String formatted = processedTag + (escape ? escape(processedText) : processedText);
        
        try {
            return MINI_MESSAGE.deserialize(formatted);
        } catch (Exception e) {
            return Component.text(rawText);
        }
    }

    public static String translateLegacyToMiniMessage(String text) {
        if (text == null) return "";
        String result = text;

        // Only translate hex if it has a prefix and isn't already in a tag
        result = result.replaceAll("(?i)[&§]#([A-Fa-f0-9]{6})", "<#$1>");
        result = result.replaceAll("(?i)§x§([A-Fa-f0-9])§([A-Fa-f0-9])§([A-Fa-f0-9])§([A-Fa-f0-9])§([A-Fa-f0-9])§([A-Fa-f0-9])", "<#$1$2$3$4$5$6>");

        String[][] colors = {
                {"0", "black"}, {"1", "dark_blue"}, {"2", "dark_green"}, {"3", "dark_aqua"},
                {"4", "dark_red"}, {"5", "dark_purple"}, {"6", "gold"}, {"7", "gray"},
                {"8", "dark_gray"}, {"9", "blue"}, {"a", "green"}, {"b", "aqua"},
                {"c", "red"}, {"d", "light_purple"}, {"e", "yellow"}, {"f", "white"},
                {"l", "bold"}, {"m", "strikethrough"}, {"n", "underlined"}, {"o", "italic"}, {"r", "reset"}
        };

        for (String[] color : colors) {
            result = result.replaceAll("(?i)[&§]" + color[0], "<" + color[1] + ">");
        }

        return result;
    }

    public static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("<", "\\<");
    }

    public static Component escapeTags(Component component) {
        if (component == null) return Component.empty();
        return component.replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                .matchLiteral("\\")
                .replacement("\\\\")
                .build())
                .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                .matchLiteral("<")
                .replacement("\\<")
                .build());
    }

    public static Component applyTagToComponent(String tag, Component component) {
        if (tag == null || tag.isBlank() || component == null) return component;
        
        String processedTag = tag;
        if (!tag.startsWith("<") || !tag.endsWith(">")) {
            processedTag = translateLegacyToMiniMessage(tag);
            if (!processedTag.startsWith("<")) {
                processedTag = "<" + processedTag + ">";
            }
        }

        String mm = toMiniMessage(component);
        try {
            return MINI_MESSAGE.deserialize(processedTag + mm);
        } catch (Exception e) {
            return component;
        }
    }

    public static String stripLegacy(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)§[0-9A-FK-ORX]", "").replaceAll("(?i)§x(§[0-9A-F]){6}", "");
    }

    public static String stripAll(String text) {
        if (text == null) return "";
        String stripped = stripLegacy(text);
        return stripped.replaceAll("<[^>]*>", "");
    }

    public static MiniMessage getMiniMessage() {
        return MINI_MESSAGE;
    }
    public static String toMiniMessage(Component component) {
        return MINI_MESSAGE.serialize(component);
    }
    public static String toLegacy(Component component) {
        if (component == null) return "";
        return LEGACY.serialize(component);
    }
    public static LegacyComponentSerializer getLegacySerializer() {
        return LEGACY;
    }
}
