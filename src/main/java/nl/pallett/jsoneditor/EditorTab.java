package nl.pallett.jsoneditor;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class EditorTab extends Tab {
    public final EditorManager editorManager;

    public EditorTab(@Nullable Path path, String content, EditorDocument doc, EditorManager editorManager) {
        super();
        this.editorManager = editorManager;

        final String tabTitle = (path != null) ? path.getFileName().toString() : "Untitled";
        setText(tabTitle);

        TextField filterField = new TextField();
        JsonTreeView treeView = doc.getJsonTree();
        VBox leftContainer = new VBox();
        leftContainer.setSpacing(5); // space between text field and tree

        filterField.textProperty().addListener(
                (obs, oldValue, newValue) -> treeView.filterOnValue(newValue)
        );

        leftContainer.getChildren().addAll(filterField, treeView);

        // Make both take full width
        filterField.setMaxWidth(Double.MAX_VALUE);
        treeView.setMaxWidth(Double.MAX_VALUE);

        // allow filter field to be cleared when pressing ESC
        filterField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                filterField.clear();
                event.consume();
            }
        });


        // Allow TreeView to grow vertically
        VBox.setVgrow(treeView, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftContainer, doc.getContainer());
        setContent(splitPane);

        setOnCloseRequest(event -> {
            if (doc.dirtyProperty().getValue()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Unsaved changes");
                alert.setContentText("Close without saving?");

                if (alert.showAndWait().get() != ButtonType.OK) {
                    event.consume();
                }
            }
        });

        setOnClosed(event -> {
            editorManager.closeDocument(this);
        });

        doc.dirtyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                setText(tabTitle + "*");
            } else {
                setText(tabTitle);
            }
        });
    }
}
