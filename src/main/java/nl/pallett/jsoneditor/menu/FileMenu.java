package nl.pallett.jsoneditor.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.pallett.jsoneditor.EditorDocument;
import nl.pallett.jsoneditor.EditorManager;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static nl.pallett.jsoneditor.SwingJsonEditorApp.showError;

public class FileMenu extends Menu {
    private final EditorManager editorManager;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Stage stage;

    public FileMenu(EditorManager editorManager, Stage stage) {
        super("File");
        this.editorManager = editorManager;
        this.stage = stage;

        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As...");
        MenuItem saveAllItem = new MenuItem("Save All");

        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));

        getItems().addAll(
                newItem,
                openItem,
                new SeparatorMenuItem(),
                saveItem,
                saveAsItem,
                saveAllItem
        );

        saveItem.disableProperty().bind(editorManager.activeDocumentAvailableProperty().not());
        saveAsItem.disableProperty().bind(editorManager.activeDocumentAvailableProperty().not());
        saveAllItem.disableProperty().bind(editorManager.activeDocumentAvailableProperty().not());

        // ---- Actions ----
        newItem.setOnAction(e -> newFile());
        openItem.setOnAction(e -> openFile());
        saveItem.setOnAction(e -> saveFile(editorManager.getActiveDocument()));
        saveAsItem.setOnAction(e -> saveFileAs(editorManager.getActiveDocument()));
        saveAllItem.setOnAction(_ -> saveAll());
    }

    private void newFile() {
        editorManager.openDocument(null, "");
    }

    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON files", "*.json"),
                new FileChooser.ExtensionFilter("All files", "*.*")
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

    private void saveAll() {
        editorManager.getOpenDocuments().forEach(editorDoc -> {
            if (editorDoc.dirtyProperty().get()) {
                saveFile(editorDoc);
            }
        });
    }

    private void saveFile(@Nullable EditorDocument editorDocument) {
        if (editorDocument == null) {
            showError(new IllegalStateException("No active document open"));
            return;
        }

        Path file = editorDocument.getPath();
        String content = editorDocument.getJson();

        try {
            if (file != null) {
                Files.writeString(file, content);
                editorDocument.setDirtyChecksum(content);
            } else {
                saveFileAs(editorDocument);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveFileAs(@Nullable EditorDocument editorDocument) {
        if (editorDocument == null) {
            showError(new IllegalStateException("No active document open"));
            return;
        }

        String content = editorDocument.getJson();

        try {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            File file = chooser.showSaveDialog(stage);
            if (file != null) {
                Files.writeString(file.toPath(), content);
                editorDocument.setDirtyChecksum(content);
                editorDocument.setFile(file.toPath());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

}
