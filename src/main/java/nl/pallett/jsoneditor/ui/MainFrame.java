package nl.pallett.jsoneditor.ui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import nl.pallett.jsoneditor.editor.EditorManager;
import nl.pallett.jsoneditor.menu.FileMenu;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("My Swing App");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        setJMenuBar(createMenuBar());
        //add(createMainPanel(), BorderLayout.CENTER);
        add(new EditorManager(), BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel();

        JButton button = new JButton("Click Me");

        button.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Hello from macOS Swing!")
        );

        panel.add(button);
        return panel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        // JMenu fileMenu = new JMenu("File");
        //
        // JMenuItem exit = new JMenuItem("Quit");
        // exit.setAccelerator(
        //     KeyStroke.getKeyStroke("meta Q") // Cmd+Q on Mac
        // );
        //
        // exit.addActionListener(e -> System.exit(0));
        //
        // fileMenu.add(exit);

        bar.add(new FileMenu());

        return bar;
    }

    public static void showError(Exception e) {
        JOptionPane.showMessageDialog(null,
            "An error occurred: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}