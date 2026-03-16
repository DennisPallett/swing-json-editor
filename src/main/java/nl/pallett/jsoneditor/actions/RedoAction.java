package nl.pallett.jsoneditor.actions;

import java.awt.event.ActionEvent;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.EditorPanelView;

public class RedoAction extends AbstractActionWithState{
    private final EditorManager editorManager;

    public RedoAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Redo");
    }

    @Override
    void updateState() {
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
