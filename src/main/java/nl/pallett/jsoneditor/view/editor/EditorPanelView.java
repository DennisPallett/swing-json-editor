package nl.pallett.jsoneditor.view.editor;

import nl.pallett.jsoneditor.model.EditorDocument;

public interface EditorPanelView {
    EditorDocument getEditorDocument();
    CodePanelView getCodePanel();
}
