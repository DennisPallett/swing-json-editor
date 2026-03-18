package nl.pallett.jsoneditor.actions.edit;

import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;

import java.awt.event.ActionEvent;

public class UndoAction extends AbstractActionWithState {
    private final EditorManager editorManager;

    public UndoAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Undo");
    }

    @Override
    public void updateState(EditorPanelView editorPanel) {
        setEnabled (editorPanel != null && editorPanel.canUndo());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPanelView activeEditorPanel = editorManager.getActiveEditorPanel();
        if (activeEditorPanel != null) {
            activeEditorPanel.undo();
        }
    }
}
