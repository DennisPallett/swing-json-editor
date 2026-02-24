package nl.pallett.jsoneditor;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EditorManager {

    private TabPane tabPane = new TabPane();
    private Map<Tab, EditorDocument> openDocuments = new HashMap<>();

    public EditorManager () {
        tabPane.setTabMinWidth(80);
        tabPane.setTabMaxWidth(160);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
    }

    public void openDocument(@Nullable Path path, String content) {
        EditorDocument doc = new EditorDocument(path, content);

        String tabTitle = (path != null) ? path.getFileName().toString() : "Untitled";

        Tab tab = new Tab(tabTitle);
        //tab.setContent(new VirtualizedScrollPane<>(doc.getEditor()));

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(doc.getEditor());
        doc.setScrollPane(scrollPane);

        TextField filterField = new TextField();
        var treeView = doc.getJsonTree();
        VBox container = new VBox();
        container.setSpacing(5); // space between text field and tree

        var originalRoot = treeView.getRoot();

        filterField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                treeView.setRoot(originalRoot);
            } else {
                TreeItem<JsonTreeNode> filteredRoot =
                        filterTree(originalRoot, newValue.toLowerCase());
                treeView.setRoot(filteredRoot);
            }
        });

        container.getChildren().addAll(filterField, treeView);

        // Make both take full width
        filterField.setMaxWidth(Double.MAX_VALUE);
        treeView.setMaxWidth(Double.MAX_VALUE);

        // Allow TreeView to grow vertically
        VBox.setVgrow(treeView, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(container, scrollPane);
        tab.setContent(splitPane);

        tab.setOnCloseRequest(event -> {
            if (doc.dirtyProperty().getValue()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Unsaved changes");
                alert.setContentText("Close without saving?");

                if (alert.showAndWait().get() != ButtonType.OK) {
                    event.consume();
                }
            }
        });

        openDocuments.put(tab, doc);
        tabPane.getTabs().add(tab);

        tabPane.getSelectionModel().select(tab);

        doc.dirtyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tab.setText(tabTitle + "*");
            } else {
                tab.setText(tabTitle);
            }
        });
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

    public TabPane getTabPane() {
        return tabPane;
    }

    public CodeArea getActiveEditor() {
        return getActiveDocument().getEditor();
    }

    public EditorDocument getActiveDocument () {
        return openDocuments.get(tabPane.getSelectionModel().getSelectedItem());
    }
}
