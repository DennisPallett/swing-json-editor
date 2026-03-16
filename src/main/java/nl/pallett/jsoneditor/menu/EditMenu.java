package nl.pallett.jsoneditor.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;

public class EditMenu extends JMenu  {
    private final EditorManager editorManager;

    public EditMenu(EditorManager editorManager) {
        super("Edit");
        this.editorManager = editorManager;

        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem formatJsonItem = new JMenuItem("Format JSON");

        undoItem.setAccelerator(KeyStroke.getKeyStroke("meta Z"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke("meta shift Z"));
        formatJsonItem.setAccelerator(KeyStroke.getKeyStroke("meta alt L"));

        add(undoItem);
        add(redoItem);
        addSeparator();
        add(formatJsonItem);

        undoItem.setEnabled(false); // todo
        redoItem.setEnabled(false); // todo

        //undoItem.addActionListener(_ -> newDocument());
        //redoItem.addActionListener(_ -> newDocument());
        formatJsonItem.addActionListener(_ -> formatJson());
    }

    private void formatJson() {
        EditorDocument editorDocument = editorManager.getActiveDocument();
        if (editorDocument != null) {
            editorDocument.formatContents();
        }
    }
}
