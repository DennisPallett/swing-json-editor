package nl.pallett.jsoneditor.actions.edit;

import java.awt.event.ActionEvent;
import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.EditorPanelView;

public class RedoAction extends AbstractActionWithState {
    private final EditorManager editorManager;

    public RedoAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Redo");
    }

    @Override
    public void updateState(EditorPanelView editorPanel) {
        EditorPanelView activeEditorPanel = editorManager.getActiveEditorPanel();
        setEnabled (activeEditorPanel != null && activeEditorPanel.canRedo());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPanelView activeEditorPanel = editorManager.getActiveEditorPanel();
        if (activeEditorPanel != null) {
            activeEditorPanel.redo();
        }
    }
}
