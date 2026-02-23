package nl.pallett.jsoneditor;

import javafx.scene.control.*;
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

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(doc.getJsonTree(), scrollPane);
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
