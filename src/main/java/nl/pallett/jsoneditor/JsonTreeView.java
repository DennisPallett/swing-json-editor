package nl.pallett.jsoneditor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class JsonTreeView extends TreeView<JsonTreeNode> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private TreeItem<JsonTreeNode> unfilteredFullRoot;

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
            Set<JsonPath> expanded = new HashSet<>();

            if (getRoot() != null) {
                expanded = captureExpandedPaths(getRoot());
            }

            JsonNode node = objectMapper.readTree(json);

            TreeItem<JsonTreeNode> newRoot =
                    buildTree(node, "root", null);

            setRoot(newRoot);
            unfilteredFullRoot = getRoot();

            restoreExpandedPaths(newRoot, expanded);

        } catch (Exception ignored) {
            // invalid JSON
        }
    }

    public void filterOnValue(String value) {
        if (value == null || value.isBlank()) {
            setRoot(unfilteredFullRoot);
        } else {
            TreeItem<JsonTreeNode> filteredRoot =
                    filterTree(unfilteredFullRoot, value.toLowerCase());
            setRoot(filteredRoot);
        }
    }

    private TreeItem<JsonTreeNode> filterTree(TreeItem<JsonTreeNode> source, String filter) {

        if (source == null) return null;

        TreeItem<JsonTreeNode> filteredItem = new TreeItem<>(source.getValue());

        for (TreeItem<JsonTreeNode> child : source.getChildren()) {
            TreeItem<JsonTreeNode> filteredChild = filterTree(child, filter);
            if (filteredChild != null) {
                filteredItem.getChildren().add(filteredChild);
            }
        }

        // Keep item if:
        // 1) It matches
        // 2) Any child matches
        String jsonKey = source.getValue().getKey();
        String jsonValue = source.getValue().getValue();
        if ((jsonKey != null && jsonKey.toLowerCase().contains(filter))
                || (jsonValue != null && jsonValue.toLowerCase().contains(filter))
                || !filteredItem.getChildren().isEmpty()) {
            filteredItem.setExpanded(true); // auto-expand matches
            return filteredItem;
        }

        return null;
    }

    private Set<JsonPath> captureExpandedPaths(TreeItem<JsonTreeNode> root) {
        Set<JsonPath> expanded = new HashSet<>();
        captureExpanded(root, expanded);
        return expanded;
    }

    private void captureExpanded(TreeItem<JsonTreeNode> item, Set<JsonPath> expanded) {
        if (item.isExpanded()) {
            expanded.add(item.getValue().getPath());
        }
        for (TreeItem<JsonTreeNode> child : item.getChildren()) {
            captureExpanded(child, expanded);
        }
    }

    private void restoreExpandedPaths(TreeItem<JsonTreeNode> root, Set<JsonPath> expanded) {
        restoreExpanded(root, expanded);
    }

    private void restoreExpanded(TreeItem<JsonTreeNode> item, Set<JsonPath> expanded) {
        if (expanded.contains(item.getValue().getPath())) {
            item.setExpanded(true);
        }
        for (TreeItem<JsonTreeNode> child : item.getChildren()) {
            restoreExpanded(child, expanded);
        }
    }

    private TreeItem<JsonTreeNode> buildTree(JsonNode node, String key, @Nullable JsonPath parentPath) {
        JsonPath currentPath = new JsonPath(parentPath, key);

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
