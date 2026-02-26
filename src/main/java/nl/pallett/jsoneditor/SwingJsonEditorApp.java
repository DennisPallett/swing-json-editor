package nl.pallett.jsoneditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import nl.pallett.jsoneditor.menu.EditMenu;
import nl.pallett.jsoneditor.menu.FileMenu;

public class SwingJsonEditorApp extends Application {

    private final EditorManager editorManager = new EditorManager();

    @Override
    public void start(Stage stage) {
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

        // open application with an initial empty JSON document
        editorManager.openDocument(null, "");
    }

    public static void showError(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}