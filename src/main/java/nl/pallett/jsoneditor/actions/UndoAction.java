package nl.pallett.jsoneditor.actions;

import java.awt.event.ActionEvent;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.EditorPanelView;

public class UndoAction extends AbstractActionWithState{
    private final EditorManager editorManager;

    public UndoAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Undo");
    }

    @Override
    void updateState() {
        EditorPanelView activeEditorPanel = editorManager.getActiveEditorPanel();
        setEnabled (activeEditorPanel != null && activeEditorPanel.canUndo());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPanelView activeEditorPanel = editorManager.getActiveEditorPanel();
        if (activeEditorPanel != null) {
            activeEditorPanel.undo();
        }
    }
}
