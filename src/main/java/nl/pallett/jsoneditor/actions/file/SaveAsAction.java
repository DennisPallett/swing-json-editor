package nl.pallett.jsoneditor.actions.file;

import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import org.jspecify.annotations.Nullable;

import java.awt.event.ActionEvent;

public class SaveAsAction extends AbstractActionWithState {
    public final EditorManager editorManager;

    public SaveAsAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Save As...");
    }

    @Override
    public void updateState(@Nullable EditorPanelView editorPanel) {
        setEnabled(editorPanel != null
            && editorPanel.getEditorDocument() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editorManager.saveFileAs();
    }
}
