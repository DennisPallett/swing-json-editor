package nl.pallett.jsoneditor.actions.view;

import nl.pallett.jsoneditor.controller.EditorManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ResetFontSizeAction extends AbstractAction {
    private final EditorManager editorManager;

    public ResetFontSizeAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Reset Font Size");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editorManager.resetFontSize();
    }
}
