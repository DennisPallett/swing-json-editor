package nl.pallett.jsoneditor.ui;

import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.menu.FileMenu;
import nl.pallett.jsoneditor.ui.editor.tabs.EditorTabbedPane;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final EditorManager editorManager;


    public MainFrame() {
        super("My Swing App");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        EditorTabbedPane editorTabbedPane = new EditorTabbedPane();
        editorManager = new EditorManager(editorTabbedPane);

        setJMenuBar(createMenuBar());

        add(editorTabbedPane, BorderLayout.CENTER);



        // initialize with a new document
        editorManager.newDocument();

        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.add(new FileMenu(editorManager));
        return bar;
    }

    public static void showError(Exception e) {
        JOptionPane.showMessageDialog(null,
            "An error occurred: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}