package nl.pallett.jsoneditor.actions.view;

import nl.pallett.jsoneditor.controller.EditorManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DecreaseFontSizeAction extends AbstractAction {
    private final EditorManager editorManager;

    public DecreaseFontSizeAction(EditorManager editorManager) {
        this.editorManager = editorManager;
        putValue(NAME, "Decrease Font Size");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editorManager.decreaseFontSize();
    }
}
