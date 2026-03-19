package nl.pallett.jsoneditor.actions;

import nl.pallett.jsoneditor.actions.edit.FormatAction;
import nl.pallett.jsoneditor.actions.edit.RedoAction;
import nl.pallett.jsoneditor.actions.edit.UndoAction;
import nl.pallett.jsoneditor.actions.file.SaveAction;
import nl.pallett.jsoneditor.actions.file.SaveAsAction;
import nl.pallett.jsoneditor.actions.find.ShowFindDialogAction;
import nl.pallett.jsoneditor.actions.find.ShowGotoLineDialogAction;
import nl.pallett.jsoneditor.actions.find.ShowReplaceDialogAction;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActionManager {
    private final EditorManager editorManager;

    private final Map<Action, AbstractAction> actions = new HashMap<>();

    public ActionManager(EditorManager editorManager) {
        this.editorManager = editorManager;

        actions.put(Action.SAVE, new SaveAction(editorManager));
        actions.put(Action.SAVE_AS, new SaveAsAction(editorManager));
        actions.put(Action.FORMAT, new FormatAction(editorManager));
        actions.put(Action.UNDO, new UndoAction(editorManager));
        actions.put(Action.REDO, new RedoAction(editorManager));
        actions.put(Action.FIND, new ShowFindDialogAction(editorManager));
        actions.put(Action.REPLACE, new ShowReplaceDialogAction(editorManager));
        actions.put(Action.GOTO_LINE, new ShowGotoLineDialogAction(editorManager));
    }

    public void updateState(@Nullable EditorPanelView editorPanel) {
        actions.values().stream()
            .filter(AbstractActionWithState.class::isInstance)
            .forEach(a -> ((AbstractActionWithState)a).updateState(editorPanel));
    }

    public AbstractAction getAction(Action action) {
        AbstractAction result = actions.get(action);
        Objects.requireNonNull(result);
        return result;
    }

    public AbstractAction getFormatAction() {
        return actions.get(Action.FORMAT);
    }

    public enum Action {
        SAVE,
        SAVE_AS,
        FORMAT,
        UNDO,
        REDO,
        FIND,
        REPLACE,
        GOTO_LINE
    }

}

