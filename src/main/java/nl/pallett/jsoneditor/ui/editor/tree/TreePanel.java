package nl.pallett.jsoneditor.ui.editor.tree;

import nl.pallett.jsoneditor.editor.ast.AstNode;
import nl.pallett.jsoneditor.model.EditorDocument;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class TreePanel extends JPanel {
    private final EditorDocument editorDocument;

    private final TreeBuilder treeBuilder = new TreeBuilder();

    private final JTree tree;

    public TreePanel (EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setLayout(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("$");
        tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(24);
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.setCellRenderer(new TreeCellRenderer());

        // create initial tree
        refreshTree();

        add (tree);

        addAstListener();
    }

    private void addAstListener() {
        editorDocument.addPropertyChangeListener(event -> {
            if (event.getPropertyName().equals(EditorDocument.Property.AST_TREE.name())) {
                refreshTree();
            }
        });
    }

    private void refreshTree() {
        AstNode astTree = editorDocument.getAstTree();
        if (astTree != null) {
            DefaultMutableTreeNode newRoot = treeBuilder.buildTree(astTree);
            tree.setModel(new DefaultTreeModel(newRoot));
        }
    }
}
