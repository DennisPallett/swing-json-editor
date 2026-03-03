package nl.pallett.jsoneditor.editor.document;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import nl.pallett.jsoneditor.editor.EditorMode;
import nl.pallett.jsoneditor.editor.document.code.JsonCodeEditor;
import nl.pallett.jsoneditor.editor.document.tree.JsonTreeNode;
import nl.pallett.jsoneditor.editor.document.tree.JsonTreeView;
import nl.pallett.jsoneditor.util.FileUtil;
import nl.pallett.jsoneditor.util.HashUtil;
import nl.pallett.jsoneditor.util.ObjectMapperUtil;
import nl.pallett.jsoneditor.util.StringUtil;
import org.controlsfx.control.StatusBar;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static nl.pallett.jsoneditor.SwingJsonEditorApp.showError;

public class EditorDocument {

    private @Nullable Path path;
    private final JsonCodeEditor editor;
    private final CodeArea codeArea;
    private final StatusBar statusBar;
    private final BorderPane containerPane;

    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    private final PauseTransition debounce = new PauseTransition(Duration.millis(400));

    private final ObjectProperty<EditorMode> currentMode = new SimpleObjectProperty<>();

    private final JsonTreeView jsonTree;

    private long dirtyChecksum;
    private Timeline scrollAnimation;

    private int lastFlashedParagraph = -1;
    private Timeline flashTimeline;

    public EditorDocument(@Nullable Path path, String content) {
        this.path = path;
        editor = new JsonCodeEditor();

        this.codeArea = editor.getCodeArea();
        this.containerPane = new BorderPane();

        this.statusBar = new StatusBar();
        statusBar.setText("Ready");

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);

        EditorMode initialMode = FileUtil.isYamlFile(path) ? EditorMode.YAML : EditorMode.JSON;
        EditorToolbar editorToolbar = new EditorToolbar(initialMode);
        this.currentMode.bind(editorToolbar.currentMode());
        this.editor.getEditorModeProperty().bind(editorToolbar.currentMode());

        containerPane.setTop(editorToolbar);
        containerPane.setCenter(scrollPane);
        containerPane.setBottom(statusBar);

        jsonTree = new JsonTreeView(this);

        debounce.setOnFinished(e -> handleContentRefresh());

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.textProperty().addListener((obs, oldVal, newVal) -> debounce.playFromStart());

        init(content);
    }

    private void init(String content) {
        codeArea.replaceText(content);
        jsonTree.refreshJsonTree(content);
        setDirtyChecksum(content);
    }

    private void flashCurrentLine() {
        int paragraph = codeArea.getCurrentParagraph();

        // Clear previous flash immediately
        if (lastFlashedParagraph >= 0) {
            codeArea.setParagraphStyle(lastFlashedParagraph, Collections.emptyList());
        }

        // Apply flash style to current paragraph
        codeArea.setParagraphStyle(paragraph, Collections.singleton("flash-line"));
        lastFlashedParagraph = paragraph;

        // Cancel previous timeline
        if (flashTimeline != null) {
            flashTimeline.stop();
        }

        // Remove the flash after 300ms
        flashTimeline = new Timeline(
                new KeyFrame(Duration.millis(300),
                        e -> {
                            codeArea.setParagraphStyle(paragraph, Collections.emptyList());
                            lastFlashedParagraph = -1;
                        })
        );
        flashTimeline.play();
    }

    private void smoothScrollToOffset(int targetOffset) {

        if (scrollAnimation != null) {
            scrollAnimation.stop();
        }

        int startOffset = codeArea.getCaretPosition();
        int distance = targetOffset - startOffset;

        int steps = 15;          // smoothness
        int durationMs = 250;    // total animation time

        scrollAnimation = new Timeline();

        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            double eased = t * t * (3 - 2 * t); // smoothstep easing
            int stepOffset = startOffset + (int)(distance * eased);

            scrollAnimation.getKeyFrames().add(
                    new KeyFrame(
                            Duration.millis(i * (durationMs / (double) steps)),
                            e -> {
                                codeArea.moveTo(stepOffset);
                                codeArea.requestFollowCaret();
                            }
                    )
            );
        }

        scrollAnimation.setOnFinished(e -> flashCurrentLine());

        scrollAnimation.play();
    }

    public void scrollToJsonPath(JsonTreeNode node) {
        List<String> parts = node.getPath().toList();

        String text = codeArea.getText();
        int searchStart = 0;

        for (String part : parts) {

            if (part.equals("root")) continue;
            if (part.startsWith("[")) continue;

            String search = "\"" + part + "\"";

            int index = text.indexOf(search, searchStart);
            if (index < 0) return;

            searchStart = index + search.length();
        }

        smoothScrollToOffset(searchStart);
    }

    public void setDirtyChecksum(String content) {
        dirtyChecksum = HashUtil.crc32(content);
        dirty.set(false);
    }

    public void setFile(Path file) {
        this.path = file;
    }

    private void handleContentRefresh() {
        long newChecksum = HashUtil.crc32(codeArea.getText());
        dirty.set(dirtyChecksum != newChecksum);

        jsonTree.refreshJsonTree(codeArea.getText());

        validateContent();
    }

    public void formatContent() {
        try {
            String formatted;
            if (getEditorMode() == EditorMode.JSON) {
                ObjectMapper objectMapper = ObjectMapperUtil.getInstance(getEditorMode());
                Object object = objectMapper.readValue(codeArea.getText(), Object.class);
                formatted = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(object);
            } else {
                formatted = StringUtil.formatYaml(codeArea.getText());
            }
            codeArea.replaceText(formatted);
            editor.computeHighlightingAsync();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void validateContent() {
        try {
            ObjectMapperUtil.getInstance(getEditorMode()).readTree(codeArea.getText());
            statusBar.setText("✅ Valid " + getEditorMode());
            statusBar.setTooltip(null);
        } catch (JsonProcessingException ex) {
            String message = "❌ Invalid " + getEditorMode();
            JsonLocation errorLocation = ex.getLocation();
            if (errorLocation != null) {
                message += " at line " + errorLocation.getLineNr()
                        + ", column " + errorLocation.getColumnNr();
            }

            statusBar.setText(message);
            statusBar.setTooltip(new Tooltip(ex.getMessage()));
        }
    }



    public CodeArea getEditor() { return codeArea; }
    public BooleanProperty dirtyProperty() { return dirty; }
    public @Nullable Path getPath() { return path; }
    public JsonTreeView getJsonTree() { return jsonTree; }
    public String getJson() {
        return codeArea.getText();
    }

    public Pane getContainer() {
        return containerPane;
    }
    public EditorMode getEditorMode() { return currentMode.get(); }
}
