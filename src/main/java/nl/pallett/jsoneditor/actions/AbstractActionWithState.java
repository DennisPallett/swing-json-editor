package nl.pallett.jsoneditor.actions;

import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import org.jspecify.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractActionWithState extends AbstractAction {
    public abstract void updateState(@Nullable EditorPanelView editorPanel);
}
