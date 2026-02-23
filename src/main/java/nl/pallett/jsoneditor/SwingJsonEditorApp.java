package nl.pallett.jsoneditor;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SwingJsonEditorApp extends Application {

    private final ObjectMapper mapper = new ObjectMapper();
    //private final CodeArea codeArea = new CodeArea();
    private TabPane tabPane = new TabPane();

    private final EditorManager editorManager = new EditorManager();

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.setCenter(editorManager.getTabPane());

        // ---- Menu Bar ----
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);

        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As...");
        MenuItem prettyItem = new MenuItem("Pretty Print");
        MenuItem validateItem = new MenuItem("Validate");

        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));

        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, new SeparatorMenuItem(),
                prettyItem, validateItem);

        menuBar.getMenus().add(fileMenu);
        root.setTop(menuBar);

        // ---- Actions ----
        newItem.setOnAction(e -> newFile());
        openItem.setOnAction(e -> openFile(stage));
        saveItem.setOnAction(e -> saveFile(stage));
        saveAsItem.setOnAction(e -> saveFileAs(stage));
        prettyItem.setOnAction(e -> prettyPrint());
        validateItem.setOnAction(e -> validateJson());

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(
                getClass().getResource("/json-editor.css").toExternalForm()

        );
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );
        scene.getStylesheets().add(
                getClass().getResource("/json-tree.css").toExternalForm()
        );
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Swing JSON Editor");
        stage.setScene(scene);
        stage.show();

        newFile();
    }

    private void newFile() {
        editorManager.openDocument(null, "");
    }

    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                editorManager.openDocument(file.toPath(), content);
            } catch (IOException ex) {
                showError(ex);
            }
        }
    }

    private void saveFile(Stage stage) {
        EditorDocument activeDocument = editorManager.getActiveDocument();
        if (activeDocument == null) {
            showError(new IllegalStateException("No active document open"));
            return;
        }

        Path file = activeDocument.getPath();
        String content = activeDocument.getEditor().getText();

        try {
            if (file != null) {
                Files.writeString(file, content);
                activeDocument.setDirtyChecksum(content);
            } else {
                saveFileAs(stage);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveFileAs(Stage stage) {
        EditorDocument activeDocument = editorManager.getActiveDocument();
        if (activeDocument == null) {
            showError(new IllegalStateException("No active document open"));
            return;
        }

        String content = activeDocument.getEditor().getText();

        try {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            File file = chooser.showSaveDialog(stage);
            if (file != null) {
                Files.writeString(file.toPath(), content);
                activeDocument.setDirtyChecksum(content);
                activeDocument.setFile(file.toPath());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void prettyPrint() {
        try {
            Object json = mapper.readValue(editorManager.getActiveEditor().getText(), Object.class);
            String formatted = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json);
            editorManager.getActiveEditor().replaceText(formatted);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void validateJson() {
        try {
            mapper.readTree(editorManager.getActiveEditor().getText());
            showInfo("Valid JSON ✅");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}