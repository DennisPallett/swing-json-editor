package nl.pallett.jsoneditor.actions.view;

import nl.pallett.jsoneditor.controller.EditorManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class IncreaseFontSizeAction extends AbstractAction {
    private final EditorManager editorManager;

    public IncreaseFontSizeAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Increase Font Size");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editorManager.increaseFontSize();
    }
}
