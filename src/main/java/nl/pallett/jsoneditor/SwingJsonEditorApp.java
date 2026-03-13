package nl.pallett.jsoneditor;

import java.awt.desktop.OpenFilesEvent;
import java.util.Arrays;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import nl.pallett.jsoneditor.editor.EditorManager;
import nl.pallett.jsoneditor.menu.EditMenu;
import nl.pallett.jsoneditor.menu.FileMenu;

import java.awt.*;
import java.io.File;
import java.util.List;

public class SwingJsonEditorApp extends Application {
    public static final String APP_ID = "nl.pallett.jsoneditor";

    private final EditorManager editorManager = new EditorManager();

    private Stage stage;



    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage) {
        System.out.println("start");

        this.stage = stage;

        MacOSIntegration.setFileOpenHandler(file -> {
            editorManager.openDocument(file.toPath());
        });

        Parameters params = getParameters();
        List<String> launchArgs = params.getRaw();
        //preventDuplicateInstances(launchArgs);

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
        stage.setTitle("Swing JSON/YAML Editor");
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

        MacOSIntegration.markJavaFXReady();

        // open application with an initial empty JSON document if no initial file has been opened
        if (!editorManager.anyOpenDocuments()) {
            editorManager.openDocument(null, "");
        }
    }

    @Override
    public void init() {
        System.out.println("init");
        // if (Desktop.isDesktopSupported()) {
        //     Desktop desktop = Desktop.getDesktop();
        //
        //     desktop.setOpenFileHandler(event -> {
        //         System.out.println("RECEIVED OPEN FILE EVENT");
        //         for (File file : event.getFiles()) {
        //             System.out.println("Opening file: " + file.getAbsolutePath());
        //             Platform.runLater(() -> {
        //                 editorManager.openDocument(file.toPath());
        //                 bringToFront();
        //             });
        //         }
        //     });
        // }


    }

    public static void showError(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
        alert.showAndWait();
    }

    private void preventDuplicateInstances (List<String> launchArgs) {
        boolean isPrimary = SingleInstanceManager.startServer(files -> {
            Platform.runLater(() -> {
                editorManager.openDocuments(files);
                bringToFront();
            });
        });

        if (!isPrimary) {
            // Send args to running instance
            SingleInstanceManager.sendToRunningInstance(launchArgs);
            System.exit(0);
        }
    }

    private static void bringToFront() {
        Platform.runLater(() -> {
            for (Stage stage : Stage.getWindows()
                    .filtered(w -> w instanceof Stage)
                    .stream()
                    .map(w -> (Stage) w)
                    .toList()) {
                stage.toFront();
            }
        });
    }
}