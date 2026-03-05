package nl.pallett.jsoneditor.editor.document.code;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nl.pallett.jsoneditor.editor.EditorMode;
import nl.pallett.jsoneditor.util.StringUtil;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

public class JsonCodeEditor {
    private static final String YAML_INDENT = "  "; // 2 spaces (YAML standard)

    private final CodeArea codeArea = new CodeArea();

    private final ObjectProperty<EditorMode> editorMode = new SimpleObjectProperty<>();

    public JsonCodeEditor() {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setWrapText(false);

        codeArea.setOnKeyPressed(this::handleKeyPressedEvent);

        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (getEditorMode() == EditorMode.YAML && e.getCode() == KeyCode.TAB) {
                e.consume();
                codeArea.insertText(codeArea.getCaretPosition(), YAML_INDENT);
            }
        });

        // Debounced highlighting (prevents CPU spike while typing)
        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(300))
                .subscribe(ignore -> computeHighlightingAsync());
    }

    private void handleKeyPressedEvent(KeyEvent event) {
        if (event.isMetaDown()) {
            switch (event.getCode()) {
                case Z:
                    if (event.isShiftDown()) {
                        codeArea.redo();   // Ctrl + Shift + Z
                    } else {
                        codeArea.undo();   // Ctrl + Z
                    }
                    event.consume();
                    break;

                case Y:
                    codeArea.redo();       // Ctrl + Y
                    event.consume();
                    break;
            }
        }

        if (getEditorMode() == EditorMode.YAML) {
            handleKeyPressedEventYaml(event);
        }
    }

    private void handleKeyPressedEventYaml(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            int caretPos = codeArea.getCaretPosition();
            int currentParagraph = codeArea.getCurrentParagraph();

            if (currentParagraph < 0) return;

            String previousLine = codeArea.getParagraph(currentParagraph-1).getText();

            String baseIndent = StringUtil.getLeadingWhitespace(previousLine);
            String newIndent = baseIndent;

            // Rule 1: Increase indent after colon
            if (previousLine.trim().endsWith(":")) {
                newIndent += YAML_INDENT;
            }

            // Rule 2: Continue list items
            else if (previousLine.trim().startsWith("- ")) {
                newIndent = baseIndent + "- ";
            }

            final String finalNewIndent = newIndent;

            if (previousLine.trim().equals("-")) {
                Platform.runLater(() -> {
                    codeArea.replaceText(
                            codeArea.getCaretPosition() - 2,
                            codeArea.getCaretPosition(),
                            ""
                    );
                });
            } else {
                // Delay insertion until after newline is inserted
                Platform.runLater(() -> {
                    codeArea.insertText(codeArea.getCaretPosition(), finalNewIndent);
                });
            }
        }

        if (event.getCode() == KeyCode.TAB) {
            event.consume();
            codeArea.insertText(codeArea.getCaretPosition(), YAML_INDENT);
        }
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

        Task<StyleSpans<Collection<String>>> task;

        if (getEditorMode() == EditorMode.JSON) {
            task = new Task<>() {
                @Override
                protected StyleSpans<Collection<String>> call() {
                    return JsonHighlighter.computeHighlighting(text);
                }
            };
        } else {
            task = new Task<>() {
                @Override
                protected StyleSpans<Collection<String>> call() {
                    return YamlHighlighter.computeHighlighting(text);
                }
            };
        }

        task.setOnSucceeded(e ->
                codeArea.setStyleSpans(0, task.getValue())
        );

        new Thread(task, "code-highlighter").start();
    }

    // --- Error Highlighting (Optional Feature) ---
    public void highlightErrorLine(int line) {
        int paragraph = line - 1;
        if (paragraph >= 0 && paragraph < codeArea.getParagraphs().size()) {
            codeArea.setParagraphStyle(paragraph,
                    Collections.singleton("json-error-line"));
        }
    }

    public ObjectProperty<EditorMode> getEditorModeProperty() { return editorMode; }
    public EditorMode getEditorMode() { return editorMode.get(); }
}
