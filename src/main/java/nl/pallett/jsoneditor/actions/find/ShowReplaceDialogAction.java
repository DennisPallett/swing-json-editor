package nl.pallett.jsoneditor.actions.find;

import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowReplaceDialogAction extends AbstractActionWithState {
    private final EditorManager editorManager;

    public ShowReplaceDialogAction(EditorManager editorManager) {
        this.editorManager = editorManager;

        putValue(NAME, "Replace...");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("meta R"));
    }

    @Override
    public void updateState(EditorPanelView editorPanel) {
        EditorPanelView activeEditorPanel = editorManager.getActiveEditorPanel();
        setEnabled (activeEditorPanel != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPanelView activeEditorPanel = editorManager.getActiveEditorPanel();
        if (activeEditorPanel != null) {
            activeEditorPanel.getCodePanel().showReplaceDialog();
        }
    }

}
