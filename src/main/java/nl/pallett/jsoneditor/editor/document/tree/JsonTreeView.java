package nl.pallett.jsoneditor.editor.document.tree;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import nl.pallett.jsoneditor.editor.EditorMode;
import nl.pallett.jsoneditor.editor.ast.AstIntervalIndex;
import nl.pallett.jsoneditor.editor.ast.AstNode;
import nl.pallett.jsoneditor.editor.ast.AstPrinter;
import nl.pallett.jsoneditor.editor.ast.PointerType;
import nl.pallett.jsoneditor.editor.document.EditorDocument;
import nl.pallett.jsoneditor.editor.parser.FormatParser;
import nl.pallett.jsoneditor.editor.parser.JsonParserAdapter;
import nl.pallett.jsoneditor.editor.parser.YamlParserAdapter;
import nl.pallett.jsoneditor.util.TreeViewUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonTreeView extends TreeView<AstNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTreeView.class);

    private TreeItem<AstNode> unfilteredFullRoot;

    private final EditorDocument editorDocument;

    private final FormatParser yamlParser = new YamlParserAdapter();

    private final AstTreeBuilder astTreeBuilder = new AstTreeBuilder();

    private AstIntervalIndex astIntervalIndex;

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

    public void selectAndReveal(AstNode node) {
        TreeItem<AstNode> item = astIntervalIndex.getTreeItemForNode(node);
        if (item == null) {
            return;
        }

        // Expand all parents
        TreeItem<AstNode> parent = item.getParent();
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }

        // Select the item
        getSelectionModel().select(item);

        // Scroll to it after layout updates
        Platform.runLater(() -> {
            int row = getRow(item);
            if (row >= 0) {
                scrollTo(row);
                getFocusModel().focus(row);
            }
        });
    }

    public void selectTreeItemForCaretPosition(int caretPosition) {
        if (astIntervalIndex != null) {
            LOGGER.debug("Looking for: {}", caretPosition);

            AstNode node = astIntervalIndex.findDeepest(caretPosition);

            LOGGER.debug("Node to select: {}", node);

            if (node != null) {
                selectAndReveal(node);
            }
        }
    }

    public void refreshJsonTree(String text) {
        try {
            Set<List<PointerType>> expanded;

            if (getRoot() != null) {
                expanded = captureExpandedPaths(getRoot());
            } else {
                expanded = new HashSet<>();
            }

            AstNode root;
            if (editorDocument.getEditorMode() == EditorMode.JSON) {
                root = new JsonParserAdapter().parse(text);
            } else {
                root = yamlParser.parse(text);
            }

            if (root != null) {
                // log the full AST
                if (LOGGER.isDebugEnabled()) {
                    AstPrinter.logAst(log -> LOGGER.debug("AST tree:\n{}", log), root);
                }

                TreeItem<AstNode> rootItem = astTreeBuilder.buildTree(root);
                astIntervalIndex = new AstIntervalIndex(rootItem);
                setRoot(rootItem);

                unfilteredFullRoot = rootItem;

                // initial state of tree is somewhat collapsed to prevent node overload
                Platform.runLater(() -> {
                    collapseAll(rootItem);

                    // for YAML we show 2 more levels
                    if (editorDocument.getEditorMode() == EditorMode.YAML) {
                        TreeViewUtil.setExpandedAtLevel(rootItem, 1, true);
                        TreeViewUtil.setExpandedAtLevel(rootItem, 2, true);
                    }

                    restoreExpandedPaths(rootItem, expanded);
                });


            }
        } catch (Exception e) {
            LOGGER.error("Failed to refresh tree", e);
        }
    }

    public void filterOnValue(String value) {
        if (value == null || value.isBlank()) {
            setRoot(unfilteredFullRoot);
        } else {
            TreeItem<AstNode> filteredRoot =
                    filterTree(unfilteredFullRoot, value.toLowerCase());
            setRoot(filteredRoot);
        }
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

    private TreeItem<AstNode> filterTree(TreeItem<AstNode> source, String filter) {

        if (source == null) return null;

        TreeItem<AstNode> filteredItem = new TreeItem<>(source.getValue());

        for (TreeItem<AstNode> child : source.getChildren()) {
            TreeItem<AstNode> filteredChild = filterTree(child, filter);
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

    private Set<List<PointerType>> captureExpandedPaths(TreeItem<AstNode> root) {
        Set<List<PointerType>> expanded = new HashSet<>();
        captureExpanded(root, expanded);
        return expanded;
    }

    private void captureExpanded(TreeItem<AstNode> item, Set<List<PointerType>> expanded) {
        if (item.isExpanded()) {
            expanded.add(item.getValue().getPointer());
        }
        for (TreeItem<AstNode> child : item.getChildren()) {
            captureExpanded(child, expanded);
        }
    }

    private void restoreExpandedPaths(TreeItem<AstNode> root, Set<List<PointerType>> expanded) {
        restoreExpanded(root, expanded);
    }

    private void restoreExpanded(TreeItem<AstNode> item, Set<List<PointerType>> expanded) {
        if (expanded.contains(item.getValue().getPointer())) {
            item.setExpanded(true);
        }
        for (TreeItem<AstNode> child : item.getChildren()) {
            restoreExpanded(child, expanded);
        }
    }

    public EditorDocument getEditorDocument () {
        return editorDocument;
    }
}
