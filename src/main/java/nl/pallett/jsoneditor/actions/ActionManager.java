package nl.pallett.jsoneditor.actions;

import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import nl.pallett.jsoneditor.controller.EditorManager;

public class ActionManager {
    private final EditorManager editorManager;

    private final Map<Action, AbstractAction> actions = new HashMap<>();

    public ActionManager(EditorManager editorManager) {
        this.editorManager = editorManager;

        actions.put(Action.FORMAT, new FormatAction(editorManager));
    }

    public void updateState() {
        actions.values().stream()
            .filter(AbstractActionWithState.class::isInstance)
            .forEach(a -> ((AbstractActionWithState)a).updateState());
    }

    public AbstractAction getFormatAction() {
        return actions.get(Action.FORMAT);
    }



    public enum Action {
        FORMAT
    }

}
