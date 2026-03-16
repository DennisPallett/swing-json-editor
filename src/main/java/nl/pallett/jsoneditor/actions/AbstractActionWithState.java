package nl.pallett.jsoneditor.actions;

import javax.swing.AbstractAction;
import nl.pallett.jsoneditor.view.EditorPanelView;
import org.jspecify.annotations.Nullable;

public abstract class AbstractActionWithState extends AbstractAction {
    public abstract void updateState(@Nullable EditorPanelView editorPanel);
}
