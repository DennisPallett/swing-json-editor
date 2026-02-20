package nl.pallett;

import javax.swing.*;

public class MainGUI {
    public MainGUI () {
        // Creating instance of JFrame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Creating instance of JButton
        JButton button = new JButton("Test");

        // x axis, y axis, width, height
        button.setBounds(150, 200, 220, 50);

        // adding button in JFrame
        frame.add(button);

        // 400 width and 500 height
        frame.setSize(500, 600);

        // using no layout managers
        frame.setLayout(null);

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenu newFile = new JMenu("New");
        JMenuItem newEmpty = new JMenuItem("Empty file");
        JMenuItem newFromPaste = new JMenuItem("From pasteboard");
        newFile.add(newEmpty);
        newFile.add(newFromPaste);

        menu.add(newFile);

        JMenuItem openFile = new JMenuItem("Open");
        menu.add(openFile);
        menubar.add(menu);
        frame.setJMenuBar(menubar);

        // making the frame visible
        frame.setVisible(true);
    }
}
