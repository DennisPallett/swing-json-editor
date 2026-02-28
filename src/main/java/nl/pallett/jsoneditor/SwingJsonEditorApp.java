package nl.pallett.jsoneditor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import nl.pallett.jsoneditor.menu.EditMenu;
import nl.pallett.jsoneditor.menu.FileMenu;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SwingJsonEditorApp extends Application {

    private final EditorManager editorManager = new EditorManager();

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        BorderPane root = new BorderPane();
        root.setCenter(editorManager.getTabPane());

        // ---- Menu Bar ----
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);

        menuBar.getMenus().addAll(
                new FileMenu(editorManager, stage),
                new EditMenu(editorManager)
        );
        root.setTop(menuBar);

        Scene scene = new Scene(root, 1000, 800);
        scene.getStylesheets().addAll(
                getClass().getResource("/json-editor.css").toExternalForm(),
                getClass().getResource("/style.css").toExternalForm(),
                getClass().getResource("/json-tree.css").toExternalForm()

        );

        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Swing JSON Editor");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            if (editorManager.anyDirtyDocuments()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Unsaved files");
                alert.setContentText("There are unsaved files!\n\nAre you sure you want to close before saving?");

                if (alert.showAndWait().get() != ButtonType.OK) {
                    event.consume();
                }
            }
        });

        // allow an initial file to be opened when passed as first argument
        boolean initialFileOpened = false;
        Parameters params = getParameters();
        List<String> launchArgs = params.getRaw();
        if (!launchArgs.isEmpty()) {
            Path initialPath = Path.of(launchArgs.getFirst());
            if (Files.exists(initialPath)) {
                initialFileOpened = editorManager.openDocument(initialPath);
            }
        }

        // open application with an initial empty JSON document if no initial file has been opened
        if (!initialFileOpened) {
            editorManager.openDocument(null, "");
        }
    }

    @Override
    public void init() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            desktop.setOpenFileHandler(event -> {
                for (File file : event.getFiles()) {
                    Platform.runLater(() -> {
                        editorManager.openDocument(file.toPath());
                        stage.show();
                        stage.toFront();
                    });
                }
            });
        }
    }

    public static void showError(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}