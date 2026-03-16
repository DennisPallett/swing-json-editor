package nl.pallett.jsoneditor.view;

import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;
import org.jspecify.annotations.Nullable;

public interface EditorTabbedView {
    EditorPanelView addTab(EditorDocument editorDocument);
    void setEditorManager(EditorManager editorManager);
    @Nullable EditorPanelView getActiveEditorPanel();
}
