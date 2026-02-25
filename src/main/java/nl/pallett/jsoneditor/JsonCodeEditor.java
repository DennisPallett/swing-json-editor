package nl.pallett.jsoneditor;

import javafx.concurrent.Task;
import javafx.scene.Node;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonCodeEditor {

    private final CodeArea codeArea = new CodeArea();

    // --- JSON Regex Pattern ---
    private static final String KEY_PATTERN =
            "\"([^\"\\\\]|\\\\.)*\"(?=\\s*:)";

    private static final String STRING_PATTERN =
            "\"([^\"\\\\]|\\\\.)*\"";

    private static final String NUMBER_PATTERN =
            "-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?";

    private static final String BOOLEAN_PATTERN =
            "\\b(true|false)\\b";

    private static final String NULL_PATTERN =
            "\\bnull\\b";

    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[\\]]";
    private static final String COLON_PATTERN = ":";
    private static final String COMMA_PATTERN = ",";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEY>" + KEY_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<BOOLEAN>" + BOOLEAN_PATTERN + ")"
                    + "|(?<NULL>" + NULL_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<COLON>" + COLON_PATTERN + ")"
                    + "|(?<COMMA>" + COMMA_PATTERN + ")"
    );

    public JsonCodeEditor() {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setWrapText(false);

        // Debounced highlighting (prevents CPU spike while typing)
        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(300))
                .subscribe(ignore -> computeHighlightingAsync());
    }

    public Node getNode() {
        return new VirtualizedScrollPane<>(codeArea);
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    // --- Async Highlighting ---
    public void computeHighlightingAsync() {
        String text = codeArea.getText();

        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return computeHighlighting(text);
            }
        };

        task.setOnSucceeded(e ->
                codeArea.setStyleSpans(0, task.getValue())
        );

        new Thread(task, "json-highlighter").start();
    }

    // --- Core Highlighting Logic ---
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {

        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;

        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();

        while (matcher.find()) {

            String styleClass =
                    matcher.group("KEY") != null ? "json-key" :
                            matcher.group("STRING") != null ? "json-string" :
                                    matcher.group("NUMBER") != null ? "json-number" :
                                            matcher.group("BOOLEAN") != null ? "json-boolean" :
                                                    matcher.group("NULL") != null ? "json-null" :
                                                            matcher.group("BRACE") != null ? "json-brace" :
                                                                    matcher.group("BRACKET") != null ? "json-bracket" :
                                                                            matcher.group("COLON") != null ? "json-colon" :
                                                                                    matcher.group("COMMA") != null ? "json-comma" :
                                                                                            null;
            spansBuilder.add(Collections.emptyList(),
                    matcher.start() - lastKwEnd);

            spansBuilder.add(Collections.singleton(styleClass),
                    matcher.end() - matcher.start());

            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(),
                text.length() - lastKwEnd);

        return spansBuilder.create();
    }

    // --- Error Highlighting (Optional Feature) ---
    public void highlightErrorLine(int line) {
        int paragraph = line - 1;
        if (paragraph >= 0 && paragraph < codeArea.getParagraphs().size()) {
            codeArea.setParagraphStyle(paragraph,
                    Collections.singleton("json-error-line"));
        }
    }
}
