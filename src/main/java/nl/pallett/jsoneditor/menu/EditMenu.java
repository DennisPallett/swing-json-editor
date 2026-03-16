package nl.pallett.jsoneditor.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import nl.pallett.jsoneditor.actions.ActionManager;
import nl.pallett.jsoneditor.actions.ActionManager.Action;
import nl.pallett.jsoneditor.controller.EditorManager;

public class EditMenu extends JMenu  {

    public EditMenu(EditorManager editorManager) {
        super("Edit");
        ActionManager actionManager = editorManager.getActionManager();

        var undoItem = new JMenuItem(actionManager.getAction(Action.UNDO));
        var redoItem = new JMenuItem(actionManager.getAction(Action.REDO));
        var formatJsonItem = new JMenuItem(actionManager.getAction(Action.FORMAT));

        undoItem.setAccelerator(KeyStroke.getKeyStroke("meta Z"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke("meta shift Z"));
        formatJsonItem.setAccelerator(KeyStroke.getKeyStroke("meta alt L"));

        add(undoItem);
        add(redoItem);
        addSeparator();
        add(formatJsonItem);
    }
}
