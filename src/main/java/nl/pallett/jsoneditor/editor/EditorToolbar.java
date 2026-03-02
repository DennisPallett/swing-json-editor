package nl.pallett.jsoneditor.editor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class EditorToolbar extends ToolBar {
    private final ObjectProperty<EditorMode> currentMode = new SimpleObjectProperty<>();

    public EditorToolbar (EditorMode initialMode) {
        super();
        currentMode.set(initialMode);

        ToggleButton jsonMode = new ToggleButton("JSON");
        ToggleButton yamlMode = new ToggleButton("YAML");

        ToggleGroup modeGroup = new ToggleGroup();
        jsonMode.setToggleGroup(modeGroup);
        yamlMode.setToggleGroup(modeGroup);

        // Default selection
        jsonMode.setSelected(initialMode == EditorMode.JSON);
        yamlMode.setSelected(initialMode == EditorMode.YAML);

        modeGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == jsonMode) {
                currentMode.set(EditorMode.JSON);
            } else {
                currentMode.set(EditorMode.YAML);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

//        currentMode.addListener((obs, oldMode, newMode) -> {
//            switch (newMode) {
//                case JSON -> switchToEditMode();
//                case YAML -> switchToRunMode();
//            }
//        });

        ToolBar toolBar = new ToolBar(spacer, jsonMode, yamlMode);
        getItems().addAll(spacer, jsonMode, yamlMode);

    }

    public ObjectProperty<EditorMode> currentMode () {
        return currentMode;
    }
}
