package nl.pallett.jsoneditor.ui.editor;

import nl.pallett.jsoneditor.model.EditorDocument;

import javax.swing.*;
import java.awt.*;

public class TreePanel extends JPanel {
    private final EditorDocument editorDocument;

    public TreePanel (EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setLayout(new BorderLayout());

        JTree jtree = new JTree();

        add (jtree);
    }
}
