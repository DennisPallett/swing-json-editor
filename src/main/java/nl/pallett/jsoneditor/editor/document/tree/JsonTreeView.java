package nl.pallett.jsoneditor.editor.document.tree;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.*;
import nl.pallett.jsoneditor.editor.EditorMode;
import nl.pallett.jsoneditor.editor.ast.AstNode;
import nl.pallett.jsoneditor.editor.ast.AstPrinter;
import nl.pallett.jsoneditor.editor.document.EditorDocument;
import nl.pallett.jsoneditor.editor.document.JsonPath;
import nl.pallett.jsoneditor.editor.parser.FormatParser;
import nl.pallett.jsoneditor.editor.parser.JsonParserAdapter;
import nl.pallett.jsoneditor.editor.parser.YamlParserAdapter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonTreeView extends TreeView<AstNode> {

    private TreeItem<JsonTreeNode> unfilteredFullRoot;

    private final EditorDocument editorDocument;

    private List<TreeItem<JsonTreeNode>> flatNodes = new ArrayList<>();

    private final FormatParser jsonParser = new JsonParserAdapter();

    private final FormatParser yamlParser = new YamlParserAdapter();

    public JsonTreeView(EditorDocument document) {
        super();
        this.editorDocument = document;

        setShowRoot(false);
        setCellFactory(tv -> new JsonTreeCell(this));

        createContextMenu();
    }

    public void expandAll(TreeItem<?> item) {
        if (item != null) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandAll(child);
            }
        }
    }

    public void collapseAll(TreeItem<?> item) {
        if (item != null) {
            if (item.getParent() != null) {
                item.setExpanded(false);
            }
            for (TreeItem<?> child : item.getChildren()) {
                collapseAll(child);
            }
        }
    }

    public void refreshJsonTree(String text) {
        // todo
        try {
            AstNode root;
            if (editorDocument.getEditorMode() == EditorMode.JSON) {
                root = jsonParser.parse(text);
            } else {
                root = yamlParser.parse(text);
            }
            AstTreeBuilder builder = new AstTreeBuilder();

            if (root != null) {
                // Print the AST to the console
                AstPrinter.printAst(root);

                TreeItem<AstNode> rootItem = builder.buildTree(root);
                setRoot(rootItem);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public TreeItem<JsonTreeNode> selectNodeForCaretPosition(int caretPos) {

        TreeItem<JsonTreeNode> best = null;
        int smallestSize = Integer.MAX_VALUE;

        for (TreeItem<JsonTreeNode> item : flatNodes) {

            IndexRange r = item.getValue().getRange();
            if (r == null) continue;

            if (caretPos >= r.getStart() && caretPos <= r.getEnd()) {

                int size = r.getEnd() - r.getStart();

                if (size < smallestSize) {
                    smallestSize = size;
                    best = item;
                }
            }
        }

        if (best != null) {
            //getSelectionModel().select(best);
            //scrollTo(getRow(best));
        }

        return best;
    }




    public void filterOnValue(String value) {
//        if (value == null || value.isBlank()) {
//            setRoot(unfilteredFullRoot);
//        } else {
//            TreeItem<JsonTreeNode> filteredRoot =
//                    filterTree(unfilteredFullRoot, value.toLowerCase());
//            setRoot(filteredRoot);
//        }
    }

    private void createContextMenu() {
        MenuItem expandAllItem = new MenuItem("Expand All");
        MenuItem collapseAllItem = new MenuItem("Collapse All");
        expandAllItem.setOnAction(e -> expandAll(getRoot()));
        collapseAllItem.setOnAction(e -> collapseAll(getRoot()));
        // Create context menu
        ContextMenu contextMenu = new ContextMenu(expandAllItem, collapseAllItem);

        // Attach it to the TreeView (applies to empty space and anywhere in the control)
        setContextMenu(contextMenu);
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
                    new TreeItem<>(new JsonTreeNode(key, null, JsonTreeNode.Type.OBJECT, currentPath, node));

            node.fieldNames().forEachRemaining(field ->
                    item.getChildren().add(
                            buildTree(node.get(field), field, currentPath)
                    )
            );
            return item;
        }

        if (node.isArray()) {
            TreeItem<JsonTreeNode> item =
                    new TreeItem<>(new JsonTreeNode(key, null, JsonTreeNode.Type.ARRAY, currentPath, node));

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
                new JsonTreeNode(key, node.asText(), type, currentPath, node)
        );
    }

    public EditorDocument getEditorDocument () {
        return editorDocument;
    }
}
