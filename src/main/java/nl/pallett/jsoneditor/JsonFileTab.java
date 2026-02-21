package nl.pallett.jsoneditor;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

import java.io.File;

class JsonFileTab extends Tab {
    private File file;
    private TextArea editor;

    public JsonFileTab(File file, String content) {
        super(file == null ? "Untitled" : file.getName());

        this.file = file;
        this.editor = new TextArea(content);

        setContent(editor);
        setClosable(true);

        setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Unsaved changes");
            alert.setContentText("Close without saving?");

            if (alert.showAndWait().get() != ButtonType.OK) {
                event.consume();
            }
        });
    }

    public File getFile() {
        return file;
    }

//    public String getText() {
//        return editor.getText();
//    }
}