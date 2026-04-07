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
        if (tag == null || tag.isBlank() || rawText == null) return Component.text(rawText != null ? rawText : "");
        String formatted = tag + escape(rawText);
        try {
            return MINI_MESSAGE.deserialize(formatted);
        } catch (Exception e) {
            return Component.text(rawText);
        }
    }


    public static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("<", "\\<");
    }

    public static String stripLegacy(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)§[0-9A-FK-ORX]", "");
    }

    public static MiniMessage getMiniMessage() {
        return MINI_MESSAGE;
    }

    public static LegacyComponentSerializer getLegacySerializer() {
        return LEGACY;
    }
}
