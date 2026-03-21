package nl.pallett.jsoneditor.menu;

import nl.pallett.jsoneditor.actions.ActionManager;

import javax.swing.*;

public class FindMenu extends JMenu {
    public FindMenu(ActionManager actionManager) {
        super("Find");

        add(new JMenuItem(actionManager.getAction(ActionManager.Action.FIND)));
        add(new JMenuItem(actionManager.getAction(ActionManager.Action.REPLACE)));
        add(new JMenuItem(actionManager.getAction(ActionManager.Action.GOTO_LINE)));
    }
}
