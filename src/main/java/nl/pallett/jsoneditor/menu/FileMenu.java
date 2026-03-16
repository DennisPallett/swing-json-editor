package nl.pallett.jsoneditor.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import nl.pallett.jsoneditor.actions.ActionManager;
import nl.pallett.jsoneditor.controller.EditorManager;

public class FileMenu extends JMenu {
    private final EditorManager editorManager;

    public FileMenu(EditorManager editorManager) {
        super("File");

        this.editorManager = editorManager;
        ActionManager actionManager = editorManager.getActionManager();

        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open...");
        JMenuItem saveItem = new JMenuItem(actionManager.getAction(ActionManager.Action.SAVE));
        JMenuItem saveAsItem = new JMenuItem(actionManager.getAction(ActionManager.Action.SAVE_AS));

        newItem.setAccelerator(KeyStroke.getKeyStroke("meta T"));
        openItem.setAccelerator(KeyStroke.getKeyStroke("meta O"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke("meta S"));
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("meta S"));

        add(newItem);
        add(openItem);
        add(saveItem);
        add(saveAsItem);

        newItem.addActionListener(_ -> newDocument());
        openItem.addActionListener(_ -> openFile());
    }

    private void newDocument() {
        editorManager.newDocument();
    }

    private void openFile() {
        // TODO: shown open file picker
        editorManager.selectFileToOpen();
        //editorManager.openFile(Path.of("/Users/dennis/Downloads/test.json"));
    }

}
