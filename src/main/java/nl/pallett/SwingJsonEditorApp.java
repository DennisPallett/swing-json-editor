package nl.pallett;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.nio.file.Files;

public class SwingJsonEditorApp extends Application {

    private final ObjectMapper mapper = new ObjectMapper();
    private final CodeArea codeArea = new CodeArea();

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        // ---- Menu Bar ----
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);

        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem saveItem = new MenuItem("Save As...");
        MenuItem prettyItem = new MenuItem("Pretty Print");
        MenuItem validateItem = new MenuItem("Validate");

        fileMenu.getItems().addAll(openItem, saveItem, new SeparatorMenuItem(),
                prettyItem, validateItem);

        menuBar.getMenus().add(fileMenu);
        root.setTop(menuBar);

        // ---- Editor ----
        codeArea.setWrapText(true);
        root.setCenter(codeArea);

        // ---- Actions ----
        openItem.setOnAction(e -> openFile(stage));
        saveItem.setOnAction(e -> saveFile(stage));
        prettyItem.setOnAction(e -> prettyPrint());
        validateItem.setOnAction(e -> validateJson());

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Swing JSON Editor");
        stage.setScene(scene);
        stage.show();
    }

    private void openFile(Stage stage) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                codeArea.replaceText(Files.readString(file.toPath()));
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveFile(Stage stage) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            File file = chooser.showSaveDialog(stage);
            if (file != null) {
                Files.writeString(file.toPath(), codeArea.getText());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void prettyPrint() {
        try {
            Object json = mapper.readValue(codeArea.getText(), Object.class);
            String formatted = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json);
            codeArea.replaceText(formatted);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void validateJson() {
        try {
            mapper.readTree(codeArea.getText());
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