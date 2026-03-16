package nl.pallett.jsoneditor.actions;

import java.awt.event.ActionEvent;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;

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
    void updateState() {
        var enabled = false;

        EditorDocument editorDocument = editorManager.getActiveDocument();
        if (editorDocument != null) {
            enabled = editorDocument.canBeFormatted();
        }

        setEnabled(enabled);
    }
}
