package nl.pallett.jsoneditor.view;

import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;

public interface EditorTabbedView {
    EditorPanelView addTab(EditorDocument editorDocument);
    void setEditorManager(EditorManager editorManager);
}
