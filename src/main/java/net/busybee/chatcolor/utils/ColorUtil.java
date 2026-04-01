package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.defaults())
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .build())
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

    public static MiniMessage getMiniMessage() {
        return MINI_MESSAGE;
    }
}
