package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Colours a chat component one character at a time while leaving chosen tokens as a single,
 * uncoloured run of text.
 *
 * <p>Chat plugins like ZelChat swap tokens such as {@code [item]} or a mentioned player's name
 * for interactive components with Adventure's {@code replaceText}. That only matches inside one
 * text component, so a gradient or pattern that splits {@code [item]} into six coloured letters
 * hides it from them. Keeping each token whole lets them still find it after we colour.
 */
public final class TokenSafeColorizer {

    private TokenSafeColorizer() {
    }

    /**
     * @param message the component to colour
     * @param tokens  text to keep whole and uncoloured; blanks are ignored
     * @param styler  colours a plain string, e.g. by applying a gradient tag to it. It must keep
     *                the text unchanged, only styled.
     * @return the coloured component, or null when {@code styler} changed the text so the
     *         colours cannot be lined up with the message
     */
    public static Component apply(Component message, Collection<String> tokens, Function<String, Component> styler) {
        Pattern pattern = buildPattern(tokens);
        Set<String> tokenSet = tokens.stream().filter(t -> t != null && !t.isBlank()).collect(Collectors.toSet());

        // Walk the message exactly as the colouring pass will, so offsets line up with it.
        List<String> pieces = new ArrayList<>();
        message.replaceText(TextReplacementConfig.builder()
                .match(pattern)
                .replacement((match, builder) -> {
                    pieces.add(match.group());
                    return builder;
                })
                .build());
        if (pieces.isEmpty()) {
            return message;
        }

        // Tokens are left out of what gets styled, so a gradient or pattern runs across the text
        // around them as if they weren't there. Blanks at either end of what's left are skipped
        // too: in "hi [item]" the space before the token would otherwise take the gradient's end
        // color, where nobody can see it.
        boolean[] styledPiece = new boolean[pieces.size()];
        for (int i = 0; i < pieces.size(); i++) {
            styledPiece[i] = !tokenSet.contains(pieces.get(i));
        }
        for (int i = 0; i < pieces.size() && (!styledPiece[i] || pieces.get(i).isBlank()); i++) {
            styledPiece[i] = false;
        }
        for (int i = pieces.size() - 1; i >= 0 && (!styledPiece[i] || pieces.get(i).isBlank()); i--) {
            styledPiece[i] = false;
        }
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < pieces.size(); i++) {
            if (styledPiece[i]) text.append(pieces.get(i));
        }
        if (text.isEmpty()) {
            return message;
        }
        String plain = text.toString();
        Component styled = styler.apply(plain);
        if (styled == null) {
            return message;
        }
        List<Style> styles = new ArrayList<>(plain.length());
        flatten(styled, Style.empty(), styles);
        if (styles.size() != plain.length()) {
            return null;
        }

        int[] offset = {0};
        int[] index = {0};
        return message.replaceText(TextReplacementConfig.builder()
                .match(pattern)
                .replacement((match, builder) -> {
                    String piece = match.group();
                    if (!styledPiece[index[0]++]) {
                        return Component.text(piece);
                    }
                    int at = offset[0];
                    offset[0] += piece.length();
                    return Component.text(piece, styles.get(at));
                })
                .build());
    }

    /** Longest token first, so "[inventory]" wins over "[inv]"; otherwise one character. */
    static Pattern buildPattern(Collection<String> tokens) {
        String alternatives = tokens.stream()
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        return Pattern.compile(alternatives.isEmpty() ? "." : alternatives + "|.", Pattern.DOTALL);
    }

    /** One style per UTF-16 char of the component's text, with parent styles merged in. */
    private static void flatten(Component component, Style inherited, List<Style> out) {
        Style style = inherited.merge(component.style());
        if (component instanceof TextComponent text) {
            String content = text.content();
            for (int i = 0; i < content.length(); i++) {
                out.add(style);
            }
        }
        for (Component child : component.children()) {
            flatten(child, style, out);
        }
    }
}
