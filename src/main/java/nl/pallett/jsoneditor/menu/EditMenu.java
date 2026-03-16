package nl.pallett.jsoneditor.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;

public class EditMenu extends JMenu  {
    private final EditorManager editorManager;

    private JMenuItem undoItem;
    private JMenuItem redoItem;
    private JMenuItem formatJsonItem;

    public EditMenu(EditorManager editorManager) {
        super("Edit");
        this.editorManager = editorManager;

        undoItem = new JMenuItem("Undo");
        redoItem = new JMenuItem("Redo");
        formatJsonItem = new JMenuItem(editorManager.getActionManager().getFormatAction());

        undoItem.setAccelerator(KeyStroke.getKeyStroke("meta Z"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke("meta shift Z"));
        formatJsonItem.setAccelerator(KeyStroke.getKeyStroke("meta alt L"));

        add(undoItem);
        add(redoItem);
        addSeparator();
        add(formatJsonItem);

        //undoItem.addActionListener(_ -> newDocument());
        //redoItem.addActionListener(_ -> newDocument());

        //updateState();
    }

    private void updateState() {
        boolean formatEnabled = false;
        boolean undoEnabled = false;
        boolean redoEnabled = false;

        EditorDocument editorDocument = editorManager.getActiveDocument();
        if (editorDocument != null) {
            formatEnabled = editorDocument.canBeFormatted();
        }

        undoItem.setEnabled(undoEnabled);
        redoItem.setEnabled(redoEnabled);
        formatJsonItem.setEnabled(formatEnabled);
    }
}
