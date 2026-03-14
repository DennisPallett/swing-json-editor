package nl.pallett.jsoneditor.menu;

import nl.pallett.jsoneditor.controller.EditorManager;

import javax.swing.*;

public class FileMenu extends JMenu {
    private final EditorManager editorManager;

    public FileMenu(EditorManager editorManager) {
        super("File");

        this.editorManager = editorManager;

        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open...");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        JMenuItem saveAllItem = new JMenuItem("Save All");

        newItem.setAccelerator(KeyStroke.getKeyStroke("meta T"));
        openItem.setAccelerator(KeyStroke.getKeyStroke("meta O"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke("meta S"));
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("meta S"));
        saveAllItem.setAccelerator(KeyStroke.getKeyStroke("ctrl meta S"));

        add(newItem);
        add(openItem);
        add(saveItem);
        add(saveAsItem);
        add(saveAllItem);

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
