package nl.pallett.jsoneditor.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class FileMenu extends JMenu {
    public FileMenu() {
        super("File");

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

        newItem.addActionListener(e -> newDocument());
    }

    private void newDocument() {

    }

}
