package nl.pallett.jsoneditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import nl.pallett.jsoneditor.util.ClipboardUtil;
import nl.pallett.jsoneditor.util.ObjectMapperUtil;

public class JsonTreeCell extends TreeCell<JsonTreeNode> {

    private final JsonTreeView treeView;

    public JsonTreeCell(JsonTreeView treeView) {
        super();
        this.treeView = treeView;

        createContextMenu();
    }

    @Override
    protected void updateItem(JsonTreeNode item, boolean empty) {
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

        switch (item.getType()) {
            case OBJECT -> {
                setText(item.getKey() + " { }");
                getStyleClass().add("json-object");
                setGraphic(new Label("🟣"));
            }
            case ARRAY -> {
                setText(item.getKey() + " [ ]");
                getStyleClass().add("json-array");
            }
            case STRING -> {
                setText(item.getKey() + " : \"" + item.getValue() + "\"");
                getStyleClass().add("json-string");
            }
            case NUMBER -> {
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
        }
    }

    private void createContextMenu() {
        // Create context menu for this cell
        MenuItem copyKeyItem = new MenuItem("Copy key");
        MenuItem copyValueItem = new MenuItem("Copy value");
        MenuItem copyPathItem = new MenuItem("Copy JSON path");
        MenuItem copyJsonItem = new MenuItem("Copy full JSON");

        MenuItem expandAllItem = new MenuItem("Expand All");
        MenuItem collapseAllItem = new MenuItem("Collapse All");

        copyKeyItem.setOnAction(e -> {
            TreeItem<JsonTreeNode> treeItem = getTreeItem();
            if (treeItem != null) {
                ClipboardUtil.copyToClipboard(treeItem.getValue().getKey());
            }
        });

        copyValueItem.setOnAction(e -> {
            TreeItem<JsonTreeNode> treeItem = getTreeItem();
            if (treeItem != null) {
                ClipboardUtil.copyToClipboard(treeItem.getValue().getValue());
            }
        });

        copyPathItem.setOnAction(e -> {
            TreeItem<JsonTreeNode> treeItem = getTreeItem();
            if (treeItem != null) {
                ClipboardUtil.copyToClipboard(treeItem.getValue().getPath().toFullPath());
            }
        });

        copyJsonItem.setOnAction(_ -> {
            TreeItem<JsonTreeNode> treeItem = getTreeItem();
            if (treeItem != null) {
                try {
                    String prettyJson = ObjectMapperUtil.getInstance()
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(treeItem.getValue().getJsonNode());
                    ClipboardUtil.copyToClipboard(prettyJson);
                } catch (JsonProcessingException e) {
                    SwingJsonEditorApp.showError(e);
                }
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
