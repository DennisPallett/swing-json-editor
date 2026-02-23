package nl.pallett.jsoneditor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.util.Duration;
import nl.pallett.jsoneditor.util.HashUtil;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EditorDocument {

    private @Nullable Path path;
    private final CodeArea codeArea;
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    private final PauseTransition debounce = new PauseTransition(Duration.millis(400));

    private final TreeView<JsonTreeNode> jsonTree;

    private final ObjectMapper objectMapper;

    private long dirtyChecksum;
    private Timeline scrollAnimation;

    private int lastFlashedParagraph = -1;
    private Timeline flashTimeline;

    private VirtualizedScrollPane<CodeArea> scrollPane;

    public EditorDocument(@Nullable Path path, String content) {
        this.path = path;
        JsonCodeEditor editor = new JsonCodeEditor();

        objectMapper = new ObjectMapper();

        this.codeArea = editor.getCodeArea();

        jsonTree = new TreeView<>();
        jsonTree.setShowRoot(false);
        jsonTree.setCellFactory(tv -> new JsonTreeCell());

        jsonTree.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        scrollToJsonPath(newItem.getValue());
                    }
                });

        debounce.setOnFinished(e -> handleJsonRefresh());

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.textProperty().addListener((obs, oldVal, newVal) -> debounce.playFromStart());

        init(content);
    }

    private void init(String content) {
        codeArea.replaceText(content);
        refreshJsonTree();
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

    public void setScrollPane(VirtualizedScrollPane<CodeArea> scrollPane) {
        this.scrollPane = scrollPane;
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

    private void scrollToJsonPath(JsonTreeNode node) {

        String[] parts = node.getPath().split("\\.");

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
        //flashCurrentLine();
    }

    public void setDirtyChecksum(String content) {
        dirtyChecksum = HashUtil.crc32(content);
        dirty.set(false);
    }

    public void setFile(Path file) {
        this.path = file;
    }

    private void handleJsonRefresh() {
        long newChecksum = HashUtil.crc32(codeArea.getText());
        dirty.set(dirtyChecksum != newChecksum);

        refreshJsonTree();
    }

    private void refreshJsonTree() {
        try {
            Set<String> expanded = new HashSet<>();

            if (jsonTree.getRoot() != null) {
                expanded = captureExpandedPaths(jsonTree.getRoot());
            }

            JsonNode node = objectMapper.readTree(codeArea.getText());

            TreeItem<JsonTreeNode> newRoot =
                    buildTree(node, "root", "");

            jsonTree.setRoot(newRoot);

            restoreExpandedPaths(newRoot, expanded);

        } catch (Exception ignored) {
            // invalid JSON
        }
    }

    private Set<String> captureExpandedPaths(TreeItem<JsonTreeNode> root) {
        Set<String> expanded = new HashSet<>();
        captureExpanded(root, expanded);
        return expanded;
    }

    private void captureExpanded(TreeItem<JsonTreeNode> item, Set<String> expanded) {
        if (item.isExpanded()) {
            expanded.add(item.getValue().getPath());
        }
        for (TreeItem<JsonTreeNode> child : item.getChildren()) {
            captureExpanded(child, expanded);
        }
    }

    private void restoreExpandedPaths(TreeItem<JsonTreeNode> root, Set<String> expanded) {
        restoreExpanded(root, expanded);
    }

    private void restoreExpanded(TreeItem<JsonTreeNode> item, Set<String> expanded) {
        if (expanded.contains(item.getValue().getPath())) {
            item.setExpanded(true);
        }
        for (TreeItem<JsonTreeNode> child : item.getChildren()) {
            restoreExpanded(child, expanded);
        }
    }

    private TreeItem<JsonTreeNode> buildTree(JsonNode node, String key, String path) {

        String currentPath = path.isEmpty() ? key : path + "." + key;

        JsonTreeNode.Type type;

        if (node.isObject()) {
            TreeItem<JsonTreeNode> item =
                    new TreeItem<>(new JsonTreeNode(key, null, JsonTreeNode.Type.OBJECT, currentPath));

            node.fieldNames().forEachRemaining(field ->
                    item.getChildren().add(
                            buildTree(node.get(field), field, currentPath)
                    )
            );
            return item;
        }

        if (node.isArray()) {
            TreeItem<JsonTreeNode> item =
                    new TreeItem<>(new JsonTreeNode(key, null, JsonTreeNode.Type.ARRAY, currentPath));

            for (int i = 0; i < node.size(); i++) {
                item.getChildren().add(
                        buildTree(node.get(i), "[" + i + "]", currentPath)
                );
            }
            return item;
        }

        // value node
        if (node.isTextual()) type = JsonTreeNode.Type.STRING;
        else if (node.isNumber()) type = JsonTreeNode.Type.NUMBER;
        else if (node.isBoolean()) type = JsonTreeNode.Type.BOOLEAN;
        else type = JsonTreeNode.Type.NULL;

        return new TreeItem<>(
                new JsonTreeNode(key, node.asText(), type, currentPath)
        );
    }

    public CodeArea getEditor() { return codeArea; }
    public BooleanProperty dirtyProperty() { return dirty; }
    public @Nullable Path getPath() { return path; }
    public TreeView<JsonTreeNode> getJsonTree() { return jsonTree; }
}
