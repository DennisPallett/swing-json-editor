package nl.pallett.jsoneditor;

import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EditorManager {

    private final TabPane tabPane = new TabPane();
    private final Map<Tab, EditorDocument> openDocuments = new HashMap<>();

    public EditorManager () {
        tabPane.setTabMinWidth(80);
        tabPane.setTabMaxWidth(160);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
    }

    public void openDocument(@Nullable Path path, String content) {
        EditorDocument doc = new EditorDocument(path, content);

        Tab tab = new EditorTab(path, content, doc);

        openDocuments.put(tab, doc);
        tabPane.getTabs().add(tab);

        tabPane.getSelectionModel().select(tab);
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public CodeArea getActiveEditor() {
        return getActiveDocument().getEditor();
    }

    public EditorDocument getActiveDocument () {
        return openDocuments.get(tabPane.getSelectionModel().getSelectedItem());
    }
}
