package net.busybee.chatcolor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Colours a chat component one character at a time while leaving chosen tokens as a single,
 * uncoloured run of text.
 *
 * <p>Chat plugins like ZelChat and InteractiveChat swap tokens such as {@code [item]} or a mentioned player's name
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
        return apply(message, tokens, List.of(), styler);
    }

    /**
     * Same as {@link #apply(Component, Collection, Function)}, also keeping whole every match of
     * {@code patterns}. For plugins like InteractiveChat whose keywords are regexes.
     */
    public static Component apply(Component message, Collection<String> tokens, Collection<Pattern> patterns,
                                  Function<String, Component> styler) {
        Pattern pattern = buildPattern(tokens, patterns);

        // Walk the message exactly as the colouring pass will, so offsets line up with it.
        List<String> pieces = new ArrayList<>();
        List<Boolean> isToken = new ArrayList<>();
        message.replaceText(TextReplacementConfig.builder()
                .match(pattern)
                .replacement((match, builder) -> {
                    pieces.add(match.group());
                    isToken.add(match.group(KEEP) != null);
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
            styledPiece[i] = !isToken.get(i);
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

    private static final String KEEP = "keep";

    static Pattern buildPattern(Collection<String> tokens) {
        return buildPattern(tokens, List.of());
    }

    /**
     * Matches one kept token (group {@value #KEEP}) or else one character. Literal tokens go
     * longest first, so "[inventory]" wins over "[inv]"; regexes follow, each in its own group so
     * inline flags like {@code (?i)} stay inside it. A regex that doesn't compile on its own or
     * that can match empty text is skipped rather than breaking chat.
     */
    static Pattern buildPattern(Collection<String> tokens, Collection<Pattern> patterns) {
        List<String> alternatives = new ArrayList<>();
        tokens.stream()
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(Pattern::quote)
                .forEach(alternatives::add);
        for (Pattern p : patterns) {
            if (p == null || p.pattern().isEmpty()) continue;
            String wrapped = "(?:" + p.pattern() + ")";
            try {
                if (Pattern.compile(wrapped, p.flags()).matcher("").matches()) continue;
            } catch (PatternSyntaxException e) {
                continue;
            }
            alternatives.add(p.flags() == 0 ? wrapped : "(?" + inlineFlags(p.flags()) + ":" + p.pattern() + ")");
        }
        String body = alternatives.isEmpty() ? "" : "(?<" + KEEP + ">" + String.join("|", alternatives) + ")|";
        try {
            return Pattern.compile(body + ".", Pattern.DOTALL);
        } catch (PatternSyntaxException e) {
            // Regexes that are fine alone can still clash once joined (e.g. back-references).
            return patterns.isEmpty() ? Pattern.compile(".", Pattern.DOTALL) : buildPattern(tokens, List.of());
        }
    }

    private static String inlineFlags(int flags) {
        StringBuilder sb = new StringBuilder();
        if ((flags & Pattern.CASE_INSENSITIVE) != 0) sb.append('i');
        if ((flags & Pattern.UNICODE_CASE) != 0) sb.append('u');
        if ((flags & Pattern.MULTILINE) != 0) sb.append('m');
        if ((flags & Pattern.DOTALL) != 0) sb.append('s');
        if ((flags & Pattern.COMMENTS) != 0) sb.append('x');
        return sb.toString();
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
