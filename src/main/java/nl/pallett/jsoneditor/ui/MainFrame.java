package nl.pallett.jsoneditor.ui;

import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.menu.FileMenu;
import nl.pallett.jsoneditor.ui.tabs.EditorTabbedPane;
import nl.pallett.jsoneditor.view.MainView;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame implements MainView {
    private final EditorManager editorManager;


    public MainFrame() {
        super("Swing JSON Editor");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        EditorTabbedPane editorTabbedPane = new EditorTabbedPane();
        editorManager = new EditorManager(this, editorTabbedPane);

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

    @Override
    public File[] showOpenFileDialog() {
        FileDialog dialog = new FileDialog(this, "Open File", FileDialog.LOAD);
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        dialog.setMultipleMode(true);

        return dialog.getFiles();
    }
}