package nl.pallett.jsoneditor.editor.document.tree;

import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import nl.pallett.jsoneditor.editor.ast.AstNode;
import nl.pallett.jsoneditor.util.ClipboardUtil;

public class JsonTreeCell extends TreeCell<AstNode> {

    private final JsonTreeView treeView;

    public JsonTreeCell(JsonTreeView treeView) {
        super();
        this.treeView = treeView;

        createContextMenu();
    }

    @Override
    protected void updateItem(AstNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            getStyleClass().removeAll(
                    "json-object", "json-array",
                    "json-string", "json-number",
                    "json-boolean", "json-null"
            );
            return;
        }

        getStyleClass().removeAll(
                "json-object", "json-array",
                "json-string", "json-number",
                "json-boolean", "json-null"
        );

        formatNode(item);
    }

    private void formatNodeValue(AstNode item) {
        if (item.getValueType() == null) {
            setText((item.getKey() != null) ? item.getKey() + " : \"" + item.getValue() + "\"" : item.getValue());
            getStyleClass().add("json-string");
            return;
        }

        switch (item.getValueType()) {
            case INTEGER, FLOAT -> {
                setText(item.getKey() + " : " + item.getValue());
                getStyleClass().add("json-number");
            }
            case BOOLEAN -> {
                setText(item.getKey() + " : " + item.getValue());
                getStyleClass().add("json-boolean");
            }
            case NULL -> {
                setText(item.getKey() + " : null");
                getStyleClass().add("json-null");
            }
            default -> {
                setText((item.getKey() != null) ? item.getKey() + " : \"" + item.getValue() + "\"" : item.getValue());
                getStyleClass().add("json-string");
            }
        }
    }

    private void formatNode(AstNode item) {
        switch (item.getType()) {
            case OBJECT -> {
                setText(item.getKey() != null ? item.getKey() + " { }" : "{ }");
                getStyleClass().add("json-object");
                setGraphic(new Label("🟣"));
            }
            case ARRAY -> {
                setText(item.getKey() + " [ ]");
                getStyleClass().add("json-array");
            }
            case VALUE -> formatNodeValue(item);
            case PROPERTY -> setText(item.getKey());
            case COMMENT -> setText("# " + item.getValue());
            case ALIAS -> setText("*" + item.getAlias());
            case DOCUMENT -> setText("document");
        }
    }

    private void createContextMenu() {
        // Create context menu for this cell
        MenuItem copyKeyItem = new MenuItem("Copy key");
        MenuItem copyValueItem = new MenuItem("Copy value");
        MenuItem copyPathItem = new MenuItem();
        MenuItem copyJsonItem = new MenuItem();

        copyPathItem.textProperty().bind(Bindings.createStringBinding(
                () -> "Copy " + treeView.getEditorDocument().getEditorMode() + " path",
                treeView.getEditorDocument().getEditorModeProperty()))
        ;

        copyJsonItem.textProperty().bind(Bindings.createStringBinding(
                () -> "Copy full " + treeView.getEditorDocument().getEditorMode(),
                treeView.getEditorDocument().getEditorModeProperty()))
        ;

        MenuItem expandAllItem = new MenuItem("Expand All");
        MenuItem collapseAllItem = new MenuItem("Collapse All");

        copyKeyItem.setOnAction(e -> {
            TreeItem<AstNode> treeItem = getTreeItem();
            if (treeItem != null) {
                ClipboardUtil.copyToClipboard(treeItem.getValue().getKey());
            }
        });

        copyValueItem.setOnAction(e -> {
            TreeItem<AstNode> treeItem = getTreeItem();
            if (treeItem != null) {
                ClipboardUtil.copyToClipboard(treeItem.getValue().getValue());
            }
        });

        copyPathItem.setOnAction(e -> {
            TreeItem<AstNode> treeItem = getTreeItem();
            if (treeItem != null) {
                //ClipboardUtil.copyToClipboard(treeItem.getValue().getPath().toFullPath());
            }
        });

        copyJsonItem.setOnAction(_ -> {
            TreeItem<AstNode> treeItem = getTreeItem();
            if (treeItem != null) {
//                try {
//                    String prettyCode = StringUtil.formatCode(
//                            treeView.getEditorDocument().getEditorMode(),
//                            treeItem.getValue().getJsonNode()
//                    );
//                    ClipboardUtil.copyToClipboard(prettyCode);
//                } catch (JsonProcessingException e) {
//                    SwingJsonEditorApp.showError(e);
//                }
            }
        });

        expandAllItem.setOnAction(e -> treeView.expandAll(treeView.getRoot()));
        collapseAllItem.setOnAction(e -> treeView.collapseAll(treeView.getRoot()));


        ContextMenu menu = new ContextMenu(
                copyKeyItem,
                copyValueItem,
                copyPathItem,
                copyJsonItem,
                new SeparatorMenuItem(),
                expandAllItem,
                collapseAllItem
        );

        // Only show menu for non-empty cells
        contextMenuProperty().bind(
                Bindings.when(emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(menu)
        );
    }
}
