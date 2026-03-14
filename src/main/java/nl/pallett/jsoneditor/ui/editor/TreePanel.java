package nl.pallett.jsoneditor.ui.editor;

import javax.swing.*;
import java.awt.*;

public class TreePanel extends JPanel {
    public TreePanel () {
        setLayout(new BorderLayout());

        JTree jtree = new JTree();

        add (jtree);
    }
}
