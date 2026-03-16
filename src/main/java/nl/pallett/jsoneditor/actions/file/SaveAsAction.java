package nl.pallett.jsoneditor.actions.file;

import java.awt.event.ActionEvent;
import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.EditorPanelView;
import org.jspecify.annotations.Nullable;

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
