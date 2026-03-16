package nl.pallett.jsoneditor.view;

import nl.pallett.jsoneditor.model.EditorDocument;

public interface EditorPanelView {
    EditorDocument getEditorDocument();

    void undo();
    void redo();
    boolean canUndo();
    boolean canRedo();
}
