package nl.pallett.jsoneditor.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import nl.pallett.jsoneditor.EditorManager;

public class EditMenu extends Menu {
    private final EditorManager editorManager;

    public EditMenu(EditorManager editorManager) {
        super("Edit");
        this.editorManager = editorManager;

        MenuItem undoItem = new MenuItem("Undo");
        MenuItem redoItem = new MenuItem("Redo");
        MenuItem formatJsonItem = new MenuItem("Format JSON");

        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        formatJsonItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN));

        getItems().addAll(
                undoItem,
                redoItem,
                new SeparatorMenuItem(),
                formatJsonItem
        );

        formatJsonItem.setOnAction(e -> formatJson());
        undoItem.setOnAction(e -> editorManager.getActiveEditor().undo());
        redoItem.setOnAction(e -> editorManager.getActiveEditor().redo());

        undoItem.disableProperty().bind(editorManager.undoAvailableProperty().not());
        redoItem.disableProperty().bind(editorManager.redoAvailableProperty().not());
    }

    private void formatJson() {
        editorManager.getActiveDocument().formatJson();
    }
}
