package nl.pallett.jsoneditor.actions;

import javax.swing.AbstractAction;

public abstract class AbstractActionWithState extends AbstractAction {
    abstract void updateState();

}
