package net.busybee.chatcolor.utils;

import net.busybee.chatcolor.models.PatternEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Colors text one letter at a time from a pattern. Spaces never take a color.
 *
 * <p>The random modes are driven by a seed, so the same seed always gives the same colors. Chat
 * passes one seed per message, which keeps every viewer, the renderer and %chatcolor_message%
 * in agreement; names pass a fixed seed per player so they don't change on every refresh.
 */
public class PatternApplier {

    private static final String SPACE_OR_CHAR = ".|\\s";

    // ------------------------------------------------------------------ cycle (list only)

    public static Component apply(String text, List<String> colors) {
        return apply(text, colors, true);
    }

    public static Component apply(String text, List<String> colors, boolean escape) {
        if (colors == null || colors.isEmpty() || text == null || text.isEmpty()) {
            return Component.text(text != null ? text : "");
        }
        return applyTags(text, cycle(colors, countLetters(text)), escape);
    }

    public static Component apply(Component component, List<String> colors) {
        if (colors == null || colors.isEmpty() || component == null) return component;
        return applyTags(component, cycle(colors, countLetters(component)));
    }

    public static Component applyToName(String text, List<String> colors) {
        return apply(text, colors);
    }

    // ------------------------------------------------------------------ any mode

    public static Component apply(String text, PatternEntry pattern, long seed) {
        return apply(text, pattern, seed, true);
    }

    public static Component apply(String text, PatternEntry pattern, long seed, boolean escape) {
        if (pattern == null || pattern.getColors() == null || pattern.getColors().isEmpty()
                || text == null || text.isEmpty()) {
            return Component.text(text != null ? text : "");
        }
        return applyTags(text, tagsFor(pattern, countLetters(text), seed), escape);
    }

    public static Component apply(Component component, PatternEntry pattern, long seed) {
        if (pattern == null || pattern.getColors() == null || pattern.getColors().isEmpty() || component == null) {
            return component;
        }
        return applyTags(component, tagsFor(pattern, countLetters(component), seed));
    }

    /** The color tag for each of {@code letters} letters, in order. */
    public static List<String> tagsFor(PatternEntry pattern, int letters, long seed) {
        List<String> colors = pattern.getColors();
        return switch (pattern.getMode()) {
            case RANDOM -> random(colors, letters, seed);
            case RANDOM_GRADIENT -> randomGradient(colors, letters, seed);
            default -> cycle(colors, letters);
        };
    }

    private static List<String> cycle(List<String> colors, int letters) {
        List<String> tags = new ArrayList<>(letters);
        for (int i = 0; i < letters; i++) {
            tags.add(colors.get(i % colors.size()));
        }
        return tags;
    }

    /** A random color per letter, never the same as the letter before when there's a choice. */
    private static List<String> random(List<String> colors, int letters, long seed) {
        SplittableRandom rng = new SplittableRandom(seed);
        List<String> tags = new ArrayList<>(letters);
        int last = -1;
        for (int i = 0; i < letters; i++) {
            int pick = rng.nextInt(colors.size());
            if (pick == last && colors.size() > 1) {
                pick = (pick + 1 + rng.nextInt(colors.size() - 1)) % colors.size();
            }
            tags.add(colors.get(pick));
            last = pick;
        }
        return tags;
    }

    /**
     * A gradient between two different colors picked from the list. Entries that aren't a plain
     * color (only a decoration, say) can't be blended and are left out; with fewer than two usable
     * colors this falls back to cycling.
     */
    private static List<String> randomGradient(List<String> colors, int letters, long seed) {
        List<TextColor> usable = new ArrayList<>();
        for (String tag : colors) {
            TextColor color = parseColor(tag);
            if (color != null && !usable.contains(color)) usable.add(color);
        }
        if (usable.size() < 2) {
            return cycle(colors, letters);
        }

        SplittableRandom rng = new SplittableRandom(seed);
        int from = rng.nextInt(usable.size());
        int to = (from + 1 + rng.nextInt(usable.size() - 1)) % usable.size();
        TextColor start = usable.get(from);
        TextColor end = usable.get(to);

        List<String> tags = new ArrayList<>(letters);
        for (int i = 0; i < letters; i++) {
            float t = letters <= 1 ? 0f : (float) i / (letters - 1);
            tags.add("<" + TextColor.lerp(t, start, end).asHexString() + ">");
        }
        return tags;
    }

    static TextColor parseColor(String tag) {
        try {
            Component probe = ColorUtil.getMiniMessage().deserialize(ColorUtil.normalizeTag(tag) + "x");
            if (probe.color() != null) return probe.color();
            for (Component child : probe.children()) {
                if (child.color() != null) return child.color();
            }
        } catch (Exception ignored) {
            // not a color tag
        }
        return null;
    }

    // ------------------------------------------------------------------ painting

    private static int countLetters(String text) {
        int letters = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != ' ') letters++;
        }
        return letters;
    }

    /** Counts what {@link #applyTags(Component, List)} will color, walking the same matches. */
    private static int countLetters(Component component) {
        AtomicInteger letters = new AtomicInteger();
        component.replaceText(TextReplacementConfig.builder()
                .match(SPACE_OR_CHAR)
                .replacement((match, builder) -> {
                    if (!match.group().equals(" ")) letters.incrementAndGet();
                    return builder;
                })
                .build());
        return letters.get();
    }

    private static Component applyTags(String text, List<String> tags, boolean escape) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                sb.append(' ');
                continue;
            }
            sb.append(tags.get(index++)).append(escape ? ColorUtil.escape(String.valueOf(c)) : String.valueOf(c));
        }
        return ColorUtil.colorize(sb.toString());
    }

    private static Component applyTags(Component component, List<String> tags) {
        AtomicInteger index = new AtomicInteger();
        return component.replaceText(TextReplacementConfig.builder()
                .match(SPACE_OR_CHAR)
                .replacement((match, builder) -> {
                    String c = match.group();
                    if (c.equals(" ")) return Component.text(" ");
                    int at = index.getAndIncrement();
                    return at < tags.size() ? ColorUtil.applyTagToText(tags.get(at), c) : Component.text(c);
                })
                .build());
    }
}
