package nl.pallett.jsoneditor;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;

public class JsonTreeCell extends TreeCell<JsonTreeNode> {

    @Override
    protected void updateItem(JsonTreeNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            getStyleClass().removeAll(
                    "json-object", "json-array",
                    "json-string", "json-number",
                    "json-boolean", "json-null"
            );
            return;
        }

        getStyleClass().removeAll(
                "json-object", "json-array",
                "json-string", "json-number",
                "json-boolean", "json-null"
        );

        switch (item.getType()) {
            case OBJECT -> {
                setText(item.getKey() + " { }");
                getStyleClass().add("json-object");
                setGraphic(new Label("🟣"));
            }
            case ARRAY -> {
                setText(item.getKey() + " [ ]");
                getStyleClass().add("json-array");
            }
            case STRING -> {
                setText(item.getKey() + " : \"" + item.getValue() + "\"");
                getStyleClass().add("json-string");
            }
            case NUMBER -> {
                setText(item.getKey() + " : " + item.getValue());
                getStyleClass().add("json-number");
            }
            case BOOLEAN -> {
                setText(item.getKey() + " : " + item.getValue());
                getStyleClass().add("json-boolean");
            }
            case NULL -> {
                setText(item.getKey() + " : null");
                getStyleClass().add("json-null");
            }
        }
    }
}
