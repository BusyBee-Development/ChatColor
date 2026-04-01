package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component colorize(String text) {
        if (text == null) return Component.empty();
        return MINI_MESSAGE.deserialize(text);
    }

    public static Component applyTagToText(String tag, String rawText) {
        if (tag == null || tag.isBlank() || rawText == null) return Component.text(rawText != null ? rawText : "");
        String closing = getClosingTag(tag);
        String formatted = tag + escape(rawText) + closing;
        try {
            return MINI_MESSAGE.deserialize(formatted);
        } catch (Exception e) {
            return Component.text(rawText);
        }
    }

    public static String getClosingTag(String tag) {
        if (tag == null || tag.isBlank()) return "<reset>";
        String inner = tag.trim();
        if (!inner.startsWith("<") || !inner.endsWith(">")) return "<reset>";
        
        String content = inner.substring(1, inner.length() - 1);
        if (content.startsWith("#")) return "</color>";
        if (content.startsWith("gradient")) return "</gradient>";
        if (content.startsWith("rainbow")) return "</rainbow>";
        
        int colonIndex = content.indexOf(':');
        String tagName = colonIndex > 0 ? content.substring(0, colonIndex) : content;
        
        if (tagName.isEmpty()) return "<reset>";
        return "</" + tagName + ">";
    }

    public static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("<", "\\<");
    }

    public static MiniMessage getMiniMessage() {
        return MINI_MESSAGE;
    }
}
