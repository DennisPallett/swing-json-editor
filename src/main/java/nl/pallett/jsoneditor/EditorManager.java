package nl.pallett.jsoneditor;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EditorManager {

    private TabPane tabPane = new TabPane();
    private Map<Tab, EditorDocument> openDocuments = new HashMap<>();

    public void openDocument(Path path, String content) {
        EditorDocument doc = new EditorDocument(path, content);

        Tab tab = new Tab(path.getFileName().toString());
        tab.setContent(new VirtualizedScrollPane<>(doc.getEditor()));
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

        doc.dirtyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tab.setText(path.getFileName() + "*");
            } else {
                tab.setText(path.getFileName().toString());
            }
        });
    }

    public TabPane getTabPane() {
        return tabPane;
    }
}
