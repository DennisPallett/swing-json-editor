package nl.pallett.jsoneditor.actions.find;

import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowGotoLineDialogAction extends AbstractActionWithState {
    private final EditorManager editorManager;

    public ShowGotoLineDialogAction(EditorManager editorManager) {
        this.editorManager = editorManager;

        putValue(NAME, "Go to line...");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("meta L"));
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
            activeEditorPanel.getCodePanel().showGotoLineDialog();
        }
    }

}
