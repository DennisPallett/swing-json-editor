package nl.pallett.jsoneditor.menu;

import nl.pallett.jsoneditor.actions.ActionManager;
import nl.pallett.jsoneditor.actions.ActionManager.Action;

import javax.swing.*;

public class ViewMenu extends JMenu {

    public ViewMenu(ActionManager actionManager) {
        super("View");

        var increaseItem = new JMenuItem(actionManager.getAction(Action.INCREASE_FONT_SIZE));
        var decreaseItem = new JMenuItem(actionManager.getAction(Action.DECREASE_FONT_SIZE));
        var resetItem = new JMenuItem(actionManager.getAction(Action.RESET_FONT_SIZE));

        increaseItem.setAccelerator(KeyStroke.getKeyStroke("meta EQUALS"));
        decreaseItem.setAccelerator(KeyStroke.getKeyStroke("meta MINUS"));
        resetItem.setAccelerator(KeyStroke.getKeyStroke("meta 0"));

        add(increaseItem);
        add(decreaseItem);
        addSeparator();
        add(resetItem);
    }
}
