package net.busybee.chatcolor.pattern;

import net.busybee.chatcolor.pattern.api.BasePattern;
import net.busybee.chatcolor.pattern.format.TextFormatOptions;
import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.List;

public class SineWavePattern extends BasePattern {

    private final double frequency;
    private final double phase;

    public SineWavePattern(String name, String permission, TextFormatOptions formatOptions, ChatColor... colors) {
        this(name, "Regular", permission, formatOptions, 0.5, 0.0, colors);
    }

    public SineWavePattern(String name, String category, String permission, TextFormatOptions formatOptions, double frequency, double phase, ChatColor... colors) {
        super(name, category, permission, formatOptions, colors);
        this.frequency = frequency;
        this.phase = phase;
    }

    @Override
    public String getText(String text) {
        if (text == null || text.isEmpty()) return text;
        List<ChatColor> colors = getColors();
        if (colors.size() < 2) {
            String color = colors.isEmpty() ? ChatColor.WHITE.toString() : colors.get(0).toString();
            return color + getTextFormatOptions().setFormat(text);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            double value = (Math.sin(i * frequency + phase) + 1) / 2;

            Color c1 = colors.get(0).getColor();
            Color c2 = colors.get(1).getColor();
            
            int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * value);
            int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * value);
            int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * value);
            
            sb.append(ChatColor.of(new Color(r, g, b)));
            sb.append(getTextFormatOptions().setFormat(String.valueOf(text.charAt(i))));
        }
        return sb.toString();
    }
}
