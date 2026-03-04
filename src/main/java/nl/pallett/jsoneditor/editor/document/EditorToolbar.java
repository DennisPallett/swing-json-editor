package nl.pallett.jsoneditor.editor.document;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import nl.pallett.jsoneditor.editor.EditorMode;

public class EditorToolbar extends ToolBar {
    private final ObjectProperty<EditorMode> currentMode = new SimpleObjectProperty<>();

    public EditorToolbar (EditorMode initialMode) {
        super();


        ToggleButton jsonMode = new ToggleButton("JSON");
        jsonMode.setUserData(EditorMode.JSON);

        ToggleButton yamlMode = new ToggleButton("YAML");
        yamlMode.setUserData(EditorMode.YAML);

        ToggleGroup modeGroup = new ToggleGroup();
        jsonMode.setToggleGroup(modeGroup);
        yamlMode.setToggleGroup(modeGroup);

        modeGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                currentMode.set((EditorMode) selected.getUserData());
            }
        });

        currentMode.addListener((obs, oldMode, newMode) -> {
            for (Toggle toggle : modeGroup.getToggles()) {
                if (toggle.getUserData() == newMode) {
                    modeGroup.selectToggle(toggle);
                    break;
                }
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getItems().addAll(spacer, jsonMode, yamlMode);

        currentMode.set(initialMode);
    }

    public ObjectProperty<EditorMode> currentMode () {
        return currentMode;
    }
}
