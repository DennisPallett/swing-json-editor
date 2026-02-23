package nl.pallett.jsoneditor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Duration;
import nl.pallett.jsoneditor.util.HashUtil;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class EditorDocument {

    private @Nullable Path path;
    private final CodeArea codeArea;
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    private final PauseTransition debounce = new PauseTransition(Duration.millis(400));

    private final TreeView<JsonTreeNode> jsonTree;

    private final ObjectMapper objectMapper;

    private long dirtyChecksum;

    public EditorDocument(@Nullable Path path, String content) {
        this.path = path;
        JsonCodeEditor editor = new JsonCodeEditor();

        objectMapper = new ObjectMapper();

        this.codeArea = editor.getCodeArea();

        jsonTree = new TreeView<>();
        jsonTree.setShowRoot(false);
        jsonTree.setCellFactory(tv -> new JsonTreeCell());

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
            JsonNode node = objectMapper.readTree(codeArea.getText());
            jsonTree.setRoot(buildTree(node, "root"));
        } catch (Exception ignored) {
            // Invalid JSON — don't update tree
        }
    }

    private TreeItem<JsonTreeNode> buildTree(JsonNode node, String key) {

        if (node.isObject()) {
            TreeItem<JsonTreeNode> item =
                    new TreeItem<>(new JsonTreeNode(key, null, JsonTreeNode.Type.OBJECT));

            node.fieldNames().forEachRemaining(field ->
                    item.getChildren().add(
                            buildTree(node.get(field), field)
                    )
            );
            return item;
        }

        if (node.isArray()) {
            TreeItem<JsonTreeNode> item =
                    new TreeItem<>(new JsonTreeNode(key, null, JsonTreeNode.Type.ARRAY));

            for (int i = 0; i < node.size(); i++) {
                item.getChildren().add(
                        buildTree(node.get(i), "[" + i + "]")
                );
            }
            return item;
        }

        // Value nodes
        JsonTreeNode.Type type;
        if (node.isTextual()) type = JsonTreeNode.Type.STRING;
        else if (node.isNumber()) type = JsonTreeNode.Type.NUMBER;
        else if (node.isBoolean()) type = JsonTreeNode.Type.BOOLEAN;
        else type = JsonTreeNode.Type.NULL;

        return new TreeItem<>(
                new JsonTreeNode(key, node.asText(), type)
        );
    }

    public CodeArea getEditor() { return codeArea; }
    public BooleanProperty dirtyProperty() { return dirty; }
    public @Nullable Path getPath() { return path; }
    public TreeView<JsonTreeNode> getJsonTree() { return jsonTree; }
}
