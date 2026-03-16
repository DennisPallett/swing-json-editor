package nl.pallett.jsoneditor.actions.file;

import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import org.jspecify.annotations.Nullable;

import java.awt.event.ActionEvent;

public class SaveAction extends AbstractActionWithState {
    public final EditorManager editorManager;

    public SaveAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Save");
    }

    @Override
    public void updateState(@Nullable EditorPanelView editorPanel) {
        setEnabled(editorPanel != null
            && editorPanel.getEditorDocument() != null
            && editorPanel.getEditorDocument().getFilePath() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editorManager.saveFile(editorManager.getActiveDocument());
    }
}
