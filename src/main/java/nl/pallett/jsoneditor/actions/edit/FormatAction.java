package nl.pallett.jsoneditor.actions.edit;

import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import org.jspecify.annotations.Nullable;

import java.awt.event.ActionEvent;

public class FormatAction extends AbstractActionWithState {

    private final EditorManager editorManager;

    public FormatAction(EditorManager editorManager) {
        this.editorManager = editorManager;

        putValue(NAME, "Format");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorDocument editorDocument = editorManager.getActiveDocument();
        if (editorDocument != null) {
            editorDocument.formatContents();
        }
    }

    @Override
    public void updateState(@Nullable EditorPanelView editorPanel) {
        setEnabled(
            editorPanel != null
                && editorPanel.getEditorDocument() != null
                && editorPanel.getEditorDocument().canBeFormatted()
        );
    }
}
