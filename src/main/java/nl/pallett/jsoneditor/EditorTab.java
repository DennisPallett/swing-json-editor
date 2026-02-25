package nl.pallett.jsoneditor;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.StatusBar;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class EditorTab extends Tab {
    public EditorTab(@Nullable Path path, String content, EditorDocument doc) {
        super();

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

        doc.dirtyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                setText(tabTitle + "*");
            } else {
                setText(tabTitle);
            }
        });
    }


}
