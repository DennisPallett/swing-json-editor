package nl.pallett.jsoneditor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.HashSet;
import java.util.Set;

public class JsonTreeView extends TreeView<JsonTreeNode> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonTreeView(EditorDocument document) {
        super();

        setShowRoot(false);
        setCellFactory(tv -> new JsonTreeCell());

        getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        document.scrollToJsonPath(newItem.getValue());
                    }
                });
    }

    public void refreshJsonTree(String json) {
        try {
            Set<String> expanded = new HashSet<>();

            if (getRoot() != null) {
                expanded = captureExpandedPaths(getRoot());
            }

            JsonNode node = objectMapper.readTree(json);

            TreeItem<JsonTreeNode> newRoot =
                    buildTree(node, "root", "");

            setRoot(newRoot);

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
}
