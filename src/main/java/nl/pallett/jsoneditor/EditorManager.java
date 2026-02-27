package nl.pallett.jsoneditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.richtext.CodeArea;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EditorManager {

    private final TabPane tabPane = new TabPane();
    private final Map<EditorTab, EditorDocument> openDocuments = new HashMap<>();
    private final BooleanProperty activeDocumentAvailable = new SimpleBooleanProperty(false);

    private final BooleanProperty undoAvailableProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty redoAvailableProperty = new SimpleBooleanProperty(false);

    public EditorManager () {
        tabPane.setTabMinWidth(80);
        tabPane.setTabMaxWidth(160);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldTab, newTab) -> {
                    if (newTab != null) {
                        handleTabActivated(newTab);
                    }
                });
    }

    private void handleTabActivated(Tab newTab) {
        CodeArea activeEditor = getActiveEditor();

        if (activeEditor != null) {
            undoAvailableProperty.bind(activeEditor.undoAvailableProperty());
            redoAvailableProperty.bind(activeEditor.redoAvailableProperty());
        } else {
            undoAvailableProperty.unbind();
            redoAvailableProperty.unbind();

            undoAvailableProperty.set(false);
            redoAvailableProperty.set(false);
        }
    }

    public void openDocument(@Nullable Path path, String content) {
        EditorDocument doc = new EditorDocument(path, content);

        EditorTab tab = new EditorTab(path, content, doc, this);

        openDocuments.put(tab, doc);
        tabPane.getTabs().add(tab);

        tabPane.getSelectionModel().select(tab);

        activeDocumentAvailable.set(true);
    }

    public void closeDocument(EditorTab tab) {
        openDocuments.remove(tab);
        if (openDocuments.isEmpty()) {
            activeDocumentAvailable.set(false);
        }
    }

    public Collection<EditorDocument> getOpenDocuments() {
        return openDocuments.values();
    }

    public boolean anyDirtyDocuments() {
        return openDocuments.values()
                .stream()
                .anyMatch(editorDoc -> editorDoc.dirtyProperty().get());
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public @Nullable CodeArea getActiveEditor() {
        EditorDocument editorDocument = getActiveDocument();
        return (editorDocument != null) ? editorDocument.getEditor() : null;
    }

    public @Nullable EditorDocument getActiveDocument () {
        return openDocuments.get(tabPane.getSelectionModel().getSelectedItem());
    }

    public BooleanProperty activeDocumentAvailableProperty () {
        return activeDocumentAvailable;
    }

    public BooleanProperty undoAvailableProperty () {
        return undoAvailableProperty;
    }

    public BooleanProperty redoAvailableProperty () {
        return redoAvailableProperty;
    }
}
