package nl.pallett.jsoneditor.editor.document.code;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlHighlighter {

    private static final String KEY_PATTERN = "(?m)^(\\s*[^:#\\n]+)(?=:)";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";
    private static final String BOOLEAN_PATTERN = "\\b(true|false|yes|no|null)\\b";
    private static final String COMMENT_PATTERN = "#[^\\n]*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEY>" + KEY_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<BOOLEAN>" + BOOLEAN_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {

        Matcher matcher = PATTERN.matcher(text);
        int lastEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEY") != null ? "yaml-key" :
                            matcher.group("STRING") != null ? "yaml-string" :
                                    matcher.group("NUMBER") != null ? "yaml-number" :
                                            matcher.group("BOOLEAN") != null ? "yaml-boolean" :
                                                    matcher.group("COMMENT") != null ? "yaml-comment" :
                                                            null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
        return spansBuilder.create();
    }
}